package com.example.myapplication.smscru

import com.google.gson.annotations.SerializedName
import retrofit2.http.Query

data class SmsModel(
//    "error": "message is denied",
//"error_code": 6,
//"id": 2164
    val error: String,
    val error_code: Int,
    val id: Int
)
