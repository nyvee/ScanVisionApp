package com.scanvision.data.remote.response

data class Article (
    val title: String,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val content: String?
)

data class ApiResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)