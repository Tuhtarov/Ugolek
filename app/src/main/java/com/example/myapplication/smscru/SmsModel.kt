package com.example.myapplication.smscru

import com.google.gson.annotations.SerializedName
import retrofit2.http.Query

data class SmsModel(
    val error: String?,
    val error_code: Int?,
    val cnt: Int?,
    val id: Int
)
