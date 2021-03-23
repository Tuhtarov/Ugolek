package com.example.myapplication.distancematrixtwo

import com.google.android.datatransport.runtime.Destination
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface MatrixApi {
    @GET("maps/api/distancematrix/json")
    fun getDistance(
        @Query("key") key: String,
        @Query("origins") origins: String,
        @Query("destinations") destinations: String
    ): io.reactivex.Single<RowsMatrix>
}