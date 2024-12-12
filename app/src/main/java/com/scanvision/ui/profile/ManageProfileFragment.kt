package com.scanvision.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.scanvision.R
import com.scanvision.data.UserRepository
import com.scanvision.data.remote.response.UpdateUserRequest
import com.scanvision.databinding.FragmentManageProfileBinding
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ManageProfileFragment : Fragment() {

    private var _binding: FragmentManageProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository
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
        _binding = FragmentManageProfileBinding.inflate(inflater, container, false)
        userRepository = UserRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGenderSpinner()
        loadUserData()
        binding.btnChangeData.setOnClickListener {
            confirmChanges()
        }
        binding.profileCardView.setOnClickListener {
            showImageSourceDialog()
        }
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun loadUserData() {
        val user = userRepository.getUserSession(requireContext())
        user?.let {
            binding.edEditAddress.hint = it.address ?: "Address"
            binding.edEditAge.hint = it.age?.toString() ?: "Age"
            val genderPosition = (binding.spinnerGender.adapter as ArrayAdapter<String>).getPosition(it.gender)
            binding.spinnerGender.setSelection(genderPosition)
        }
    }

    private fun setupGenderSpinner() {
        val genderSpinner: Spinner = binding.spinnerGender
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderSpinner.adapter = adapter
        }
    }

    private fun confirmChanges() {
        val address = binding.edEditAddress.text.toString().takeIf { it.isNotBlank() }
        val age = binding.edEditAge.text.toString().toIntOrNull()
        val gender = binding.spinnerGender.selectedItem.toString().takeIf { it.isNotBlank() }

        val userUUID = userRepository.getUserUUID()
        if (userUUID != null) {
            lifecycleScope.launch {
                val currentUser = withContext(Dispatchers.IO) {
                    userRepository.getUserSession(requireContext())
                }
                val updateUserRequest = UpdateUserRequest(
                    address = address ?: currentUser?.address,
                    age = age ?: currentUser?.age,
                    gender = gender ?: currentUser?.gender
                )
                val response = withContext(Dispatchers.IO) {
                    userRepository.updateUser(userUUID, updateUserRequest)
                }
                if (response.isSuccessful) {
                    val updatedUser = currentUser?.copy(
                        address = updateUserRequest.address,
                        age = updateUserRequest.age,
                        gender = updateUserRequest.gender
                    )
                    updatedUser?.let {
                        userRepository.saveUserSession(requireContext(), it)
                    }
                    showToast("User data updated successfully")
                } else {
                    showToast("Failed to update user data")
                }
            }
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
                binding.ivProfileImage.setImageURI(resultUri)
                sendImageToApi(resultUri)
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
            lifecycleScope.launch {
                try {
                    val userUUID = userRepository.getUserUUID()
                    if (userUUID != null) {
                        val response = withContext(Dispatchers.IO) {
                            userRepository.updateProfileImage(userUUID, file)
                        }
                        if (response.isSuccessful) {
                            showToast("Profile image updated successfully")
                        } else {
                            showToast("Failed to update profile image")
                        }
                    }
                } catch (e: Exception) {
                    showToast("Failed to send image: ${e.localizedMessage}")
                }
            }
        } else {
            showToast("File not found")
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
        binding.ivProfileImage.setImageURI(null)
        _binding = null
    }
}