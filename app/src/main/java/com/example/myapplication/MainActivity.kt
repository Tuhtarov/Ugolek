
package com.example.myapplication

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.example.myapplication.common.MainActivityData
import com.example.myapplication.databinding.ActivityMainBinding
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.Observables
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var intentForMapsLayout: Intent
    private var providerName: String? = null
    private val REQUEST_CODE = 0
    private var distanceBetweenTwoPoints: String? = null
    private lateinit var dialog: Dialog
    private lateinit var b: ActivityMainBinding
    lateinit var disposable: CompositeDisposable

    private var do25 = ""
    private var dp = ""
    private var dpk = ""
    private var dmsch = ""
    private var listMarkCoal = mutableListOf<String>()
    private var listPriceCoal = mutableMapOf<String, Int>()
    var accessMark: String? = null

    private var totalPriceCoal: Int? = null
    private var totalPriceDelivery: Int? = null
    private var totalPrice: Int? = null

    private var err: String = "JavaNullPointerException"
    private val TAG = MainActivity::class.java.simpleName

    fun Disposable.disposeAtTheEnd(){
        disposable.add(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        disposable = CompositeDisposable()
        intentForMapsLayout = Intent(this, MapsActivity::class.java)

        b.fieldProvider.setOnClickListener {
            cleanDataFields()
            showProviderDialog()
        }

        b.fieldMarkCoal.setOnClickListener {
            b.fieldProvider.text.toString().also {
                if (it != "") {
                    providerName = it
                    showMarkDialog()
                } else {
                    Toast.makeText(
                        this,
                        "Для выбора марки угля необходимо изначально выбрать поставщика",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        //боже как я обожаю rxjava
        val requiredMassIsValid: Observable<Boolean> =
            Observables.combineLatest(
                b.fieldProvider.textChanges(),
                b.fieldMarkCoal.textChanges()){
                p, m -> p.isNotEmpty() && m.isNotEmpty()
            }

        requiredMassIsValid
            .subscribe {
                if (it) {
                    b.fieldRequiredMass.isEnabled = true
                    b.fieldAddressDelivery.isEnabled = true
                    b.fieldAddressDelivery.hint = "Введите адрес"
                } else {
                    b.fieldRequiredMass.isEnabled = false
                    b.fieldAddressDelivery.isEnabled = false
                    b.fieldAddressDelivery.hint = "Выберите поставщика и марку угля"
                }
            }
            .disposeAtTheEnd()


        b.fieldRequiredMass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!MainActivityData.getListOfNumber().contains(b.fieldRequiredMass.text.toString())) {
                    b.fieldRequiredMass.text.clear()
                    Toast.makeText(applicationContext,"Только целочисленные значения от 1-40", Toast.LENGTH_SHORT).show()
                }
            }
            override fun afterTextChanged(s: Editable?) {
                val requiredMass = b.fieldRequiredMass
                if (!MainActivityData.getListOfNumber().contains(requiredMass.text.toString())) {
                    requiredMass.text.clear()
                } else if (!requiredMass.text.isNullOrEmpty()){
                    if (!b.fieldAddressDelivery.text.isNullOrEmpty()){
                        calculateTotalPriceCoal(requiredMass.text.toString().toInt())
                        calculatePriceDelivery()
                        calculateAllPrice()
                    }
                } else {
                    Log.d(TAG, "afterTextChanged: количество угля ещё не определено")
                }
                if(requiredMass.text.isNullOrEmpty()){
                    b.fieldDelivery.text = ""
                    b.fieldDistance.text = ""
                    b.fieldAllPrice.text = ""
                }
            }
        })

        b.btnChooseOnMap.setOnClickListener {
            //checkFillingFields()
            intentForMapsLayout.putExtra("provider", providerName)
            startActivityForResult(intentForMapsLayout, REQUEST_CODE)
        }

        b.btnToOrder.setOnClickListener {
            if ((b.fieldProvider.text.toString() != "") and (b.fieldMarkCoal.text.toString() != "")
                and (b.fieldRequiredMass.text.toString() != "") and (b.fieldAddressDelivery.text.toString() != "")) {
                createSecondLayout()
            } else {
                Toast.makeText(this, "Заполните недостающие поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //очистка полей от старых значений
    private fun cleanDataFields(){
        b.fieldProvider.text = ""
        b.fieldMarkCoal.text = ""
        b.fieldPriceCoal.text = ""
        b.fieldDelivery.text = ""
        b.fieldDistance.text = ""
        b.fieldAllPrice.text = ""
        if(b.fieldRequiredMass.text.toString() != ""){ b.fieldRequiredMass.text.clear() }
        b.fieldAddressDelivery.setText("")
        accessMark = null
        providerName = null
        distanceBetweenTwoPoints = null
    }

    //проверка заполнения полей перед тем, как открыть карту
    private fun checkFillingFields(){
//                    if ((textView3.text.toString() == "") and (textView4.text.toString() == "") and (editTextNumber2.text.toString() == "")){
//                createToast("Заполните поля: Поставщик, Марка угля, Требуемая масса")
//            } else if ((textView3.text.toString() != "") and (textView4.text.toString() == "") and (editTextNumber2.text.toString() == "")){
//                createToast("Заполните поля: Марка угля, Требуемая масса")
//            } else if ((textView3.text.toString() != "") and (textView4.text.toString() != "") and (editTextNumber2.text.toString() == "")){
//                createToast("Заполните поля: Требуемая масса")
//            } else {
//            }
    }

    private fun createSecondLayout() {
        Intent(this, ConfirmActivity::class.java).also {
            it.putExtra("provider", b.fieldProvider.text.toString())
            it.putExtra("coal", b.fieldMarkCoal.text.toString())
            it.putExtra("priceCoal", b.fieldPriceCoal.text.toString())
            it.putExtra("addressDelivery", b.fieldAddressDelivery.text.toString())
            it.putExtra("requiredMass", b.fieldRequiredMass.text.toString())
            it.putExtra("distance", b.fieldDistance.text.toString())
            it.putExtra("priceDelivery", b.fieldDelivery.text.toString())
            it.putExtra("allPrice", b.fieldAllPrice.text.toString())
            startActivity(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE){
            if(resultCode == RESULT_OK){
                data?.let {
                    val address = data.getStringExtra("address")
                    val distance = data.getStringExtra("distance")

                    b.fieldAddressDelivery.setText(address)
                    b.fieldDistance.text = distance!!

                    calculatePriceDelivery()
                    calculateAllPrice()
                }
            }
        }
    }

    //модалка для выбора поставщика угля
    private fun showProviderDialog() {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_provider)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val btnArshanov = dialog.findViewById<Button>(R.id.btn_arschanov)
        val btnChernogorsk = dialog.findViewById<Button>(R.id.btn_chernogorsk)
        val btnCirbinsciy = dialog.findViewById<Button>(R.id.btn_cirbinsciy)
        val btnIzyshkiy = dialog.findViewById<Button>(R.id.btn_izyskhiy)
        dialog.show()

        val listBtnProvider = listOf<Button>(btnArshanov, btnChernogorsk, btnCirbinsciy, btnIzyshkiy)
        listBtnProvider.forEach { button ->
            button.setOnClickListener {
                onClickBtn(it)
                //здесь определяется (determine) список угля и цен на них для выбранного поставщика
                determineListCoalForProvider(b.fieldProvider.text.toString())
                dialog.dismiss()
            }
        }
    }

    //модалка для выбора сорта угля
    private fun showMarkDialog() {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_mark)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val btnDMSCH = dialog.findViewById<Button>(R.id.btn_dmsch_mark)
        val btnDO = dialog.findViewById<Button>(R.id.btn_do_mark)
        val btnDP = dialog.findViewById<Button>(R.id.btn_dp_mark)
        val btnDPK = dialog.findViewById<Button>(R.id.btn_dpk_mark)

        //создание списка для сокрытия кнопок (марок угля), несоответствующих поставщику угля
        val listBtnMark = listOf<Button>(btnDMSCH, btnDO, btnDP, btnDPK)

        for (button in listBtnMark) {
            button.text.toString().also { textB ->
                button.isGone = !listMarkCoal.contains(textB)
            }
        }

        listBtnMark.forEach { button ->
            button.setOnClickListener {
                onClickBtn(it)
                //здесь вызывается ф-я для определения цены за выбранную марку угля
                countPriceCoal(button.text.toString())
                if (b.fieldRequiredMass.text.toString() != "") {
                    calculateTotalPriceCoal(b.fieldRequiredMass.text.toString().toInt())
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    //здесь определяется список угля и цен на них для выбранного поставщика
    private fun determineListCoalForProvider(providerName: String) {
        listPriceCoal.clear()
        when (providerName) {
            getString(R.string.cirbinskiy) -> {
                //я тебя вижу друг
                //лист цен для кирбинского разреза
                listPriceCoal[dmsch] = 1495
                listPriceCoal[do25] = 1705
                listPriceCoal[dp] = 1205
            }
            getString(R.string.chernogorskiy) -> {
                //лист цен для черногорского разреза
                listPriceCoal[dpk] = 1795
                listPriceCoal[dp] = 1205
            }
            getString(R.string.izyhskiy) -> {
                //лист цен для изыхского разреза
                listPriceCoal[do25] = 1705
                listPriceCoal[dpk] = 1905
                listPriceCoal[dp] = 1195
            }
            getString(R.string.arschanovr) -> {
                //лист цен для аршановского разреза
                listPriceCoal[dmsch] = 1495
                listPriceCoal[do25] = 1700
                listPriceCoal[dpk] = 1905
                listPriceCoal[dp] = 1195
            }
            else -> Toast.makeText(this, err + "299", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onClickBtn(button: View) {
        when (button.id) {
            //для диалога "выбор поставщика"
            R.id.btn_izyskhiy -> setTextProvider(getString(R.string.izyhskiy))
            R.id.btn_chernogorsk -> setTextProvider(getString(R.string.chernogorskiy))
            R.id.btn_cirbinsciy -> setTextProvider(getString(R.string.cirbinskiy))
            R.id.btn_arschanov -> setTextProvider(getString(R.string.arschanovr))

            //для диалога "выбор марки угля"
            R.id.btn_dpk_mark -> setTextMark(dpk)
            R.id.btn_dp_mark -> setTextMark(dp)
            R.id.btn_do_mark -> setTextMark(do25)
            R.id.btn_dmsch_mark -> setTextMark(dmsch)
            else -> Toast.makeText(this, err + "229", Toast.LENGTH_LONG).show()
        }
    }

    private fun setTextMark(markOfCoal: String) {
        b.fieldMarkCoal.text = ""
        b.fieldMarkCoal.append(markOfCoal)
    }

    //для присвоения имени выбранного поставщика в поле "поставщик"
    private fun setTextProvider(nameProvider: String) {
        b.fieldProvider.text = ""
        //переменным присваиваются названия марок угля, определенные в строковых ресурсах (необходимо для создания списка угля соответствующего поставщика) v1.2.5
        do25 = getString(R.string.do25)
        dp = getString(R.string.dp)
        dpk = getString(R.string.dpk)
        dmsch = getString(R.string.dmsch)

        b.fieldProvider.append(nameProvider).also {
            when (b.fieldProvider.text.toString()) {
                //обработчики для диалога "выбор поставщика"
                getString(R.string.izyhskiy) -> listMarkCoal = mutableListOf(do25, dp, dpk)
                getString(R.string.chernogorskiy) -> listMarkCoal = mutableListOf(dpk, dp)
                getString(R.string.cirbinskiy) -> listMarkCoal = mutableListOf(do25, dmsch, dp)
                getString(R.string.arschanovr) -> listMarkCoal = mutableListOf(do25, dp, dpk, dmsch)
                else -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateTotalPriceCoal(quantity: Int) {
        if (accessMark != null || quantity != null) {
            totalPriceCoal = quantity * accessMark.toString().toInt()
        } else {
            Log.d(TAG, "calculateTotalPriceCoal: стоимость марки ещё не определена")
        }
    }

    private fun calculatePriceDelivery(){
        var priceQuantityCoal: Int? = null
        val quantityCoal = b.fieldRequiredMass.text.toString()
        distanceBetweenTwoPoints = b.fieldDistance.text.toString().replace("км","").trim()

            when (quantityCoal.toInt()) {
                in 1..3 -> {
                    priceQuantityCoal = 10
                }
                in 4..7 -> {
                    priceQuantityCoal = 15
                }
                in 8..20 -> {
                    priceQuantityCoal = 35
                }
                in 21..40 -> {
                    priceQuantityCoal = 80
                }
                else -> {
                    Log.e(TAG, "err 195")
                }
            }

        priceQuantityCoal?.let{
            totalPriceDelivery = it * distanceBetweenTwoPoints!!.toInt()
            b.fieldDelivery.text = totalPriceDelivery!!.toString()
            b.fieldDelivery.append(" руб.")
        } ?: run {
            Toast.makeText(this@MainActivity, "Выберите адрес доставки", Toast.LENGTH_SHORT).show()
        }
    }



    private fun calculateAllPrice(){
        totalPrice = totalPriceCoal?.plus(totalPriceDelivery!!)
        b.fieldAllPrice.text = totalPrice.toString()
        b.fieldAllPrice.append(" руб.")
    }

    //подсчёт стоимости одной тонны на основании выбора марки угля определённого поставщика
    private fun countPriceCoal(mark: String) {
        for ((key, value) in listPriceCoal) {
            key.also {
                if (it == mark) {
                    accessMark = "$value"
                }
            }
        }
        accessMark?.let {
            b.fieldPriceCoal.text = accessMark
        } ?: Toast.makeText(this, err + "317 | accessMark = null", Toast.LENGTH_SHORT).show()
    }



    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

}