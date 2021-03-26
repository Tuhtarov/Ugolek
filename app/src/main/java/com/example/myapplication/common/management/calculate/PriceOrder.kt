package com.example.myapplication.common.management.calculate

import android.util.Log

interface PriceOrder {
    companion object {
        //подсчёт стоимости доставки
        fun calcPriceDelivery(distance: Float, requiredMass: Int): String {
            var priceQuantityCoal: Float = 0f
            when (requiredMass) {
                in 1..3 -> priceQuantityCoal = 10f
                in 4..7 -> priceQuantityCoal = 15f
                in 8..20 -> priceQuantityCoal = 35f
                in 21..40 -> priceQuantityCoal = 80f
                else -> Log.e("TAG", "Уголь не привезут, ошибка на 18 строчке кода, class PriceOrder")
            }
            return (distance * priceQuantityCoal).toString()
        }

        //подсчёт общей стоимости заказа
        fun calcPriceOrder(priceCoal: Int, requiredMass: Int, deliveryPrice: Float): String{
            return ((priceCoal * requiredMass).toFloat() + deliveryPrice).toString().replace(".0", "")
        }
    }
}