package com.scanvision.data.remote.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val ARTICLE_URL = "https://newsapi.org/"
    private const val BASE_URL = "https://user-matane-api-951509849147.asia-southeast2.run.app/"

    val api: NewsApiService by lazy {
        createRetrofit(ARTICLE_URL).create(NewsApiService::class.java)
    }

    val modelApi: ModelApiService by lazy {
        createRetrofit(BASE_URL).create(ModelApiService::class.java)
    }

    val userApi: UserApiService by lazy {
        createRetrofit(BASE_URL).create(UserApiService::class.java)
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
