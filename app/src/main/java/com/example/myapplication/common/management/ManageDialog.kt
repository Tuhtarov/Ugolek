package com.example.myapplication.common.management

import android.view.View
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.common.MainActivityData

open class ManagementApp {
    private val data = MainActivityData()
    var listMarkCoal = mutableListOf<String>()

    open fun setTextProvider(nameProvider: String, fieldProvider: TextView) {
        fieldProvider.text = ""
        fieldProvider.append(nameProvider).also {
            when (nameProvider) {
                //обработчики для диалога "выбор поставщика"
                data.Izyhskiy -> listMarkCoal = mutableListOf(data.do25, data.dp, data.dpk)
                data.Chernogorskiy -> listMarkCoal = mutableListOf(data.dpk, data.dp)
                data.Cirbinskiy -> listMarkCoal = mutableListOf(data.do25, data.dmsch, data.dp)
                data.Arschanovr -> listMarkCoal = mutableListOf(data.do25, data.dp, data.dpk, data.dmsch)
            }
        }
    }

    open fun setTextMark(nameMark: String, fieldMarkCoal: TextView){
        fieldMarkCoal.text = ""
        fieldMarkCoal.append(nameMark)
    }
}


interface ManageDialog {
    companion object Manage : ManagementApp() {
        fun onClickElementsOfDialog(button: View, field: TextView) {
            when (button.id) {
                //для диалога "выбор поставщика"
                R.id.btn_izyskhiy -> setTextProvider(MainActivityData().Izyhskiy, field)
                R.id.btn_chernogorsk -> setTextProvider(MainActivityData().Chernogorskiy, field)
                R.id.btn_cirbinsciy -> setTextProvider(MainActivityData().Cirbinskiy, field)
                R.id.btn_arschanov -> setTextProvider(MainActivityData().Arschanovr, field)

                //для диалога "выбор марки угля"
                R.id.btn_dpk_mark -> setTextMark(MainActivityData().dpk, field)
                R.id.btn_dp_mark -> setTextMark(MainActivityData().dp, field)
                R.id.btn_do_mark -> setTextMark(MainActivityData().do25, field)
                R.id.btn_dmsch_mark -> setTextMark(MainActivityData().dmsch, field)
            }
        }
    }
}
