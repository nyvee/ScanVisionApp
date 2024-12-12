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

data class RegisterResponse(
    val message: String,
    val status: String,
    val data: RegisterData?
)

data class RegisterData(
    val userId: String,
    val username: String
)

data class RegisterRequest(
    val email: String,
    val name: String,
    val password_hash: String
)

data class ProfileImageResponse(
    val url: String,
    val status: String,
    val message: String
)

data class UpdateImageRequest(
    val image: String
)

data class UpdateUserRequest(
    val address: String?,
    val age: Int?,
    val gender: String?
)