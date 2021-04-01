package com.example.myapplication.smscru

import io.reactivex.Single
import retrofit2.http.POST
import retrofit2.http.Query

interface SmsApi {

    @POST("sys/send.php?")
    fun sendConfirmSms(
        @Query("login") login: String,
        @Query("psw") psw: String,
        @Query("phones") phones: String,
        @Query("fmt") fmt: String,
        @Query("mes") mes: String
    ): Single<SmsModel>
//
//    @POST("sys/status.php?")
//    fun checkSmsStatus(
//        @Query("login") login: String,
//        @Query("psw") psw: String,
//        @Query("phones") phones: String,
//        @Query("fmt") fmt: String,
//        @Query("all") all: String,
//        @Query("id") id: String
//    ) : Single<SmsStatusModel>

}