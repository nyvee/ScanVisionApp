package com.scanvision.data.remote.retrofit

import com.scanvision.data.remote.response.ApiResponse
import com.scanvision.data.remote.response.LoginData
import com.scanvision.data.remote.response.LoginRequest
import com.scanvision.data.remote.response.ModelResponse
import com.scanvision.data.remote.response.ProfileImageResponse
import com.scanvision.data.remote.response.RegisterRequest
import com.scanvision.data.remote.response.UpdateImageRequest
import com.scanvision.data.remote.response.UpdateUserRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface NewsApiService {
    @GET("v2/everything?q=health&apiKey=599581a512a44dc4ac2c444628d2b5ef")
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
    @POST("regis")
    suspend fun register(
        @Header("x-api-key") apiKey: String,
        @Body request: RegisterRequest
    ): Response<ResponseBody>

    @POST("login")
    suspend fun login(
        @Header("x-api-key") apiKey: String,
        @Body request: LoginRequest
    ): Response<ResponseBody>

    @GET("user/profile/{useruuid}")
    suspend fun getProfileImage(
        @Header("x-api-key") apiKey: String,
        @Path("useruuid") useruuid: String
    ): Response<ProfileImageResponse>

    @Multipart
    @POST("user/profile/{useruuid}")
    suspend fun updateProfileImage(
        @Header("x-api-key") apiKey: String,
        @Path("useruuid") useruuid: String,
        @Part image: MultipartBody.Part
    ): Response<ResponseBody>

    @PUT("user/personal/{useruuid}")
    suspend fun updateUser(
        @Header("x-api-key") apiKey: String,
        @Path("useruuid") useruuid: String,
        @Body request: UpdateUserRequest
    ): Response<ResponseBody>
}