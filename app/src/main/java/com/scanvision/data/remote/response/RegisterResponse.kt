package com.scanvision.data.remote.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    val data: UserData,
    val message: String,
    val status: String
)

data class UserData(
    val userId: String,
    val username: String
)
data class ErrorResponse(
    @field:SerializedName("error")
    val error: Boolean? = null,
    @field:SerializedName("message")
    val message: String? = null
)