package com.scanvision.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.scanvision.R
import com.scanvision.data.UserRepository
import com.scanvision.databinding.FragmentProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        userRepository = UserRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwipeRefresh()
        loadUserProfile()
        setupLogoutButton()
        setupManageProfileButton()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadUserProfile()
        }
    }

    private fun loadUserProfile() {
        val userUUID = userRepository.getUserUUID()
        if (userUUID != null) {
            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) {
                    userRepository.getUserSession(requireContext())
                }
                user?.let {
                    binding.tvFullName.text = it.username
                    binding.tvAge.text = it.age?.toString() ?: "N/A"
                    binding.tvGender.text = it.gender ?: "N/A"
                    binding.tvAddress.text = it.address ?: "N/A"
                    loadProfileImage(userUUID)
                }
                binding.swipeRefreshLayout.isRefreshing = false
            }
        } else {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadProfileImage(userUUID: String) {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                userRepository.getProfileImage(userUUID)
            }
            if (response.isSuccessful) {
                val imageUrl = response.body()?.url
                imageUrl?.let {
                    Glide.with(this@ProfileFragment)
                        .load(it)
                        .into(binding.ivProfileImage)
                }
            }
        }
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            userRepository.clearSession()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun setupManageProfileButton() {
        binding.btnModifyProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_manageProfileFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}