package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_second.*
import kotlinx.android.synthetic.main.mark_layout.*


class MainActivity : AppCompatActivity() {

    lateinit var Preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var str: String

    //инициализация переменных
    @SuppressLint("CommitPrefEdits")
    private fun prefinit() {
        Preferences = getSharedPreferences("Hello", MODE_PRIVATE)
        editor = Preferences.edit()
    }

    fun createSecondLayout(){
        val secondLayout = Intent(this, SecondActivity::class.java)
        startActivity(secondLayout)
    }

    fun createMapsLayout(){
        val mapsLayout = Intent(this, MapsActivity::class.java)
        mapsLayout.putExtra("provider", str)
        startActivity(mapsLayout)
    }

    fun createToast(string: String){
            Toast.makeText(
            applicationContext,
            "$string",
            Toast.LENGTH_SHORT
        ).show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefinit()


        //активация кнопки провайдера
        textView3.setOnClickListener {
            textView3.text = ""
            textView4.text = ""
            summ_tonn.text = ""
            showdialogprovider()
        }


        imageButton.setOnClickListener {

            if ((textView3.text.toString() == "") and (textView4.text.toString() == "") and (editTextNumber2.text.toString() == "")){
                createToast("Заполните поля: Поставщик, Марка угля, Требуемая масса")
            } else if ((textView3.text.toString() != "") and (textView4.text.toString() == "") and (editTextNumber2.text.toString() == "")){
                createToast("Заполните поля: Марка угля, Требуемая масса")
            } else if ((textView3.text.toString() != "") and (textView4.text.toString() != "") and (editTextNumber2.text.toString() == "")){
                createToast("Заполните поля: Требуемая масса")
            } else {
                createMapsLayout()
            }

        }


        textView4.setOnClickListener {
            if (textView3.text.toString() != "") {
                when (textView3.text.toString()) {
                    getString(R.string.arschanovr) -> {
                        showdialogmarkfor_arshanov()
                        str = ""
                        str = "arshanov"
                    }

                    getString(R.string.cirbinskiy) -> {
                        showdialogmarkfor_cirbinskiy()
                        str = ""
                        str = "cirbinskiy"
                    }

                    getString(R.string.chernogorskiy) -> {
                        showdialogmarkfor_chernogoskiy()
                        str = ""
                        str = "chernogorskiy"
                    }

                    getString(R.string.izyhskiy) -> {
                        showdialogmarkfor_izyhskiy()
                        str = ""
                        str = "izyhskiy"
                    }

                    else -> createToast("Заполняй последовательно")
                }
            } else {
                createToast("Заполняй последовательно")
            }
        }




        imageButton2.setOnClickListener{
            if ((textView3.text.toString() != "") and (textView4.text.toString() != "") and (editTextNumber2.text.toString() != "") and (editTextTextPersonName2.text.toString() != "")){
                createSecondLayout()
            }
        }
        

        //ограничение на ввод чисел для текстового поля
        editTextNumber2.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val listNumber = listOf<String>(
                    "1",
                    "2",
                    "3",
                    "4",
                    "5",
                    "6",
                    "7",
                    "8",
                    "9",
                    "10",
                    "11",
                    "12",
                    "13",
                    "14",
                    "15",
                    "16",
                    "17",
                    "18",
                    "19",
                    "20",
                    "21",
                    "22",
                    "23",
                    "24",
                    "25",
                    "26",
                    "27",
                    "28",
                    "29",
                    "30",
                    "31",
                    "32",
                    "33",
                    "34",
                    "35",
                    "36",
                    "37",
                    "38",
                    "39",
                    "40",
                    ""
                )
                if (!listNumber.contains(editTextNumber2.text.toString())) {
                    editTextNumber2.text.clear()
                    val WARN = Toast.makeText(
                        applicationContext,
                        "Только целочисленные значения от 1-40",
                        Toast.LENGTH_SHORT
                    )
                    WARN.show()
                }


            }


        })


    }





    //функция, подставляющая текстовое значение в поле "стоимость тонны", в зависимости от значений предыдущих полей
    fun countsumm(value: String) {
        summ_tonn.text = value
    }

    //функции для добавления необходимого текста в поля "поставщик" и "марка угля"
    fun setTextProvider(str: String) {
        textView3.text = ""
        textView3.append(str)
    }

    fun setTextMark(str: String) {
        textView4.text = ""
        textView4.append(str)

    }



    //функция, вызывающая диалоговое окно провайдера
    private fun showdialogprovider() {

        val dialog = MaterialDialog(this)
            .noAutoDismiss()
            .customView(R.layout.provoder_layoutt)

        dialog.findViewById<ImageButton>(R.id.btn_arschanovr).setOnClickListener {
            setTextProvider(getString(R.string.arschanovr))
            dialog.dismiss()
        }

        dialog.findViewById<ImageButton>(R.id.btn_cirbinsciyr).setOnClickListener {
            setTextProvider(getString(R.string.cirbinskiy))
            dialog.dismiss()
        }

        dialog.findViewById<ImageButton>(R.id.btn_chernogorskiyr).setOnClickListener {
            setTextProvider(getString(R.string.chernogorskiy))
            dialog.dismiss()
        }

        dialog.findViewById<ImageButton>(R.id.btn_izyskhiyr).setOnClickListener {
            setTextProvider(getString(R.string.izyhskiy))
            dialog.dismiss()
        }

        dialog.show()

    }

    //диалоговое окно для аршановского разреза
    private fun showdialogmarkfor_arshanov() {

        val dialog = MaterialDialog(this)
            .noAutoDismiss()
            .customView(R.layout.mark_layout)

        dialog.findViewById<TextView>(R.id.mark1).setOnClickListener {
            setTextMark(getString(R.string.mark1))
            dialog.dismiss()
            countsumm("1495")
        }

        dialog.findViewById<TextView>(R.id.mark2).setOnClickListener {
            setTextMark(getString(R.string.mark2))
            dialog.dismiss()
            countsumm("1700")
        }

        dialog.findViewById<TextView>(R.id.mark3).setOnClickListener {
            setTextMark(getString(R.string.mark3))
            countsumm("1905")
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.mark4).setOnClickListener {
            setTextMark(getString(R.string.mark4))
            countsumm("1195")
            dialog.dismiss()
        }
        dialog.show()

    }

    //диалоговое окно для кирбинского разреза
    private fun showdialogmarkfor_cirbinskiy() {

        val dialog = MaterialDialog(this)
            .noAutoDismiss()
            .customView(R.layout.mark_layout)

        dialog.findViewById<TextView>(R.id.mark1).setOnClickListener {
            setTextMark(getString(R.string.mark1))
            countsumm("1495")
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.mark2).setOnClickListener {
            setTextMark(getString(R.string.mark2))
            countsumm("1705")
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.mark4).setOnClickListener {
            setTextMark(getString(R.string.mark4))
            countsumm("1205")
            dialog.dismiss()
        }

        dialog.mark3.isGone = true
        dialog.show()

    }

    //диалоговое окно для черногорского разреза
    private fun showdialogmarkfor_chernogoskiy() {

        val dialog = MaterialDialog(this)
            .noAutoDismiss()
            .customView(R.layout.mark_layout)

        dialog.mark1.isGone = true
        dialog.mark2.isGone = true

        dialog.findViewById<TextView>(R.id.mark3).setOnClickListener {
            setTextMark(getString(R.string.mark3))
            countsumm("1795")
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.mark4).setOnClickListener {
            setTextMark(getString(R.string.mark4))
            countsumm("1205")
            dialog.dismiss()
        }

        dialog.show()

    }

    //диалоговое окно для изыхского разреза
    private fun showdialogmarkfor_izyhskiy() {

        val dialog = MaterialDialog(this)
            .noAutoDismiss()
            .customView(R.layout.mark_layout)

        dialog.mark1.isGone = true

        dialog.findViewById<TextView>(R.id.mark2).setOnClickListener {
            setTextMark(getString(R.string.mark2))
            countsumm("1705")
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.mark3).setOnClickListener {
            setTextMark(getString(R.string.mark3))
            countsumm("1905")
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.mark4).setOnClickListener {
            setTextMark(getString(R.string.mark4))
            countsumm("1195")
            dialog.dismiss()
        }

        dialog.show()


    }
}