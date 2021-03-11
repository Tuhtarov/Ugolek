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
import android.renderscript.ScriptGroup
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
import com.example.myapplication.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.dialog_inputs_address.*
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMapClickListener {
    private lateinit var mMap: GoogleMap
    private lateinit var markerr: Marker
    private lateinit var dialog: Dialog
    private lateinit var dialogCalculate: Dialog
    private lateinit var binding: ActivityMapsBinding
    private val tag = MapsActivity::class.java.simpleName
    private var addressResult = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private val LOCATION_PERMISSION_REQUEST = 1
    //получение доступа к местоположению
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
        val searchField = binding.inputAddressMap
        searchField.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.action == KeyEvent.KEYCODE_ENTER) {
                Log.d("TAG_ok", "button was pressed")
                if (searchField.text.toString() != "" && searchField.text.length >= 3) {

                    val dispose = findGeolocationFromName(searchField.text.toString())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
                        },{
                            Toast.makeText(this, "Адрес не был найден", Toast.LENGTH_SHORT).show()
                        })

                } else {
                    searchField.setText("")
                }
                checkFocus()
            }
            return@OnEditorActionListener false
        })

    }

    //отправляет выбранный адрес, дистанцию между поставщиком и адресом, в MainActivity
    private fun sendResult() {
        val intentResult = Intent(this, MainActivity::class.java)
        if (addressResult == "Адрес не был найден") { addressResult = "" }
        if (addressResult != ""){ intentResult.putExtra("distance", distance[0].toString()) }

        intentResult.putExtra("sms", addressResult)
        setResult(RESULT_OK, intentResult)
        binding.inputAddressMap.text.clear()
        binding.fieldChosenAddress.text = ""
        finish()
    }

    //подсчёт дистанции между локацией поставщика и выбранной точкой на карте
    var distance: FloatArray = FloatArray(10)
    private fun calculateDistance(flag: Boolean, p0: LatLng) {
        if (flag){
            Location.distanceBetween(positionProvider.latitude, positionProvider.longitude, p0.latitude, p0.longitude, distance)
            val resultDistance = distance[0].toString()
            Toast.makeText(this, resultDistance, Toast.LENGTH_SHORT).show()
        } else {
            Log.e(tag, "calculateDistance: provider is null or empty")
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationAccess()
        mMap.setOnMapClickListener(this)
        val btnAccept = findViewById<Button>(R.id.btn_acceptAddress)
        val btnCancel = findViewById<Button>(R.id.btn_cancelAddress)
        val btnDistance = findViewById<Button>(R.id.btn_calculateAddress)
        btnDistance.isVisible = false

        createProviderMarker()
        if(flagProvider){
            btnDistance.isVisible = true
        }

        btnAccept.setOnClickListener {
            sendResult()
            mMap.clear()
        }

        btnCancel.setOnClickListener {
            binding.fieldChosenAddress.text = ""
            addressResult = ""
            distance = FloatArray(10)
            mMap.clear()
            createProviderMarker()
        }

        btnDistance.setOnClickListener {
            showDialogCalculate()
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(53.723275, 91.432107), 10f))
        init()
    }

    private fun showConfirmDialog(p0: LatLng) {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_adress)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val fieldText = dialog.findViewById<TextView>(R.id.fieldDIalogAddressConfirm)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnDialogConfirmAddress)
        val btnCancel = dialog.findViewById<Button>(R.id.btnDialogCancelAddress)

        val dispose = findGeolocation(p0)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                addressResult = it
                fieldText.setText(addressResult)
                binding.fieldChosenAddress.text = addressResult
            },{
                Toast.makeText(this, "Адрес не был найден", Toast.LENGTH_SHORT).show()
                fieldText.text = "Адрес не найден"
                addressResult = "Адрес не был найден"
            })

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnCancelListener {
            dialog.dismiss()
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
            fieldResultDistance.setText(distance[0].toString())
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
        showConfirmDialog(p0)
        calculateDistance(flagProvider, p0)
    }

    private fun checkFocus() {
        if (binding.inputAddressMap.isFocused) {
            binding.inputAddressMap.clearFocus()
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

    //TODO поиск адреса на фоновом потоке
    private fun findGeolocation(p0: LatLng): Single<String> {
        return Single.create { subscriber ->
            val geocoder = Geocoder(this)
            try {
                val addressList = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
                if (addressList.size > 0){
                    subscriber.onSuccess(addressList[0].getAddressLine(0))
                } else{
                    Log.e(tag, "findGeolocation: адрес не был найден")
                }
            } catch (e: IOException){
                Log.e(tag, "backgroundThread: ${e.localizedMessage}")
            }
        }
    }

    //TODO поиск адреса на фоновом потоке на основании строки, возращает массив с адресом
    private fun findGeolocationFromName(s: String?):Single<Address>{
        return Single.create {
            if (s != null){
                val geocoder = Geocoder(this)
                try {
                    val addressList = geocoder.getFromLocationName(s, 1)
                    if(addressList.size > 0){
                        it.onSuccess(addressList[0])
                    } else {
                        Log.e(tag, "findGeolocationFromName: адрес не был найден")
                    }
                } catch (e: IOException){
                    Log.e(tag, "findGeolocationFromName: ${e.localizedMessage}")
                }
            } else {
                Log.e(tag,"findGeolocationFromName: в аргумент функции пришла пустая строка")
            }
        }
    }

}

