package com.example.myapplication.common.management

import android.content.Context
import android.location.Address
import io.reactivex.rxjava3.core.Single

interface FindLocationManagement{
    fun getLocationByString(context: Context, addressLine: String): Single<Address> {
        return FindLocation(null).getLocationByTextString(context, addressLine)
    }
}