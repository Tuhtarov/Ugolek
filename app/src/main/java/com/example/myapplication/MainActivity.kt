package com.example.myapplication

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_mark.*


class MainActivity : AppCompatActivity() {
    lateinit var intentForMapsLayout: Intent
    private var str: String? = null
    val requestCodes = 0
    lateinit var dialog: Dialog
    var listMarkTon = mutableListOf<String>()

    private var do25: String = ""
    private var dp: String = ""
    private var dpk: String = ""
    private var dmsch: String = ""

    private fun createSecondLayout(){
        Intent(this, SecondActivity::class.java).also {
            startActivity(it)
        }
    }

    // обработка result с класса MapsActivity
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

        imageButton.setOnClickListener {
//
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

        imageButton2.setOnClickListener{
            if ((textView3.text.toString() != "") and (textView4.text.toString() != "")
                and (editTextNumber2.text.toString() != "") and (editTextTextPersonName2.text.toString() != "")){
                createSecondLayout()
            }
        }


        //очистка полей, по нажатию на поле "поставщик"
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

            }

            override fun afterTextChanged(s: Editable?) {
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
        })
    }


    //функции для добавления необходимого текста в поля "поставщик" и "марка угля"
    private fun setTextProvider(str: String) {
        textView3.text = ""
        textView3.append(str)
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
                dialog.dismiss() }}
    }

    //обработчики для диалога "выбор поставщика"
    /*
    R.id.btn_izyskhiy -> {
        setTextProvider(getString(R.string.izyhskiy))
        listMarkTon = mutableListOf(getString(R.string.do25), getString(R.string.dp), getString(R.string.dpk))
    }
    R.id.btn_chernogorsk -> {
        setTextProvider(getString(R.string.chernogorskiy))
        listMarkTon = mutableListOf(getString(R.string.dpk), getString(R.string.dp))
    }
    R.id.btn_cirbinsciy -> {
        setTextProvider(getString(R.string.cirbinskiy))
        listMarkTon = mutableListOf(getString(R.string.do25), getString(R.string.dmsch), getString(R.string.dp))
    }
    R.id.btn_arschanov -> {
        setTextProvider(getString(R.string.arschanovr))
        listMarkTon = mutableListOf(getString(R.string.do25), getString(R.string.dp), getString(R.string.dpk), getString(R.string.dmsch))
    }
     */


    private fun onClick(v: View){
        when(v.id){

            //обработчики для диалога "выбор поставщика"
            R.id.btn_izyskhiy -> setTextProvider(getString(R.string.izyhskiy))
            R.id.btn_chernogorsk -> setTextProvider(getString(R.string.chernogorskiy))
            R.id.btn_cirbinsciy -> setTextProvider(getString(R.string.cirbinskiy))
            R.id.btn_arschanov -> setTextProvider(getString(R.string.arschanovr))

            //обработчики для диалога "выбор марки угля"
            R.id.btn_dpk_mark -> setTextMark(dpk)
            R.id.btn_dp_mark -> setTextMark(dp)
            R.id.btn_do_mark -> setTextMark(do25)
            R.id.btn_dmsch_mark -> setTextMark(dmsch)
            else -> Toast.makeText(this, "Java.NullPointerException)))))))", Toast.LENGTH_LONG).show()
        }
    }
    // Блок допиленного обработчика для нажатия на поле "Поставщик", закомиченно в версии Ugolek 1.1.5


    //TODO РАЗРАБОТКА
    //Блок кода, разрабатываемый под модалки для выбора сорта угля
    private fun showMarkDialog(){
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_mark)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnDMSCH = dialog.findViewById<Button>(R.id.btn_dmsch_mark)
        val btnDO = dialog.findViewById<Button>(R.id.btn_do_mark)
        val btnDP = dialog.findViewById<Button>(R.id.btn_dp_mark)
        val btnDPK = dialog.findViewById<Button>(R.id.btn_dpk_mark)

        do25 = btnDO?.text.toString()
        dp = btnDP?.text.toString()
        dpk = btnDPK?.text.toString()
        dmsch = btnDMSCH?.text.toString()

        val listBttnMark = listOf<Button>(btnDMSCH, btnDO, btnDP, btnDPK)

        when(textView3.text.toString()){
            //обработчики для диалога "выбор поставщика"
            getString(R.string.izyhskiy) -> listMarkTon = mutableListOf(do25, dp, dpk)
            getString(R.string.chernogorskiy) -> listMarkTon = mutableListOf(dpk, dp)
            getString(R.string.cirbinskiy) -> listMarkTon = mutableListOf(do25, dmsch, dp)
            getString(R.string.arschanovr) -> listMarkTon = mutableListOf(do25, dp, dpk, dmsch)
            else -> Toast.makeText(this, "JavaNullPointerException))))", Toast.LENGTH_SHORT).show()
        }

        for (button in listBttnMark) {
            button.text.toString().also {
                textB ->
                //аналог if, в случае соответствия условий, выдаёт перебираемой кнопке атрибут isGone с определенным значением
                button.isGone = !listMarkTon.contains(textB)
            }
        }

        //здесь вешаются "прослушки" на имеющиеся кнопки
        listBttnMark.forEach { button ->
            button.setOnClickListener {
            onClick(it)
            dialog.dismiss()
        } }

        dialog.show()
    }
    //Блок кода, разрабатываемый под модалки для выбора сорта угля



//    //TODO РАЗРАБОТКА ПОДСЧЁТА СТОИМОСТИ ТОННЫ
//    private fun countSum(dp: String?, dmsch: String?, do25: String?, dpk: String?){
//    }

    //диалоговое окно для аршановского разреза
//    private fun showdialogmarkfor_arshanov() {
//        val dialog = MaterialDialog(this)
//            .noAutoDismiss()
//            .customView(R.layout.dialog_mark)
//
//        dialog.findViewById<TextView>(R.id.mark1).setOnClickListener {
//            setTextMark(getString(R.string.mark1))
//            dialog.dismiss()
//            countsumm("1495")
//        }
//
//        dialog.findViewById<TextView>(R.id.mark2).setOnClickListener {
//            setTextMark(getString(R.string.mark2))
//            dialog.dismiss()
//            countsumm("1700")
//        }
//
//        dialog.findViewById<TextView>(R.id.mark3).setOnClickListener {
//            setTextMark(getString(R.string.mark3))
//            countsumm("1905")
//            dialog.dismiss()
//        }
//
//        dialog.findViewById<TextView>(R.id.mark4).setOnClickListener {
//            setTextMark(getString(R.string.mark4))
//            countsumm("1195")
//            dialog.dismiss()
//        }
//        dialog.show()
//
//
//    }
//
//
//
//    //диалоговое окно для кирбинского разреза
//    private fun showdialogmarkfor_cirbinskiy() {
//        val dialog = MaterialDialog(this)
//            .noAutoDismiss()
//            .customView(R.layout.dialog_mark)
//
//        dialog.findViewById<TextView>(R.id.mark1).setOnClickListener {
//            setTextMark(getString(R.string.mark1))
//            countsumm("1495")
//            dialog.dismiss()
//        }
//
//        dialog.findViewById<TextView>(R.id.mark2).setOnClickListener {
//            setTextMark(getString(R.string.mark2))
//            countsumm("1705")
//            dialog.dismiss()
//        }
//
//        dialog.findViewById<TextView>(R.id.mark4).setOnClickListener {
//            setTextMark(getString(R.string.mark4))
//            countsumm("1205")
//            dialog.dismiss()
//        }
//
//        dialog.mark3.isGone = true
//        dialog.show()
//
//    }
//
//    //диалоговое окно для черногорского разреза
//    private fun showdialogmarkfor_chernogoskiy() {
//        val dialog = MaterialDialog(this)
//            .noAutoDismiss()
//            .customView(R.layout.dialog_mark)
//
//        dialog.mark1.isGone = true
//        dialog.mark2.isGone = true
//
//        dialog.findViewById<TextView>(R.id.mark3).setOnClickListener {
//            setTextMark(getString(R.string.mark3))
//            countsumm("1795")
//            dialog.dismiss()
//        }
//
//        dialog.findViewById<TextView>(R.id.mark4).setOnClickListener {
//            setTextMark(getString(R.string.mark4))
//            countsumm("1205")
//            dialog.dismiss()
//        }
//
//        dialog.show()
//
//    }
//
//    //диалоговое окно для изыхского разреза
//    private fun showdialogmarkfor_izyhskiy() {
//        val dialog = MaterialDialog(this)
//            .noAutoDismiss()
//            .customView(R.layout.dialog_mark)
//
//        dialog.mark1.isGone = true
//
//        dialog.findViewById<TextView>(R.id.mark2).setOnClickListener {
//            setTextMark(getString(R.string.mark2))
//            countsumm("1705")
//            dialog.dismiss()
//        }
//
//        dialog.findViewById<TextView>(R.id.mark3).setOnClickListener {
//            setTextMark(getString(R.string.mark3))
//            countsumm("1905")
//            dialog.dismiss()
//        }
//
//        dialog.findViewById<TextView>(R.id.mark4).setOnClickListener {
//            setTextMark(getString(R.string.mark4))
//            countsumm("1195")
//            dialog.dismiss()
//        }
//
//        dialog.show()
//
//    }


}