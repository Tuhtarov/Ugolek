package com.example.myapplication.common.management

import android.content.Context
import com.example.myapplication.common.management.calculate.CalculateDistanceMatrixApi
import com.example.myapplication.distancematrixtwo.RowsMatrix
import io.reactivex.Single

interface CalculateDistanceManagement {
    fun calculateDistance(
        context: Context,
        provider: String,
        destination: String
    ): Single<RowsMatrix>? {
        val calculateClass = CalculateDistanceMatrixApi(context)
        val providerAddress = calculateClass.checkProvider(provider)

        providerAddress?.let {
            return calculateClass.calculateDistance(it, destination)
        } ?: run {
            return null
        }

    }
}