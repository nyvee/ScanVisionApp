package com.scanvision.data.remote.retrofit

import com.scanvision.data.UserRequest
import com.scanvision.data.remote.response.ApiResponse
import com.scanvision.data.remote.response.LoginRequest
import com.scanvision.data.remote.response.LoginResponse
import com.scanvision.data.remote.response.ModelResponse
import com.scanvision.data.remote.response.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface NewsApiService {
    @GET("v2/everything?q=cataract &apiKey=599581a512a44dc4ac2c444628d2b5ef")
    fun getArticle(): Call<ApiResponse>
}

interface ModelApiService {
    @Multipart
    @POST("predict")
    suspend fun getPrediction(
        @Header("x-api-key") apiKey: String,
        @Part photo: MultipartBody.Part
    ): ModelResponse
}

interface UserApiService {
    @POST("register")
    suspend fun register(
        @Header("x-api-key") apiKey: String,
        @Body request: UserRequest
    ): Response<ResponseBody>

    @POST("login")
    suspend fun login(
        @Header("x-api-key") apiKey: String,
        @Body request: LoginRequest
    ): Response<ResponseBody>
}