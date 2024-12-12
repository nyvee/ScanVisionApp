package com.scanvision.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.scanvision.R
import com.scanvision.adapter.ArticleAdapter
import com.scanvision.adapter.CarouselAdapter
import com.scanvision.data.UserRepository
import com.scanvision.data.remote.response.ApiResponse
import com.scanvision.data.remote.response.Article
import com.scanvision.data.remote.retrofit.RetrofitInstance
import com.scanvision.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Field

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository
    private val handler = Handler(Looper.getMainLooper())
    private val scrollRunnable = object : Runnable {
        override fun run() {
            val itemCount = binding.viewPager.adapter?.itemCount ?: 0
            if (itemCount > 0) {
                val nextItem = (binding.viewPager.currentItem + 1) % itemCount
                binding.viewPager.setCurrentItem(nextItem, true)
                handler.postDelayed(this, 3000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userRepository = UserRepository(requireContext())
        val user = userRepository.getUserSession(requireContext())
        binding.tvUsername.text = user?.username ?: "Guest"

        val images = listOf(R.drawable.image1, R.drawable.image2, R.drawable.image3)
        val adapter = CarouselAdapter(images)
        binding.viewPager.adapter = adapter

        binding.dotsIndicator.setViewPager2(binding.viewPager)

        fetchArticles()

        binding.btnQuickEyeCheckup.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_cameraFragment)
        }

        setViewPagerScrollDuration(binding.viewPager, 600) // Set ViewPager scroll duration to 600ms
        handler.postDelayed(scrollRunnable, 3000)
    }

    private fun fetchArticles() {
        RetrofitInstance.api.getArticle().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    setupRecyclerView(articles)
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun setupRecyclerView(articles: List<Article>) {
        _binding?.let { binding ->
            binding.rvArticles.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvArticles.adapter = ArticleAdapter(articles, ArticleAdapter.VIEW_TYPE_SHORT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(scrollRunnable)
        _binding = null
    }

    private fun setViewPagerScrollDuration(viewPager: ViewPager2, duration: Int) {
        try {
            val recyclerViewField: Field = ViewPager2::class.java.getDeclaredField("mRecyclerView")
            recyclerViewField.isAccessible = true
            val recyclerView = recyclerViewField.get(viewPager) as RecyclerView
            val layoutManagerField: Field = RecyclerView::class.java.getDeclaredField("mLayoutManager")
            layoutManagerField.isAccessible = true
            val layoutManager = layoutManagerField.get(recyclerView) as RecyclerView.LayoutManager
            val scrollerField: Field = layoutManager.javaClass.getDeclaredField("mSmoothScroller")
            scrollerField.isAccessible = true
            val scroller = scrollerField.get(layoutManager) as RecyclerView.SmoothScroller
            val interpolatorField: Field = scroller.javaClass.getDeclaredField("mInterpolator")
            interpolatorField.isAccessible = true
            interpolatorField.set(scroller, DecelerateInterpolator())
            val durationField: Field = scroller.javaClass.getDeclaredField("mDuration")
            durationField.isAccessible = true
            durationField.setInt(scroller, duration)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}