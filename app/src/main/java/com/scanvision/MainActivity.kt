package com.scanvision

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.scanvision.data.UserRepository
import com.scanvision.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(this)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        supportActionBar?.hide()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.articleFragment, R.id.historyFragment, R.id.profileFragment -> navView.visibility = View.VISIBLE
                else -> navView.visibility = View.GONE
            }
        }

        if (userRepository.isLoggedIn()) {
            // Navigate to Home Screen
            navController.navigate(R.id.homeFragment)
        } else {
            // Navigate to Login Screen
            navController.navigate(R.id.loginFragment)
        }
    }
}