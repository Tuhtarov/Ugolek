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
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var intentForMapsLayout: Intent
    private var providerName: String? = null
    private val REQUEST_CODE = 0
    private var distanceBetweenTwoPoints: String? = null
    private lateinit var dialog: Dialog
    private lateinit var binding: ActivityMainBinding

    private var do25 = ""
    private var dp = ""
    private var dpk = ""
    private var dmsch = ""
    private var listMarkCoal = mutableListOf<String>()
    private var listPriceCoal = mutableMapOf<String, Int>()
    var accessMark: String? = null

    private var totalPriceCoal: Float? = null
    private var totalPriceDelivery: Float? = null
    private var totalPrice: Float? = null

    private var err: String = "JavaNullPointerException"
    private val TAG = MainActivity::class.java.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        intentForMapsLayout = Intent(this, MapsActivity::class.java)

        field_provider.setOnClickListener {
            cleanDataFields()
            showProviderDialog()
        }

        field_markCoal.setOnClickListener {
            field_provider.text.toString().also {
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

        field_requiredMass.addTextChangedListener(object : TextWatcher {
            val listNumber = listOf("1", "2", "3", "4", "5", "6", "7", "8",
                "9", "10", "11", "12", "13", "14",
                "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27",
                "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", ""
            )
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!listNumber.contains(field_requiredMass.text.toString())) {
                    field_requiredMass.text.clear()
                    Toast.makeText(
                        applicationContext,
                        "Только целочисленные значения от 1-40",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun afterTextChanged(s: Editable?) {
                //TODO здесь можно производить вычисление для определение общей стоимости заказа
                if (!listNumber.contains(field_requiredMass.text.toString())) {
                    field_requiredMass.text.clear()
                } else if (field_requiredMass.text.toString() != ""){
                    calculateTotalPriceCoal(field_requiredMass.text.toString().toInt())
                } else {
                    Log.d(TAG, "afterTextChanged: количество угля ещё не определено")
                }
            }
        })

        btn_chooseOnMap.setOnClickListener {
            //checkFillingFields()
            intentForMapsLayout.putExtra("provider", providerName)
            startActivityForResult(intentForMapsLayout, REQUEST_CODE)
        }

        btn_toOrder.setOnClickListener {
            if ((field_provider.text.toString() != "") and (field_markCoal.text.toString() != "")
                and (field_requiredMass.text.toString() != "") and (field_addressDelivery.text.toString() != "")) {
                createSecondLayout()
            } else {
                Toast.makeText(this, "Заполните недостающие поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //очистка полей от старых значений
    private fun cleanDataFields(){
        field_provider.text = ""
        field_markCoal.text = ""
        field_priceCoal.text = ""
        field_delivery.text = ""
        field_distance.text = ""
        field_allPrice.text = ""
        if(field_requiredMass.text.toString() != ""){ field_requiredMass.text.clear() }
        field_addressDelivery.setText("")
        accessMark = null
        providerName = null
        distanceBetweenTwoPoints = null
    }

    //проверка заполнения полей, первый аргумент - для кнопки "выбрать на карте", второй - для кнопки "заказать"
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
            it.putExtra("provider", field_provider.text.toString())
            it.putExtra("coal", field_markCoal.text.toString())
            it.putExtra("priceCoal", field_priceCoal.text.toString())
            it.putExtra("addressDelivery", field_addressDelivery.text.toString())
            it.putExtra("requiredMass", field_requiredMass.text.toString())
            startActivity(it)
        }
    }

    //обработка result с класса MapsActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val field = findViewById<TextView>(R.id.field_provider)
        if (field.text == "") { providerName = null }

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val addressResult = data?.getStringExtra("sms")
                data?.getStringExtra("distance")?.let {
                    if(it.replaceAfter(".","").replace(".","").toInt() != 0){
                        distanceBetweenTwoPoints = it.replaceAfter(".","").replace(".","")
                        field_distance.text = distanceBetweenTwoPoints
                        calculatePriceDelivery()
                        calculateAllPrice()
                    } else {
                        Toast.makeText(
                            this,
                            "Заполните все поля, и выберите адрес",
                            Toast.LENGTH_SHORT
                        ).show()
                        field_delivery.text = ""
                        field_distance.text = ""
                        field_allPrice.text = ""
                    }
                }
                field_addressDelivery.setText(addressResult)
            }
        } else {
            field_addressDelivery.setText("")
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
                determineListCoalForProvider(field_provider.text.toString())
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
                //аналог if, в случае соответствия условий, выдаёт перебираемой кнопке атрибут isGone true/false
                button.isGone = !listMarkCoal.contains(textB)
            }
        }

        //здесь вешаются "прослушки" на имеющиеся кнопки
        listBtnMark.forEach { button ->
            button.setOnClickListener {
                onClickBtn(it)
                //здесь вызывается ф-я для определения цены за выбранную марку угля
                countPriceCoal(button.text.toString())
                if (field_requiredMass.text.toString() != "") {
                    calculateTotalPriceCoal(field_requiredMass.text.toString().toInt())
                    calculateAllPrice()
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
        field_markCoal.text = ""
        field_markCoal.append(markOfCoal)
    }

    //для присвоения имени выбранного поставщика в поле "поставщик"
    private fun setTextProvider(nameProvider: String) {
        field_provider.text = ""
        //переменным присваиваются названия марок угля, определенные в строковых ресурсах (необходимо для создания списка угля соответствующего поставщика) v1.2.5
        do25 = getString(R.string.do25)
        dp = getString(R.string.dp)
        dpk = getString(R.string.dpk)
        dmsch = getString(R.string.dmsch)

        field_provider.append(nameProvider).also {
            when (field_provider.text.toString()) {
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
            totalPriceCoal = quantity * accessMark.toString().toFloat()
        } else {
            Log.d(TAG, "calculateTotalPriceCoal: стоимость марки ещё не определена")
        }
    }

    private fun calculatePriceDelivery(){
        var priceQuantityCoal: Float? = null
        val quantityCoal = field_requiredMass.text.toString()

        if (distanceBetweenTwoPoints != null){
            when (quantityCoal.toInt()) {
                in 1..3 -> {
                    priceQuantityCoal = 10f
                }
                in 4..7 -> {
                    priceQuantityCoal = 15f
                }
                in 8..20 -> {
                    priceQuantityCoal = 35f
                }
                in 21..40 -> {
                    priceQuantityCoal = 80f
                }
                else -> {
                    Log.e(TAG, "err 195")
                }
            }
        } else {
            Toast.makeText(this, "Выберите адресс доставки", Toast.LENGTH_SHORT).show()
        }

        priceQuantityCoal?.let{
            val ONE_KILOMETER = 1000f
            totalPriceDelivery = it * (distanceBetweenTwoPoints!!.toFloat() / ONE_KILOMETER)
            field_delivery.text = totalPriceDelivery!!.toFloat().toString()
        } ?: run {
            Toast.makeText(this@MainActivity, "Выберите адресс доставки", Toast.LENGTH_SHORT).show()
        }
    }


    private fun calculateAllPrice(){
        totalPrice = totalPriceCoal?.plus(totalPriceDelivery!!)
        field_allPrice.text = totalPrice.toString()
    }

    //подсчёт стоимости одной тонны на основании выбора марки угля определённого поставщика
    private fun countPriceCoal(mark: String) {
        /* если приходящий аргумент соответствует одному из элементов списка listPriceCoal, то этот элемент
        списка присваивается переменной accessMark */
        for ((key, value) in listPriceCoal) {
            key.also {
                if (it == mark) {
                    accessMark = "$value"
                }
            }
        }
        accessMark?.let {
            field_priceCoal.text = accessMark
        } ?: Toast.makeText(this, err + "317 | accessMark = null", Toast.LENGTH_SHORT).show()
    }

}