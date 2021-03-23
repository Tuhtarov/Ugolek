package com.example.myapplication.common.management.calculate

interface PriceOrder {
    companion object{
        fun calcTotalPriceCoal(quantity: Int, price: Int): String{
            return (quantity * price).toString()
        }
    }
}