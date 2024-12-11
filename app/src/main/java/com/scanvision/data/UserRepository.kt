package com.scanvision.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.auth0.jwt.JWT
import com.scanvision.data.remote.response.ErrorResponse
import com.scanvision.data.remote.response.LoginRequest
import com.scanvision.data.remote.response.LoginResponse
import com.scanvision.data.remote.response.LoginData
import com.scanvision.data.remote.response.RegisterResponse
import com.scanvision.data.remote.response.RegisterRequest
import com.scanvision.data.remote.retrofit.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.util.*

class UserRepository(private val context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    private val apiKey = "Uhpy7TGejgwxMkp9iFX-VpA2OtphfUcO-YUd9GsjP2c" // Replace with your actual API key

    suspend fun login(email: String, password: String): LoginResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, password)
                Log.d("UserRepository", "LoginRequest: $request")
                val response = RetrofitInstance.userApi.login(apiKey, request)
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()?.string()
                    Log.d("UserRepository", "Response Body: $responseBody")
                    val loginResponse = Gson().fromJson(responseBody, LoginResponse::class.java)
                    Log.d("UserRepository", "Parsed LoginResponse: $loginResponse")
                    loginResponse?.data?.let {
                        saveUserSession(context, it)
                    } ?: throw Exception("LoginResponse or useruuid is null")
                    loginResponse
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("UserRepository", "Error Body: $errorBody")
                    Log.e("UserRepository", "Failed to login: ${response.code()} ${response.message()}")
                    throw Exception("Failed to login: ${response.code()} ${response.message()}")
                }
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                throw Exception(errorBody.message)
            }
        }
    }

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(email, name, password) // Corrected field order
                Log.d("UserRepository", "RegisterRequest: $request")
                val response = RetrofitInstance.userApi.register(apiKey, request)
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()?.string()
                    Log.d("UserRepository", "Response Body: $responseBody")
                    val registerResponse = Gson().fromJson(responseBody, RegisterResponse::class.java)
                    Log.d("UserRepository", "Parsed RegisterResponse: $registerResponse")
                    registerResponse
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("UserRepository", "Error Body: $errorBody")
                    Log.e("UserRepository", "Failed to register: ${response.code()} ${response.message()}")
                    throw Exception("Failed to register: ${response.code()} ${response.message()}")
                }
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                throw Exception(errorBody.message)
            }
        }
    }

    fun saveUserSession(context: Context, user: LoginData) {
        val sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("useruuid", user.useruuid)
        editor.putString("email", user.email)
        editor.putString("username", user.username)
        editor.apply()
    }

    fun getUserSession(context: Context): LoginData? {
        val sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val useruuid = sharedPreferences.getString("useruuid", null)
        val email = sharedPreferences.getString("email", null)
        val username = sharedPreferences.getString("username", "") ?: ""

        return if (useruuid != null && email != null) {
            LoginData(useruuid = useruuid, email = email, username = username, address = null, age = null, gender = null)
        } else null
    }

    fun clearSession() {
        sharedPref.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        val token = sharedPref.getString("token", null)
        return token != null && !isTokenExpired(token)
    }

    fun getToken(): String? {
        return sharedPref.getString("token", null)
    }

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val decodedJWT = JWT.decode(token)
            val expiryDate = decodedJWT.expiresAt
            val currentDate = Calendar.getInstance().time

            if (expiryDate == null) {
                val issueDateClaim = decodedJWT.getClaim("iat").asDate()
                val issueDate = issueDateClaim ?: currentDate
                val validUntil = Date(issueDate.time + 24 * 60 * 60 * 1000)
                return currentDate.after(validUntil)
            }
            expiryDate != null && currentDate.after(expiryDate)
        } catch (e: Exception) {
            true
        }
    }
}