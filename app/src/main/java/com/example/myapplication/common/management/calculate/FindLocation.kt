package com.example.myapplication.common.management

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.example.myapplication.MapsActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.IOException

open class FindLocation(private val cntxt: Context?){

    val compositeDisposable: CompositeDisposable = CompositeDisposable()

    open fun findAndSetAddressByLatLng(latLng: LatLng, view: TextView, view2: TextView?){
        this.cntxt?.let {
            getLocationByLatLng(latLng, it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.text = it
                    view2?.text = it
                },{
                    MapsActivity().addressResult = "Дырка от бублика"
                    Log.e("FindLocation", " -> ${it.localizedMessage}")
                })
                .disposeAtTheEnd()
        }
    }

    open fun findAndSetAddressByString(editText: EditText, map: GoogleMap?){
        getLocationByText(editText.text.toString())
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude),15f))
                editText.setText(it.getAddressLine(0).toString())
            },{
                Log.e("FindLocation", " -> ${it.localizedMessage}")
            })
            .disposeAtTheEnd()
    }

    private fun getLocationByLatLng(latLng: LatLng, context: Context): Single<String> {
        return Single.create {
            try {
                val geocoder = Geocoder(this.cntxt)
                val addressList: List<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addressList.isNotEmpty()){
                    it.onSuccess(addressList[0].getAddressLine(0))
                }
            } catch (e: IOException) {
                it.onError(e)
            }
        }
    }

   private fun getLocationByText(string: String): Single<Address>{
       return Single.create{
           try {
               val geocoder = Geocoder(this.cntxt)
               val addressList: List<Address> =
                   geocoder.getFromLocationName(string, 1)
               if (addressList.isNotEmpty()){
                   it.onSuccess(addressList[0])
               }
           } catch (e: IOException){
               it.onError(e)
           }
       }
   }

    fun getLocationByTextString(context: Context, string: String): Single<Address>{
        return Single.create{ single ->
            try {
                val geocoder = Geocoder(context)
                val addressList: List<Address> =
                    geocoder.getFromLocationName(string, 1)
                if (addressList.isNotEmpty()){
                    single.onSuccess(addressList[0])
                    Log.d("TAG", "FindLocation: адрес найден")
                }
            } catch (e: IOException){
                single.onError(e)
            }
        }
    }

    fun findLatLngByString(addressLine: String){
        val geocoder = Geocoder(cntxt)
        try {
            val address = geocoder.getFromLocationName(addressLine, 1)
            if(address.size > 0){
                val latLngString = "${address[0].latitude}, ${address[0].longitude}"
                Log.e("GEOCODER", "Данные по LatLng -> $latLngString")
            } else {
                Log.e("GEOCODER", "Ничего не найдено, возможно криворукий ввод строки")
            }
        } catch (e: IOException){
            Log.e("GEOCODER", "ошибка ${e.localizedMessage}")
        }
    }

    private fun Disposable.disposeAtTheEnd(){
        compositeDisposable.add(this)
    }
}