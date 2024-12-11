package com.scanvision.data.remote.response

data class LoginResponse(
    val status: String,
    val message: String,
    val data: LoginData?
)

data class LoginData(
    val address: String?,
    val age: Int?,
    val email: String,
    val gender: String?,
    val username: String,
    val useruuid: String
)

data class LoginRequest(
    val email: String,
    val password_hash: String
)
