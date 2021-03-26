package com.example.myapplication.common.management.calculate

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.common.MainActivityData
import com.example.myapplication.distancematrix.DistanceMatrixApi
import com.example.myapplication.distancematrix.ModelDistanceMatrix
import com.example.myapplication.distancematrixtwo.DistanceMatrix
import com.example.myapplication.distancematrixtwo.RowsMatrix
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlin.math.roundToInt

open class CalculateDistanceMatrixApi(
    val context: Context
) {

/*
    val arshanov = LatLng(53.402971, 91.083748)
    val chernogorskiy = LatLng(53.759367, 91.061604)
    val izyhskiy = LatLng(53.630114, 91.436063)
    val cirbinskiy = LatLng(53.529799, 91.410684)
*/

    var providersLocations = mutableMapOf(
        MainActivityData.Arschanovr to "53.402971, 91.083748",
        MainActivityData.Chernogorskiy to "53.759367, 91.061604",
        MainActivityData.Izyhskiy to "53.630114, 91.436063",
        MainActivityData.Cirbinskiy to "53.529799, 91.410684"
    )

    fun checkProvider(provider: String): String? {
        if (providersLocations.contains(provider)) {
            return providersLocations.getValue(provider)
        } else {
            return null
        }
    }

    fun calculateDistance(origin: String, destination: String): Single<RowsMatrix> {
        val disposable = DistanceMatrix().configureRetrofit()
            .getDistance(MainActivityData.keyMatrixApi, origin, destination)
        return disposable
    }
}

