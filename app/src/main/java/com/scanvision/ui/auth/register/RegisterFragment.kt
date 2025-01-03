package com.scanvision.ui.auth.register

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.scanvision.R
import com.scanvision.ui.auth.AuthViewModel
import com.scanvision.ui.auth.AuthViewModelFactory
import kotlinx.coroutines.launch
import androidx.navigation.NavOptions

class RegisterFragment : Fragment() {
    private lateinit var viewModel: AuthViewModel
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var registeredEmailTextView: TextView
    private lateinit var registerButton: Button
    private lateinit var goToLoginButton: Button
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, AuthViewModelFactory(requireContext())).get(AuthViewModel::class.java)
        goToLoginButton = view.findViewById(R.id.goToLoginButton)
        nameEditText = view.findViewById(R.id.ed_register_name)
        emailEditText = view.findViewById(R.id.ed_register_email)
        passwordEditText = view.findViewById(R.id.ed_register_password)
        passwordVisibilityToggle = view.findViewById(R.id.passwordVisibilityToggle)
        registeredEmailTextView = view.findViewById(R.id.registeredEmailTextView)
        registerButton = view.findViewById(R.id.signupButton) // Initialize registerButton here

        (activity as AppCompatActivity).supportActionBar?.hide()

        passwordVisibilityToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            registerButton.isEnabled = false
            registerButton.text = ""
            registerButton.setBackgroundColor(resources.getColor(R.color.gray))
            context?.let { it1 -> ContextCompat.getColor(it1, R.color.white_1) }?.let { it2 ->
            }

            lifecycleScope.launch {
                try {
                    viewModel.register(name, email, password) { isSuccess, message ->
                        registerButton.isEnabled = true
                        registerButton.text = "Register"
                        registerButton.setBackgroundColor(resources.getColor(R.color.black_1))
                        if (isSuccess) {
                            Toast.makeText(requireContext(), "Registration Successful!", Toast.LENGTH_SHORT).show()
                            val navOptions = NavOptions.Builder()
                                .setEnterAnim(R.anim.slide_in_right)
                                .setExitAnim(R.anim.slide_out_left)
                                .setPopEnterAnim(R.anim.slide_in_left)
                                .setPopExitAnim(R.anim.slide_out_right)
                                .build()
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment, null, navOptions)
                        } else {
                            registeredEmailTextView.text = message
                        }
                    }
                } catch (e: Exception) {
                    registerButton.isEnabled = true
                    registerButton.text = "Register"
                    registerButton.setBackgroundColor(resources.getColor(R.color.gray))
                    registeredEmailTextView.text = e.message
                }
            }
        }

        goToLoginButton.setOnClickListener {
            goToLoginButton.isEnabled = false
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_left)
                .setExitAnim(R.anim.slide_out_right)
                .setPopEnterAnim(R.anim.slide_in_right)
                .setPopExitAnim(R.anim.slide_out_left)
                .build()
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment, null, navOptions)
            goToLoginButton.isEnabled = true
        }

        viewModel.registrationState.observe(viewLifecycleOwner) { isRegistered ->
            if (isRegistered) {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility_off)
        } else {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility)
        }
        passwordEditText.setSelection(passwordEditText.text.length)
        isPasswordVisible = !isPasswordVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}