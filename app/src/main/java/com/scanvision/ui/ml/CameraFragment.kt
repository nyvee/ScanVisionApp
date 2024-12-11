package com.scanvision.ui.ml

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.scanvision.data.local.AppDatabase
import com.scanvision.data.local.ResultEntity
import com.scanvision.data.remote.retrofit.RetrofitInstance
import com.scanvision.databinding.FragmentCameraBinding
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private var croppedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { startCrop(it) }
    }

    private val captureImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            croppedImageUri?.let { startCrop(it) }
        } else {
            showToast("Failed to capture image")
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            showToast("Camera permission is required to use the camera")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.previewCardView.setOnClickListener {
            showImageSourceDialog()
        }

        binding.btnContinue.setOnClickListener {
            croppedImageUri?.let { uri ->
                sendImageToApi(uri)
                Log.d("CameraFragment", "Sending image to API with URI: $uri")
            } ?: showToast("No image selected")
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Image Source")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkAndRequestPermissions()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun checkAndRequestPermissions() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showToast("Camera permission is required to use the camera")
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val imageFile = File(requireContext().cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
        val imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )
        croppedImageUri = imageUri
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        captureImageLauncher.launch(intent)
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun startCrop(uri: Uri) {
        val cacheDir = File(requireContext().cacheDir, "images").apply { mkdirs() }
        val destinationUri = Uri.fromFile(File(cacheDir, "croppedImage_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            setCompressionQuality(80)
            setFreeStyleCropEnabled(true)
        }
        UCrop.of(uri, destinationUri)
            .withOptions(options)
            .start(requireContext(), this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = data?.let { UCrop.getOutput(it) }
            if (resultUri != null) {
                croppedImageUri = resultUri
                binding.previewImageView.setImageURI(resultUri)
            } else {
                showToast("Failed to retrieve cropped image")
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = data?.let { UCrop.getError(it) }
            showToast("Cropping failed: ${cropError?.message}")
        }
    }

    private fun sendImageToApi(uri: Uri) {
        val file = getFileFromUri(requireContext(), uri)
        if (file != null) {
            val mimeType = requireContext().contentResolver.getType(uri) ?: "image/jpeg"
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            Log.d("CameraFragment", "Sending image with MIME type: $mimeType and file path: ${file.path}")

            binding.progressBar.visibility = View.VISIBLE
            binding.btnContinue.isEnabled = false

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.modelApi.getPrediction(
                        "Uhpy7TGejgwxMkp9iFX-VpA2OtphfUcO-YUd9GsjP2c",
                        body
                    )
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.btnContinue.isEnabled = true
                        if (!response.error) {
                            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val resultEntity = ResultEntity(
                                imageUri = uri.toString(),
                                classificationResult = response.result,
                                time = currentTime,
                                date = currentDate
                            )
                            saveResultToDatabase(resultEntity)
                            val action = CameraFragmentDirections.actionCameraFragmentToResultFragment(
                                result = response.result,
                                imageUri = file.path,
                                time = currentTime,
                                date = currentDate
                            )
                            findNavController().navigate(action)
                        } else {
                            showToast(response.message ?: "Error occurred")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.btnContinue.isEnabled = true
                        showToast("Failed to send image: ${e.localizedMessage}")
                    }
                }
            }
        } else {
            showToast("File not found")
        }
    }

    private fun saveResultToDatabase(resultEntity: ResultEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(requireContext())
            database.resultDao().insertResult(resultEntity)
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return tempFile
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        croppedImageUri = null
        binding.previewImageView.setImageURI(null)
        _binding = null
    }
}
