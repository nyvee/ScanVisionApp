package com.scanvision.ui.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.scanvision.adapter.ArticleAdapter
import com.scanvision.data.remote.response.ApiResponse
import com.scanvision.data.remote.response.Article
import com.scanvision.data.remote.retrofit.RetrofitInstance
import com.scanvision.databinding.FragmentArticleBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArticleFragment : Fragment() {

    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchArticles()
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
        binding.rvArticlePage.layoutManager = LinearLayoutManager(context)
        binding.rvArticlePage.adapter = ArticleAdapter(articles, ArticleAdapter.VIEW_TYPE_LONG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}