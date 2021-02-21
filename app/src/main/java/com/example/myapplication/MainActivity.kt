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
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var intentForMapsLayout: Intent
    private var str: String? = null
    private val requestCodes = 0
    private lateinit var dialog: Dialog
    private var listMarkCoal = mutableListOf<String>()
    private var listPriceTon = mutableMapOf<String, Int>()

    private var do25 = ""
    private var dp = ""
    private var dpk = ""
    private var dmsch = ""

    private var err: String = "JavaNullPointerExceptions"

    private fun createSecondLayout(){
        Intent(this, SecondActivity::class.java).also {
            startActivity(it)
        }
    }

    //обработка result с класса MapsActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val field = findViewById<TextView>(R.id.textView3)
        if(field.text == ""){ str = null }

        if (requestCode == requestCodes){
            if (resultCode == RESULT_OK){
                val strr = data?.getStringExtra("sms")
                editTextTextPersonName2.setText(strr)
            }
        } else {
            editTextTextPersonName2.setText("")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        intentForMapsLayout = Intent(this, MapsActivity::class.java)

        //кнопка "выбрать на карте"
        imageButton.setOnClickListener {
//            if ((textView3.text.toString() == "") and (textView4.text.toString() == "") and (editTextNumber2.text.toString() == "")){
//                createToast("Заполните поля: Поставщик, Марка угля, Требуемая масса")
//            } else if ((textView3.text.toString() != "") and (textView4.text.toString() == "") and (editTextNumber2.text.toString() == "")){
//                createToast("Заполните поля: Марка угля, Требуемая масса")
//            } else if ((textView3.text.toString() != "") and (textView4.text.toString() != "") and (editTextNumber2.text.toString() == "")){
//                createToast("Заполните поля: Требуемая масса")
//            } else {
//            }

            intentForMapsLayout.putExtra("provider", str)
            startActivityForResult(intentForMapsLayout, requestCodes)

        }

        //кнопка "марка угля"
        textView4.setOnClickListener {
            textView3.text.toString().also {
                if(it != ""){
                    str = it
                    showMarkDialog()
                } else {
                    Toast.makeText(this, "Для выбора марки угля необходимо изначально выбрать поставщика", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //кнопка "заказать"
        imageButton2.setOnClickListener{
            if ((textView3.text.toString() != "") and (textView4.text.toString() != "")
                and (editTextNumber2.text.toString() != "") and (editTextTextPersonName2.text.toString() != "")){
                createSecondLayout()
            }
        }

        //кнопка "поставщик" + очистка полей
        textView3.setOnClickListener {
            textView3.text = ""
            textView4.text = ""
            summ_tonn.text = ""
            editTextNumber2.setText("")
            str = null
            showProviderDialog()
        }


        //ограничение на ввод чисел для текстового поля
        editTextNumber2.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val listNumber = listOf(
                    "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
                    "23", "24", "25", "26", "27", "28", "29", "30", "31",
                    "32", "33", "34", "35", "36", "37", "38", "39", "40", ""
                )
                if (!listNumber.contains(editTextNumber2.text.toString())) {
                    editTextNumber2.text.clear()
                    Toast.makeText(applicationContext, "Только целочисленные значения от 1-40", Toast.LENGTH_SHORT).show()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //TODO здесь можно производить вычисление для определение общей стоимости заказа
            }
        })
    }


    //для присвоения имени выбранного поставщика в поле "поставщик"
    private fun setTextProvider(nameProvider: String) {
        textView3.text = ""
        //переменным присваиваются названия марок угля, определенные в строковых ресурсах (необходимо для создания списка угля соответствующего поставщика)
        do25 = getString(R.string.do25)
        dp = getString(R.string.dp)
        dpk = getString(R.string.dpk)
        dmsch = getString(R.string.dmsch)

        textView3.append(nameProvider).also {
            when(textView3.text.toString()){
                //обработчики для диалога "выбор поставщика"
                getString(R.string.izyhskiy) -> listMarkCoal = mutableListOf(do25, dp, dpk)
                getString(R.string.chernogorskiy) -> listMarkCoal = mutableListOf(dpk, dp)
                getString(R.string.cirbinskiy) -> listMarkCoal = mutableListOf(do25, dmsch, dp)
                getString(R.string.arschanovr) -> listMarkCoal = mutableListOf(do25, dp, dpk, dmsch)
                else -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setTextMark(str: String) {
        textView4.text = ""
        textView4.append(str)
    }


    // Блок допиленного обработчика для нажатия на поле "Поставщик", закомиченно в версии Ugolek 1.1.5
    // изменения: использованы возможности Kotlin, реализован лаконичный обработчик кнопок в модалке, через listOf. Заменён класс модалок на более простой.
    private fun showProviderDialog(){
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_provider)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val btnArshanov = dialog.findViewById<Button>(R.id.btn_arschanov)
        val btnChernogorsk = dialog.findViewById<Button>(R.id.btn_chernogorsk)
        val btnCirbinsciy = dialog.findViewById<Button>(R.id.btn_cirbinsciy)
        val btnIzyshkiy = dialog.findViewById<Button>(R.id.btn_izyskhiy)
        dialog.show()

        val listBttnProvider = listOf<Button>(btnArshanov,btnChernogorsk,btnCirbinsciy,btnIzyshkiy)
        listBttnProvider.forEach { button ->
            button.setOnClickListener{
                onClick(it)
                //здесь определяется (determine) список угля и цен на них для выбранного поставщика
                determineListCoalForProvider(textView3.text.toString())
                dialog.dismiss()
            }
        }
    }

    //обработчик для модалок
    private fun onClick(v: View){
        when(v.id){
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
            else -> Toast.makeText(this, "Java.NullPointerException)))))))", Toast.LENGTH_LONG).show()
        }
    }

    //модалка для выбора сорта угля
    private fun showMarkDialog(){
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
            button.text.toString().also {
                textB ->
                //аналог if, в случае соответствия условий, выдаёт перебираемой кнопке атрибут isGone true/false
                button.isGone = !listMarkCoal.contains(textB)
            }
        }

        //здесь вешаются "прослушки" на имеющиеся кнопки
        listBtnMark.forEach { button ->
            button.setOnClickListener {
            onClick(it)
                //здесь вызывается ф-я для определения цены за выбранную марку угля
                countPriceCoal(button.text.toString())
                dialog.dismiss()
        } }
        dialog.show()

    }


    //здесь определяется список угля и цен на них для выбранного поставщика
    private fun determineListCoalForProvider(providerName: String){
        listPriceTon.clear()
        when(providerName){
            getString(R.string.cirbinskiy)-> {
                //я тебя вижу друг
                //лист цен для кирбинского разреза
                listPriceTon[dmsch] = 1495
                listPriceTon[do25] = 1705
                listPriceTon[dp] = 1205
            }
            getString(R.string.chernogorskiy) -> {
                //лист цен для черногорского разреза
                listPriceTon[dpk] = 1795
                listPriceTon[dp] = 1205
            }
            getString(R.string.izyhskiy)-> {
                //лист цен для изыхского разреза
                listPriceTon[do25] = 1705
                listPriceTon[dpk] = 1905
                listPriceTon[dp] = 1195
            }
            getString(R.string.arschanovr)-> {
                //лист цен для аршановского разреза
                listPriceTon[dmsch] = 1495
                listPriceTon[do25] = 1700
                listPriceTon[dpk] = 1905
                listPriceTon[dp] = 1195
            }
            else -> Toast.makeText(this, err + "262", Toast.LENGTH_SHORT).show()
        }
    }

    //подсчёт стоимости одной тонны на основании выбора марки угля определённого поставщика
    private fun countPriceCoal(mark: String){
        var accessMark:String? = null
        //если приходящий аргумент соответствует одному из элементов списка listPriceTon, то этот элемент присваивается переменной выше
        for((key, value) in listPriceTon){
            key.also {
                if(it == mark){
                    accessMark = "$value"
                    Log.d("usefulArg", "args: mark == $accessMark")
                } else {
                    Log.d("notEqual", "args: mark == $mark <> it == $it")
                }
            }
        }

        //если переменная не null, то тогда её значение присвается в текстовое поле summ_tonn на activity_main
        accessMark?.let {
            summ_tonn.text = accessMark
        } ?: Toast.makeText(this, err + "285 / accessMark = null", Toast.LENGTH_SHORT).show()
    }


}