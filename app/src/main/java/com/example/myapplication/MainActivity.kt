package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.example.myapplication.common.MainActivityData
import com.example.myapplication.common.management.*
import com.example.myapplication.common.management.progressBar.ProgressBarManage
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.distancematrixtwo.RowsMatrix
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.SingleObserver
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), FindLocationManagement, CalculateDistanceManagement,
    PriceOrderManagement {
    private lateinit var b: ActivityMainBinding
    private lateinit var management: MainActivityManagement
    private lateinit var disposableBagRxJava3: CompositeDisposable
    private lateinit var disposableBagRxJava2: io.reactivex.disposables.CompositeDisposable
    private lateinit var findLocation: FindLocation
    private lateinit var progressBarManage: ProgressBarManage

    fun Disposable.disposeAtTheEnd() {
        disposableBagRxJava3.add(this)
    }

    private fun Context.hideSoftBoard(view: View){
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

    }

    fun io.reactivex.disposables.Disposable.disposeAtTheEndRxJava2() {
        disposableBagRxJava2.add(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)

        findLocation = FindLocation(this)
        management = MainActivityManagement()
        progressBarManage = ProgressBarManage(b.progressBarMain, b.progressMainContainer)
        disposableBagRxJava3 = CompositeDisposable()
        disposableBagRxJava2 = io.reactivex.disposables.CompositeDisposable()

        setContentView(b.root)
        b.fieldMarkCoal.isEnabled = false
        progressBarManage.progressOff()

        b.fieldProvider.setOnClickListener {
            b.fieldMarkCoal.isEnabled = true
            cleanFieldData()
            showDialogProvider()
        }

        b.fieldMarkCoal.setOnClickListener {
            if (b.fieldProvider.text.isNotEmpty()) {
                showDialogMark()
            } else {
                showCustomToast("Выберите поставщика!", getString(R.string.symbol_cancel))
                b.fieldMarkCoal.isEnabled = false
            }
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
                        //TODO
                        calculateOrder()
                    } else {
                        b.fieldDelivery.text = ""
                        b.fieldAllPrice.text = ""
                    }
                } else {
                    b.fieldRequiredMass.text.clear()
                    showCustomToast(
                        "Только целочисленные значения от 1-40",
                        getString(R.string.symbol_not_equal)
                    )
                }
            }.disposeAtTheEnd()


        val intentMapsActivity = Intent(this, MapsActivity::class.java)
        b.btnChooseOnMap.setOnClickListener {
            if (checkValidatesFieldForMaps()) {
                intentMapsActivity.putExtra("provider", b.fieldProvider.text.toString())
                startActivityForResult(intentMapsActivity, 0)
            } else {
                showCustomToast("Заполните выделенные поля!", getString(R.string.symbol_pen_write))
            }
        }


        b.fieldPriceCoal.textChanges()
            .map { boolean ->
                (b.fieldDistance.text.toString()
                    .isEmpty() && b.fieldAddressDelivery.text.length > 10)
            }
            .subscribe {
                if (it) {
                    if (checkFieldForCalculate() && b.fieldAddressDelivery.text.toString() == addressString) {
                        calculateDistance(b.fieldProvider.text.toString(), addressGeolocation)
                    } else {
                        Log.d("TAG", "Не робит валидация")
                    }
                }
            }.disposeAtTheEnd()

        val intentConfirm = Intent(this, ConfirmActivity::class.java)
        b.btnToOrder.setOnClickListener {
            if (checkValidatesField()) {
                b.fieldAddressDelivery.setText(addressString)
                intentConfirm.putExtra("provider", b.fieldProvider.text.toString())
                intentConfirm.putExtra("coal", b.fieldMarkCoal.text.toString())
                intentConfirm.putExtra("priceCoal", b.fieldPriceCoal.text.toString())
                intentConfirm.putExtra("addressDelivery", b.fieldAddressDelivery.text.toString())
                intentConfirm.putExtra("requiredMass", b.fieldRequiredMass.text.toString())
                intentConfirm.putExtra("distance", b.fieldDistance.text.toString())
                intentConfirm.putExtra("priceDelivery", b.fieldDelivery.text.toString())
                intentConfirm.putExtra("allPrice", b.fieldAllPrice.text.toString())
                startActivity(intentConfirm)
            } else {
                showCustomToast("Заполните выделенные поля!", getString(R.string.symbol_pen_write))
            }
        }

        var addressFromUsersInputs = ""
        b.fieldAddressDelivery.textChanges()
            .map { text -> text.isNotEmpty() }
            .debounce(2000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ boolean ->
                if (boolean == true) {
                    if (b.fieldAddressDelivery.text.toString().contains("  ")) {
                        b.fieldAddressDelivery.setText(b.fieldAddressDelivery.text.toString().replace("  ", " "))
                        b.fieldAddressDelivery.setText(b.fieldAddressDelivery.text.toString().replace("  ", " "))
                        b.fieldAddressDelivery.setText(b.fieldAddressDelivery.text.toString().replace("  ", " "))
                        b.fieldAddressDelivery.setSelection(b.fieldAddressDelivery.text.length)
                    }
                    if(b.fieldAddressDelivery.text.length > 15){
                        if (addressFromUsersInputs != b.fieldAddressDelivery.text.toString()) {
                            if(addressString == b.fieldAddressDelivery.text.toString()){
                                if(providerName == b.fieldProvider.text.toString()){
                                    Log.e("fieldAddress", "на такой адрес уже свершился подсчёт")
                                } else {
                                    calculateDistance(b.fieldProvider.text.toString(), addressGeolocation)
                                    hideSoftBoard(b.root)
                                }
                            } else {
                                findLocation(this, b.fieldAddressDelivery.text.toString())
                                hideSoftBoard(b.root)
                            }
                        }
                        addressFromUsersInputs = b.fieldAddressDelivery.text.toString()
                    }
                }
            }, {


                Log.e("fieldAddress", it.localizedMessage)
            }).disposeAtTheEnd()
    }

    /*============================================================================================*/

    /* addressGeolocation - переменная, в которую помещается геоданные о выбранном адресе */
    var addressGeolocation: String = ""
    var addressString: String = ""
    var providerName: String = ""
    var distanceResult: String = ""

    //можно попробовать создать массив в который будет закладывается название поставщика как ключ и требуемый адресс доставки пользователем как значение этого ключа
    //потом когда возникает потребность подсчёта, сверить ключ, взять значения и воспользоваться ими

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
        dialogProvider.setOnDismissListener {
            if (b.fieldProvider.text.isEmpty()) {
                b.fieldMarkCoal.isEnabled = false
                showCustomToast("Поставщик не выбран!", getString(R.string.symbol_cancel))
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
                if (checkFieldForCalculate()) {
                    if (b.fieldDistance.text.toString().isNotEmpty()) {
                        calculateOrder()
                    } else {
                        findLocation(this, b.fieldAddressDelivery.text.toString())
                    }
                }
                dialogMark.dismiss()
            }
        }
    }

    private fun findLocation(context: Context, addressLine: String) {
        super.getLocationByString(context, addressLine)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ address ->
                b.fieldAddressDelivery.setText(address.getAddressLine(0).toString())
                addressString = b.fieldAddressDelivery.text.toString()
                addressGeolocation = "${address.latitude}, ${address.longitude}"
                Log.d("TAG", "Результат поиска локации -> ${address.getAddressLine(0)}")
                /* Подсчёт дистанции */
                calculateDistance(b.fieldProvider.text.toString(), addressGeolocation)
            }, {
                showCustomToast("Адрес не был найден.", getString(R.string.symbol_cancel))
                Log.e("TAG", "Адрес не был найден -> ${it.localizedMessage}")
            }).disposeAtTheEnd()
    }

    private fun calculateDistance(provider: String, address: String) {
        super.calculateDistance(this, provider, address)?.let {
            it
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<RowsMatrix> {
                    override fun onSubscribe(d: io.reactivex.disposables.Disposable) {
                        progressBarManage.progressOn()
                    }

                    override fun onSuccess(t: RowsMatrix) {
                        Log.d(
                            "TAG",
                            "Расстояние обнаружено, результат -> ${t.rows[0].elements[0].distance.text}"
                        )
                        progressBarManage.progressOff()
                        b.fieldDistance.text =
                            t.rows[0].elements[0].distance.text.replace("km", "км")
                        providerName = b.fieldProvider.text.toString()
                        distanceResult = b.fieldDistance.text.toString().also {
                            if (it.contains(",")) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Слишком далеко :В",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        calculateOrder()
                    }

                    override fun onError(e: Throwable) {
                        progressBarManage.progressOff()
                        Log.e(
                            "TAG",
                            " calculateTotalDistance(MainActivity) -> ${e.localizedMessage}"
                        )
                        showCustomToast(
                            "Возникла ошибка при подсчёте расстояния \n повторите попытку.",
                            getString(R.string.symbol_cancel)
                        )
                    }
                })
        }
    }

    private fun calculateOrder() {
        /* Подсчёт стоимости доставки */
        if (b.fieldDistance.text.toString().isNotEmpty() && b.fieldRequiredMass.text.toString()
                .isNotEmpty()
        ) {
            if (b.fieldDistance.text.contains(",")) {
                Log.e("TAG", "Слишком большое расстояние")
            } else {
                b.fieldDelivery.text = calculatePriceDelivery(
                    b.fieldDistance.text.toString(),
                    b.fieldRequiredMass.text.toString().toInt()
                )

                /* Подсчёт стоимости заказа */
                try {
                    b.fieldAllPrice.text = calculatePriceOrder(
                        b.fieldPriceCoal.text.toString().toInt(),
                        b.fieldRequiredMass.text.toString().toInt(),
                        b.fieldDelivery.text.toString().toFloat()
                    )
                    b.fieldAllPrice.append(" руб.")
                } catch (e: IOException) {
                    Log.e(
                        "calculate order",
                        "Возникла ошибка при подсчёте общей стоимости: \n ${e.localizedMessage} -- ${e.message}"
                    )
                }
            }
        }
    }

    private fun calculatePriceDelivery(distanceValue: String, requiredMass: Int): String {
        val distance = distanceValue.replace("км", "").trim().toFloat()
        return super.calculatePriceDelivery(distance, requiredMass)
    }

    override fun calculatePriceOrder(
        priceCoal: Int,
        requiredMass: Int,
        deliveryPrice: Float
    ): String {
        return super.calculatePriceOrder(priceCoal, requiredMass, deliveryPrice)
    }

    private fun cleanFieldData() {
        b.fieldProvider.text = ""
        b.fieldMarkCoal.text = ""
        b.fieldPriceCoal.text = ""
        b.fieldDistance.text = ""
        b.fieldDelivery.text = ""
        b.fieldAllPrice.text = ""
    }

    private fun checkFieldForCalculate(): Boolean {
        return (b.fieldProvider.text.toString().isNotEmpty()
                && b.fieldMarkCoal.text.toString().isNotEmpty()
                && b.fieldRequiredMass.text.toString().isNotEmpty()
                && b.fieldAddressDelivery.text.toString().isNotEmpty())
    }

    private fun checkValidatesField(): Boolean {
        return (b.fieldProvider.text.isNotEmpty() && b.fieldMarkCoal.text.isNotEmpty()
                && b.fieldRequiredMass.text.isNotEmpty() && b.fieldPriceCoal.text.isNotEmpty()
                && b.fieldAddressDelivery.text.isNotEmpty() && b.fieldDistance.text.isNotEmpty()
                && b.fieldDelivery.text.isNotEmpty() && b.fieldAllPrice.text.isNotEmpty())
    }

    private fun checkValidatesFieldForMaps(): Boolean {
        return (b.fieldProvider.text.isNotEmpty() && b.fieldMarkCoal.text.isNotEmpty() && b.fieldPriceCoal.text.isNotEmpty())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                data?.let {
                    b.fieldAddressDelivery.setText(data.getStringExtra("address").toString())
                    addressGeolocation = data.getStringExtra("addressGeolocation").toString()
                    b.fieldDistance.text = data.getStringExtra("distance")
                    b.fieldAddressDelivery.setSelection(b.fieldAddressDelivery.text.length)
                    addressString = data.getStringExtra("address").toString()
                    calculateOrder()
                }
            } else {
                showCustomToast(
                    "Не удалось подсчитать стоимость доставки. Попробуйте ещё раз.",
                    getString(R.string.symbol_cancel)
                )
            }
        }
    }

    private fun showCustomToast(text: String, symbol: String) {
        val view = layoutInflater.inflate(
            R.layout.custom_toast,
            findViewById(R.id.custom_toast_group)
        )
        val textT = view.findViewById<TextView>(R.id.text_custom_toast)
        val iconT = view.findViewById<TextView>(R.id.icon_custom_toast)
        textT.setText(text)
        iconT.setText(symbol)

        with(Toast(applicationContext)) {
            duration = Toast.LENGTH_SHORT
            setView(view)
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }

    override fun onBackPressed() {
        with(AlertDialog.Builder(this)) {
            setTitle("Подтверждение действия")
            setMessage("Вы точно хотите выйти?")
            setPositiveButton("Да") { _, _ ->
                super.onBackPressed()
            }
            setNegativeButton("Нет") { _, _ ->
            }
            setCancelable(true)
        }.create().show()
    }

    override fun onDestroy() {
        disposableBagRxJava3.dispose()
        disposableBagRxJava2.dispose()
        super.onDestroy()
    }

}