package com.scanvision.data.remote.response

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