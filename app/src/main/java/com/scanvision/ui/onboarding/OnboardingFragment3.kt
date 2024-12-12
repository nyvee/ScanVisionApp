package com.scanvision.ui.onboarding

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.scanvision.R
import com.scanvision.databinding.FragmentOnboard3Binding

class OnboardingFragment3 : Fragment() {

    private var _binding: FragmentOnboard3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboard3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animate progress bar
        val progressAnimator = ObjectAnimator.ofInt(binding.progressBar, "progress", 66, 100)
        progressAnimator.duration = 1000
        progressAnimator.start()

        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_onboardFragment3_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}