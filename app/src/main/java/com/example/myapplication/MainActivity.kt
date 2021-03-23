package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.example.myapplication.common.MainActivityData
import com.example.myapplication.common.management.FindLocation
import com.example.myapplication.common.management.FindLocationManagement
import com.example.myapplication.common.management.MainActivityManagement
import com.example.myapplication.common.management.calculate.CalculateDistanceManagement
import com.example.myapplication.common.management.calculate.PriceOrder
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.distancematrixtwo.RowsMatrix
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), FindLocationManagement, CalculateDistanceManagement {
    private lateinit var b: ActivityMainBinding
    private lateinit var management: MainActivityManagement
    private lateinit var disposableBag: CompositeDisposable
    private lateinit var findLocation: FindLocation

    fun Disposable.disposeAtTheEnd() {
        disposableBag.add(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)

        findLocation = FindLocation(this)
        management = MainActivityManagement()
        disposableBag = CompositeDisposable()

        setContentView(b.root)

        b.fieldProvider.setOnClickListener {
            cleanFieldData()
            showDialogProvider()
        }

        b.fieldMarkCoal.setOnClickListener {
            showDialogMark()
        }

        val massAndAddressIsValid: Observable<Boolean> =
            Observable.combineLatest(
                b.fieldProvider.textChanges(),
                b.fieldMarkCoal.textChanges()
            ) { p, m ->
                p.isNotEmpty() && m.isNotEmpty()
            }

        massAndAddressIsValid
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { bool ->
                if (bool) {
                    b.fieldRequiredMass.isEnabled = true
                    b.fieldAddressDelivery.isEnabled = true
                    b.fieldAddressDelivery.hint = getString(R.string.enterAddress)
                } else {
                    b.fieldRequiredMass.isEnabled = false
                    b.fieldAddressDelivery.isEnabled = false
                    b.fieldAddressDelivery.hint = "Заполните предыдущие поля"
                }
            }
            .disposeAtTheEnd()

        b.fieldRequiredMass.textChanges()
            .map { text -> text.toString() }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { text ->
                if (MainActivityData.listNumber.contains(text)) {
                    if (text != "") {
                        PriceOrder.calcTotalPriceCoal(
                            text.toInt(),
                            b.fieldPriceCoal.text.toString().toInt()
                        )
                    }
                } else {
                    b.fieldRequiredMass.text.clear()
                    Toast.makeText(
                        this,
                        "Только целочисленные значения от 1-40",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .disposeAtTheEnd()

        val intentMapsActivity = Intent(this, MapsActivity::class.java)
        b.btnChooseOnMap.setOnClickListener {
            massAndAddressIsValid.subscribe {
                if (it == true) {
                    intentMapsActivity.putExtra("provider", b.fieldProvider.text.toString())
                    startActivityForResult(intentMapsActivity, 0)
                } else {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                }
            }.disposeAtTheEnd()
        }

        b.fieldAddressDelivery.textChanges()
            .debounce(700, TimeUnit.MILLISECONDS)
            .map { text -> (text.length > 15) }
            .distinctUntilChanged()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ boolean ->
                if(boolean) b.btnSearchAddress.isEnabled = boolean else b.btnSearchAddress.isEnabled = boolean
            }
            .disposeAtTheEnd()

        b.btnSearchAddress.setOnClickListener {
            getLocationByString(this, b.fieldAddressDelivery.text.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Toast.makeText(this, "${it.latitude}, ${it.longitude}", Toast.LENGTH_SHORT).show()
                    b.fieldAddressDelivery.setText(it.getAddressLine(0).toString())

                    calculateDistance(this, b.fieldProvider.text.toString(), "${it.latitude}, ${it.longitude}")?.let { single ->
                        single.subscribeOn(io.reactivex.schedulers.Schedulers.io())
                            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                            .subscribe({
                                b.fieldDistance.text = it.rows.get(0).elements.get(0).distance.text


                            },{
                                Log.e("TAG","btnSearchAddress -> ${it.localizedMessage}")
                            })

                    }
                },{
                    Toast.makeText(this, "Адрес не был найден.", Toast.LENGTH_SHORT).show()
                    Log.e("TAG", "Адрес не был найден -> ${it.localizedMessage}")
                })
                .disposeAtTheEnd()
        }

    }

    /* addressGeolocation - переменная, в которую помещается геоданные о выбранном адресе */
    lateinit var addressGeolocation: String

    fun findDistance(){

    }

    override fun calculateDistance(
        context: Context,
        provider: String,
        destination: String
    ): io.reactivex.Single<RowsMatrix>? {
        return super.calculateDistance(context, provider, destination)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                data?.let {
                    val distance = data.getStringExtra("distance")
                    val address = data.getStringExtra("address")
                    addressGeolocation = data.getStringExtra("addressGeolocation").toString()
                    b.fieldAddressDelivery.setText(address)
                }
            } else {
                Toast.makeText(
                    this,
                    "Не удалось подсчитать стоимость доставки. Попробуйте ещё раз.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showDialogProvider() {
        val dialogProvider = Dialog(this)
        dialogProvider.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogProvider.setContentView(R.layout.dialog_provider)
        dialogProvider.show()

        listOf<Button>(
            dialogProvider.findViewById(R.id.btn_arschanov),
            dialogProvider.findViewById(R.id.btn_chernogorsk),
            dialogProvider.findViewById(R.id.btn_cirbinsciy),
            dialogProvider.findViewById(R.id.btn_izyskhiy)
        ).forEach { btn ->
            btn.setOnClickListener {
                b.fieldProvider.text = btn.text
                management.createListCoalForProvider(btn.text.toString())
                dialogProvider.dismiss()
            }
        }

    }

    private fun showDialogMark() {
        val dialogMark = Dialog(this)
        dialogMark.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogMark.setContentView(R.layout.dialog_mark)
        dialogMark.show()

        listOf<Button>(
            dialogMark.findViewById(R.id.btn_do_mark),
            dialogMark.findViewById(R.id.btn_dp_mark),
            dialogMark.findViewById(R.id.btn_dmsch_mark),
            dialogMark.findViewById(R.id.btn_dpk_mark)
        ).forEach { btn ->
            btn.isGone = !management.getListCoalForProvider().contains(btn.text.toString())
            btn.setOnClickListener {
                b.fieldMarkCoal.text = btn.text
                b.fieldPriceCoal.text =
                management.getPriceListOfCoal().getValue(btn.text.toString()).toString()
                dialogMark.dismiss()
            }
        }

    }

    override fun getLocationByString(context: Context, addressLine: String): Single<Address> {
        return super.getLocationByString(context, addressLine)
    }

    private fun cleanFieldData() {
        b.fieldProvider.text = ""
        b.fieldMarkCoal.text = ""
        b.fieldPriceCoal.text = ""
        b.fieldDistance.text = ""
        b.fieldDelivery.text = ""
        b.fieldAllPrice.text = ""
    }

    override fun onDestroy() {
        disposableBag.dispose()
        super.onDestroy()
    }
}