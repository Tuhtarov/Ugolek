package com.example.myapplication

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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
import com.example.myapplication.common.management.FindLocation
import com.example.myapplication.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.distancematrix.DistanceMatrixApi
import com.example.myapplication.distancematrix.ModelDistanceMatrix
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.math.roundToInt

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMapClickListener {

    lateinit var mMap: GoogleMap
    private lateinit var markerr: Marker
    private lateinit var dialog: Dialog
    private lateinit var dialogCalculate: Dialog
    private lateinit var binding: ActivityMapsBinding
    private val tag = MapsActivity::class.java.simpleName
    private var distanceResult = ""
    var addressResult = ""
    lateinit var distanceMatrixApi: DistanceMatrixApi
    lateinit var findLocation: FindLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findLocation = FindLocation(this)

        //сборка ретрофита
        configureRetrofit()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun configureRetrofit(){
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
        distanceMatrixApi = retrofit.create(DistanceMatrixApi::class.java)
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
    private fun initAddressSearchUI() {
        Log.d(tag, "init: initialing")
        val searchField = binding.inputAddressMap
        searchField.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.action == KeyEvent.KEYCODE_ENTER) {

                if (searchField.text.toString() != "" && searchField.text.length >= 3){
                    findLocation.findAndSetAddressByString(searchField, mMap)
                } else searchField.setText("")
                cleanFocus()

            }
            return@OnEditorActionListener false
        })

    }

    override fun onMapClick(p0: LatLng) {
        cleanFocus()
        createProviderMarker()
        createMarkOfChoiceAddress(p0)
        showConfirmDialog(p0)

        val stringDestination = StringBuilder("${p0.latitude},${p0.longitude}").toString()
        calculateDistanceMatrixApi(stringDestination, p0)
    }

    private fun calculateDistanceMatrixApi(destination: String, p0: LatLng){
        providerGeolocationLatLng?.let {
            distanceMatrixApi.getDistance(getString(R.string.distance_matrix_key), providerGeolocationLatLng!!, destination)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<ModelDistanceMatrix> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onSuccess(t: ModelDistanceMatrix) {
                        if(t.rows[0].elements.get(0).status != "ZERO_RESULTS"){
                            val valueDistance = t.rows.get(0).elements.get(0).distance.value.toDouble()
                            val ONE_KM = 1000.00
                            (valueDistance / ONE_KM).roundToInt().toString().also {
                                distanceResult = StringBuilder(it).append(" км").toString().trim()
                            }
                        } else {
                            Toast.makeText(this@MapsActivity, "Ничего не найдено", Toast.LENGTH_SHORT).show()
                        }

                        if (distanceResult == ""){
                            Toast.makeText(
                                this@MapsActivity,
                                "Проверьте интернет соединение",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.e("TAG retrofit", "Возникла ошибка ${e.localizedMessage}")
                        Toast.makeText(this@MapsActivity, "Проверьте подключение к интернету.", Toast.LENGTH_LONG).show()
                    }
                })
        } ?: run {
            Toast.makeText(this, "Поставщик не был выбран", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener(this)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(53.723275, 91.432107), 10f))

        initAddressSearchUI()
        getLocationAccess()
        createProviderMarker()

        val btnAccept = findViewById<Button>(R.id.btn_acceptAddress)
        val btnCancel = findViewById<Button>(R.id.btn_cancelAddress)
        val btnDistance = findViewById<Button>(R.id.btn_calculateAddress).also{it.isVisible = false}

        if(flagProviderOfCoal){
            btnDistance.isVisible = true
        }

        btnAccept.setOnClickListener {
            addressResult = binding.fieldChosenAddress.text.toString()
            sendResult()
            mMap.clear()
        }

        btnCancel.setOnClickListener {
            binding.fieldChosenAddress.text = ""
            addressResult = ""
            mMap.clear()
            createProviderMarker()
        }

        btnDistance.setOnClickListener {
            showDialogCalculate()
        }
    }

    private fun showConfirmDialog(p0: LatLng) {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_adress)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val fieldText = dialog.findViewById<TextView>(R.id.fieldDIalogAddressConfirm)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnDialogConfirmAddress)
        val btnCancel = dialog.findViewById<Button>(R.id.btnDialogCancelAddress)

        //нахождение адреса по данным LatLng + присвоение этого адреса вьюхе
        findLocation.findAndSetAddressByLatLng(p0, fieldText, binding.fieldChosenAddress)

        btnCancel.setOnClickListener {
            fieldText.text = ""
            dialog.dismiss()
        }

        dialog.setOnCancelListener {
            fieldText.text = ""
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            addressResult = fieldText.text.toString()
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

        if(!distanceResult.isNullOrEmpty()){
            fieldResultDistance.setText(distanceResult.replace("km","км"))
        } else {
            Toast.makeText(this, "Отсутствует интернет соединение", Toast.LENGTH_LONG).show()
        }

        btnOkay.setOnClickListener {
            dialogCalculate.dismiss()
        }

        dialogCalculate.show()
    }

    private fun cleanFocus() {
        if (binding.inputAddressMap.isFocused) {
            binding.inputAddressMap.clearFocus()
        }
    }

    private fun createMarkOfChoiceAddress(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        markerOptions.title("Выбранный адрес")
        markerr = mMap.addMarker(markerOptions)
    }

    private var flagProviderOfCoal: Boolean = false
    private lateinit var positionProvider: LatLng
    private var providerGeolocationLatLng: String? = null

    private fun createProviderMarker() {
        intent.getStringExtra("provider")?.let {
            mMap.clear()

            flagProviderOfCoal = true
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
                    providerGeolocationLatLng = "53.402971, 91.083748"
                }
                getString(R.string.chernogorskiy) -> {
                    mMap.addMarker(
                        MarkerOptions().position(chernogorskiy)
                            .title(getString(R.string.chernogorskiy))
                    )
                    positionProvider = chernogorskiy
                    providerGeolocationLatLng = "53.759367, 91.061604"
                }
                getString(R.string.izyhskiy) -> {
                    mMap.addMarker(
                        MarkerOptions().position(izyhskiy).title(getString(R.string.izyhskiy))
                    )
                    positionProvider = izyhskiy
                    providerGeolocationLatLng = "53.630114, 91.436063"
                }
                getString(R.string.cirbinskiy) -> {
                    mMap.addMarker(
                        MarkerOptions().position(cirbinskiy).title(getString(R.string.cirbinskiy))
                    )
                    positionProvider = cirbinskiy
                    providerGeolocationLatLng = "53.529799, 91.410684"
                }

                else -> Toast.makeText(this, "Не предвиденная ошибка", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            mMap.clear()
            flagProviderOfCoal = false
            Toast.makeText(this, "Поставщик не выбран", Toast.LENGTH_SHORT).show()

        }
    }

    private fun sendResult() {
        val intentResult = Intent(this, MainActivity::class.java)

        addressResult = "Улица пушкина, дом калатушкина"
        //TODO ХАРДКОДИНГ

        if (addressResult == "Дырка от бублика") {
            setResult(RESULT_CANCELED)
        } else if (addressResult.isNotEmpty() && distanceResult.isNotEmpty()){
            intentResult.putExtra("address", addressResult)
            intentResult.putExtra("distance", distanceResult)
            setResult(RESULT_OK, intentResult)
        }
        finish()
    }

    override fun onDestroy() {
        /* Отложенное очищение композит бэга нужно, что бы композит бэк не очистился во время выполнения запроса на сервер(иначе результат попытается придти
        в удалённый контейнер), это может случится при плохом соединении с интернетом */
        binding.inputAddressMap.text.clear()
        binding.fieldChosenAddress.text = ""
        mMap.clear()

        Handler().postDelayed({
            findLocation.compositeDisposable.dispose()
            Log.e(tag, "composite bag is cleared = ${findLocation.compositeDisposable.isDisposed}")
        }, 25000)

        super.onDestroy()
    }

}

