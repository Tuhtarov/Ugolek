package com.example.myapplication.smscru

import android.content.Context
import android.content.res.Resources
import com.example.myapplication.R
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.HTTP

class RetrofitSmsApi() {
    private val loginApi = "lflagmanl"
    private val passwordApi = "eujkmkexitdct[!"
    private val fmt = "3" //необходимо для получения ответа сервера в Json формате
//    private val all = "0" //содержимое ответа сервера (не полное)
    private fun configureRetrofit(): SmsApi{

        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okhttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://smsc.ru/")
            .client(okhttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val smsApi = retrofit.create(SmsApi::class.java)
        return smsApi
    }

    fun sendMessageApi(phone: String, message: String): Single<SmsModel>{
        configureRetrofit().also {
            return it.sendConfirmSms(login = loginApi,
            psw = passwordApi,
            phone, fmt, message)
        }
    }

    /* для проверки статуса отправленного сообщения */
//    fun checkMessageStatus(phone: String, id: String): Single<SmsStatusModel>{
//        configureRetrofit().also {
//            return it.checkSmsStatus(login = loginApi,
//            psw = passwordApi, phone, fmt, id, all)
//        }
//    }

}