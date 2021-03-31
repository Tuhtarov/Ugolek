package com.example.myapplication.smscru

import io.reactivex.Single
import retrofit2.http.POST
import retrofit2.http.Query

interface SmsApi {

    @POST("sys/send.php?")
    fun sendConfirmSms( @Query("login") login: String,
                        @Query("psw") psw: String,
                        @Query("phones") phones: String,
                        @Query("fmt") fmt: String,
                        @Query("mes") mes: String
//                        ,
//                        @Query("sender") sender: String

    ) : Single<SmsModel>

}