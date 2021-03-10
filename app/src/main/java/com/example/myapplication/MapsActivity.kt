package com.example.myapplication

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiConsumer
import io.reactivex.internal.operators.single.SingleObserveOn
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.dialog_inputs_address.*
import org.w3c.dom.Text
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.jvm.javaClass
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMapClickListener {
    private lateinit var mMap: GoogleMap
    private lateinit var markerr: Marker
    private lateinit var dialog: Dialog
    private lateinit var dialogCalculate: Dialog
    private val tag = MapsActivity::class.java.simpleName
    private var result = ""

    //переменная request для работы функций по предоставлению разрешений
    private val LOCATION_PERMISSION_REQUEST = 1

    //метод - получение доступа к местоположению
    private fun getLocationAccess() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }


    //получение разрешение на использование местоположения
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    return
                }
                mMap.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    this,
                    "Разрешите доступ к местоположению устройства",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    //TODO АДРЕСНАЯ СТРОКА
    private fun init() {
        Log.d(tag, "init: initialing")
        searchField.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.action == KeyEvent.KEYCODE_ENTER) {
                Log.d("TAG_ok", "button was pressed")
                if (searchField.text.toString() != "" && searchField.text.length >= 3) {
                    result = geoLocate(null)
                } else {
                    searchField.setText("")
                }
                checkFocus()
            }
            return@OnEditorActionListener false
        })

    }


    //TODO функция возвращающая адрессную строку, на основании входящих данных
    private fun geoLocate(p0: LatLng?): String {
        Log.d(tag, "geoLocate: geoLocating")
        var listAddress = mutableListOf<Address>()
        var address = String()
        var geocode = Geocoder(this)
        var result: String

        if (p0 != null) {
            //TODO поиск локации на основе данных lat/lng
            try {
                listAddress = geocode.getFromLocation(p0.latitude, p0.longitude, 1)
            } catch (e: IOException) {
                Log.e(tag, "geoLocate: getFromLocation = ${e.message}")
            }

        } else {
            //TODO здесь поиск локации по введённой стринге
            var searchString = field_inputAddressMap.text.toString()
            try {
                listAddress = geocode.getFromLocationName(searchString, 1)
            } catch (e: IOException) {
                Log.e(tag, "geoLocate: getFromLocationName = ${e.message}")
            }
        }

        if (listAddress.size > 0) {
            address = listAddress[0].getAddressLine(0)
            Log.d(tag, "geoLocate: address has successful found =  ${listAddress[0]}")
            if (p0 == null) {
                var forMoveCamera = listAddress[0]
                var latitude = forMoveCamera.latitude
                var longitude = forMoveCamera.longitude
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 15f))
            }
            result = address
        } else {
            result = "Адресс не был найден"
            Log.e(
                tag,
                "geoLocate: address has not found, array is null or empty = ${address.isNullOrEmpty()} "
            )
        }

        return result
    }


    lateinit var searchField: EditText
    lateinit var field: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        searchField = findViewById<EditText>(R.id.field_inputAddressMap)
        field = findViewById<TextView>(R.id.field_chosenAddress)
    }

    //функция, отправляющая результат о выбранном адрессе в мейн активити
    private fun sendResult() {
        if (result == "Адресс не был найден") {
            result = ""
        }
        val intentResult = Intent(this, MainActivity::class.java)
        intentResult.putExtra("sms", result)
        intentResult.putExtra("distance", distance[0].toString())
        setResult(RESULT_OK, intentResult)
        searchField.text.clear()
        field.text = ""
        finish()
    }

    //подсчёт дистанции между локацией поставщика и выбранной точкой на карте
    var distance: FloatArray = FloatArray(10)
    private fun calculateDistance(flag: Boolean, p0: LatLng) {
        if (flag){
            Location.distanceBetween(positionProvider.latitude, positionProvider.longitude, p0.latitude, p0.longitude, distance)
            val resultDistance = distance[0].toString()
            Toast.makeText(this, "$resultDistance", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(tag, "calculateDistance: provider is null or empty")
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationAccess()
        mMap.setOnMapClickListener(this)
        val btnOk = findViewById<Button>(R.id.btn_acceptAddress)
        val btnNo = findViewById<Button>(R.id.btn_cancelAddress)
        val btnCalc = findViewById<Button>(R.id.btn_calculateAddress)
        btnCalc.isVisible = false

        createProviderMarker()
        if(flagProvider){
            btnCalc.isVisible = true
        }

        btnOk.setOnClickListener {
            sendResult()
            mMap.clear()
        }

        btnNo.setOnClickListener {
            field.text = ""
            result = ""
            mMap.clear()
            createProviderMarker()
        }

        btnCalc.setOnClickListener {
            showDialogCalculate()
//            getDistance("53.730489"+","+"91.421181", "53.409444"+","+"91.061542")
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(53.723275, 91.432107), 10f))
        init()
    }

    private fun showConfirmDialog(p0: LatLng) {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_adress)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val fieldText = dialog.findViewById<TextView>(R.id.field_adressConfirm)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)

        result = geoLocate(p0)
        fieldText.text = result
        field.text = result

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnCancelListener {
            result = ""
            dialog.dismiss()
            Toast.makeText(this, "dismiss", Toast.LENGTH_SHORT).show()
        }

        btnConfirm.setOnClickListener {
            sendResult()
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun showDialogCalculate(){
        dialogCalculate = Dialog(this)
        dialogCalculate.setContentView(R.layout.dialog_calculate_distance)
        dialogCalculate.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnOkay = dialogCalculate.findViewById<Button>(R.id.btn_ok)
        val fieldResultDistance = dialogCalculate.findViewById<EditText>(R.id.field_outputDistance)

        if(distance.isNotEmpty()){
            fieldResultDistance.setText("${distance[0].toString()}")
        } else {
            Log.e(tag, "showDialogCalculate: результат подсчёта дистанции - нулевой" )
        }

        btnOkay.setOnClickListener {
            dialogCalculate.dismiss()
        }

        dialogCalculate.show()
    }

    //имплементированный к классу обработчик для нажатия по карте
    override fun onMapClick(p0: LatLng) {
        checkFocus()
        createProviderMarker()
        createMarkOfChoiceAddress(p0)
//        showConfirmDialog(p0)
//        calculateDistance(flagProvider, p0)
        val dispose = backgroundThread(p0)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val result = it[0].toString()
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            },{
                Toast.makeText(this, "Вообщем через жопу получилось, завтра доделать попытаюсь", Toast.LENGTH_SHORT).show()
            })
    }

    //
    private fun checkFocus() {
        if (searchField.isFocused) {
            searchField.clearFocus()
        }
    }

    private fun createMarkOfChoiceAddress(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        markerOptions.title("Выбранный адрес")
        markerr = mMap.addMarker(markerOptions)
    }

    private var flagProvider: Boolean = false
    lateinit var positionProvider: LatLng


    private fun createProviderMarker() {
        intent.getStringExtra("provider")?.let {
            mMap.clear()

            flagProvider = true
            val arshanov = LatLng(53.402971, 91.083748)
            val chernogorskiy = LatLng(53.759367, 91.061604)
            val izyhskiy = LatLng(53.630114, 91.436063)
            val cirbinskiy = LatLng(53.529799, 91.410684)
            when (it) {

                getString(R.string.arschanovr) -> {
                    mMap.addMarker(
                        MarkerOptions().position(arshanov).title(getString(R.string.arschanovr))
                    )
                    positionProvider = arshanov
                }
                getString(R.string.chernogorskiy) -> {
                    mMap.addMarker(
                        MarkerOptions().position(chernogorskiy)
                            .title(getString(R.string.chernogorskiy))
                    )
                    positionProvider = chernogorskiy
                }
                getString(R.string.izyhskiy) -> {
                    mMap.addMarker(
                        MarkerOptions().position(izyhskiy).title(getString(R.string.izyhskiy))
                    )
                    positionProvider = izyhskiy
                }
                getString(R.string.cirbinskiy) -> {
                    mMap.addMarker(
                        MarkerOptions().position(cirbinskiy).title(getString(R.string.cirbinskiy))
                    )
                    positionProvider = cirbinskiy
                }

                else -> Toast.makeText(this, "Не предвиденная ошибка", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            mMap.clear()
            flagProvider = false
            Toast.makeText(this, "Поставщик не выбран", Toast.LENGTH_SHORT).show()

        }
    }

    //TODO фоновая задача по поиску адреса, не тормозящая ui thread
    private fun backgroundThread(p0: LatLng): Single<MutableList<Address>> {
        return Single.create { subscriber ->
            val geocoder = Geocoder(this)
            val address = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
            subscriber.onSuccess(address)
        }
    }


}

