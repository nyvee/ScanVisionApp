package com.scanvision.ui.ml

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.scanvision.databinding.FragmentResultBinding
import com.scanvision.utils.DateFormat

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val result = ResultFragmentArgs.fromBundle(requireArguments()).result ?: "No result available"
        val imageUri = ResultFragmentArgs.fromBundle(requireArguments()).imageUri
        val time = ResultFragmentArgs.fromBundle(requireArguments()).time
        val date = ResultFragmentArgs.fromBundle(requireArguments()).date

        binding.resultTextView.text = result
        binding.resultImageView.setImageURI(Uri.parse(imageUri))
        binding.resultTimeTextView.text = DateFormat.formatTime(time)
        binding.resultDateTextView.text = DateFormat.formatDate(date)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.resultImageView.setImageURI(null)
        binding.resultTextView.text = ""
        _binding = null
    }
}