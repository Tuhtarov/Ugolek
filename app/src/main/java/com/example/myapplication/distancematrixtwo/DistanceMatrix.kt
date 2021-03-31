package com.example.myapplication.distancematrixtwo

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.logging.Handler
import java.util.logging.Level

open class DistanceMatrix {

    fun configureRetrofit(): MatrixApi {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient().newBuilder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient)
            .build()

        val matrixApi = retrofit.create(MatrixApi::class.java)
        return matrixApi
    }

//    fun calculateDistance(origin: String, destination: String) {
//        matrixApi.also {
//            it.getDistance(keyMatrixApi, origin, destination)
//                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : io.reactivex.SingleObserver<RowsMatrix>{
//                    override fun onSubscribe(d: io.reactivex.disposables.Disposable) {
//                        Log.e("Tag", "Успех")
//                    }
//
//                    override fun onSuccess(t: RowsMatrix) {
//                        Log.e("Tag", "результат -> ${t.rows.get(0).elements.get(0).distance.text}")
//                    }
//
//                    override fun onError(e: Throwable) {
//                        Log.e("Tag", "Ошибка случилась)) ${e.localizedMessage}")
//                    }
//
//                })
//        }
//    }
}
