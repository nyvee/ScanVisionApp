package com.scanvision.data

data class UserRequest(
    val name: String,
    val email: String,
    val password_hash: String
)
