package com.example.myapplication

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.ActivityChooserView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnMarkerClickListener, GoogleMap.OnMapClickListener{

    private lateinit var mMap: GoogleMap
    private lateinit var markerr: Marker
    private lateinit var dialog: Dialog
    val intentProviderFromMainActivity: Intent? = null

    //переменная request для работы функций по предоставлению разрешений
    private val LOCATION_PERMISSION_REQUEST = 1

    //метод - получение доступа к местоположению
    private fun getLocationAccess(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray){
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
                Toast.makeText(this, "Разрешите доступ к местоположению устройства", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationAccess()

        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)

        createProviderMarker()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(53.723275, 91.432107),10f))

    }


    private fun createProviderMarker(){
            intent.getStringExtra("provider")?.let {
            mMap.clear()

            val arshanov = LatLng(53.402971, 91.083748)
            val chernogorskiy = LatLng(53.759367, 91.061604)
            val izyhskiy = LatLng(53.630114, 91.436063)
            val cirbinskiy = LatLng(53.529799, 91.410684)

            when (it) {

                "arshanov" -> mMap.addMarker(
                    MarkerOptions().position(arshanov).title("Аршановский разрез")
                )
                "chernogorskiy" -> mMap.addMarker(
                    MarkerOptions().position(chernogorskiy).title("Черногорский разрез")
                )
                "izyhskiy" -> mMap.addMarker(
                    MarkerOptions().position(izyhskiy).title("Изыхский разрез")
                )
                "cirbinskiy" -> mMap.addMarker(
                    MarkerOptions().position(cirbinskiy).title("Кирбинский разрез")
                )

                else -> finish()
            }
        } ?: run {
                mMap.clear()
            Toast.makeText(this, "Нулевое значение", Toast.LENGTH_SHORT).show()
        }

//        if (intent.getStringExtra("provider") != null) {
//            val arshanov = LatLng(53.402971, 91.083748)
//            val chernogorskiy = LatLng(53.759367, 91.061604)
//            val izyhskiy = LatLng(53.630114, 91.436063)
//            val cirbinskiy = LatLng(53.529799, 91.410684)
//
//            when (intent.getStringExtra("provider")) {
//
//                "arshanov" -> mMap.addMarker(
//                    MarkerOptions().position(arshanov).title("Аршановский разрез")
//                )
//                "chernogorskiy" -> mMap.addMarker(
//                    MarkerOptions().position(chernogorskiy).title("Черногорский разрез")
//                )
//                "izyhskiy" -> mMap.addMarker(
//                    MarkerOptions().position(izyhskiy).title("Изыхский разрез")
//                )
//                "cirbinskiy" -> mMap.addMarker(
//                    MarkerOptions().position(cirbinskiy).title("Кирбинский разрез")
//                )
//
//                else -> finish()
//            }
//        } else {
//            return
//        }

    }

    private fun showConfirmDialog(p0: LatLng){
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_adress)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val str = "${p0.toString()}"
        val intentResult = Intent(this, MainActivity::class.java)
        val fieldText = dialog.findViewById<TextView>(R.id.field_adressConfirm)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)
        fieldText.text = "$p0"

        dialog.show()
        dialog.setOnCancelListener { showToast("Нажатием на карту выберите адресс -> Нажмите кнопку \"Подтвердить\"") }

        //обработчик кнопки ПОДТВЕРДИТЬ
        btnConfirm.setOnClickListener {
            createProviderMarker()

            //передача требуемого результата в MainActivity
            intentResult.putExtra("sms", str)
            setResult(RESULT_OK, intentResult)
            dialog.dismiss()
            finish()
        }
    }

    //имплементированный к классу обработчик для любого нажатого маркера
    override fun onMarkerClick(p0: Marker): Boolean {
        Toast.makeText(this, "Was pressed on Location ->" + p0.position, Toast.LENGTH_SHORT).show()
        return false
    }

    //имплементированный к классу обработчик для нажатия по карте
    override fun onMapClick(p0: LatLng) {
        showConfirmDialog(p0)
        Toast.makeText(this, "Latitude-> ${p0.latitude}\nLongitude-> ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }


    private fun createMarkOfChoiceAddress(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        markerOptions.title("Выбранный адрес -> ${location.longitude}\n${location.longitude}")
        markerr = mMap.addMarker(markerOptions)
    }



    //создать и показать тост на экране))0)00
    private fun showToast(string: String){
        Toast.makeText(this, "$string", Toast.LENGTH_SHORT).show()
    }


}