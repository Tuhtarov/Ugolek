package com.example.myapplication.distancematrix

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface DistanceMatrixApi {
    @GET("maps/api/distancematrix/json")
    fun getDistance(@Query("key") key: String,
                    @Query("destinations") destination: String,
                    @Query("origins") origins: String): Single<ModelDistanceMatrix>
}