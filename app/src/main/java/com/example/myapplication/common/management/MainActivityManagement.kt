package com.example.myapplication.common.management

import com.example.myapplication.R
import com.example.myapplication.common.MainActivityData

class MainActivityManagement {
    val d = MainActivityData
    private var listCoalForProvider = listOf<String>()
    private var priceListOfCoal = mutableMapOf<String, Int>()

    fun createListCoalForProvider(providerName: String){
        priceListOfCoal.clear()
        when(providerName){
            d.Arschanovr -> {
                listCoalForProvider = listOf(d.dmsch, d.do25, d.dpk, d.dp)
                priceListOfCoal[d.dmsch] = 1495
                priceListOfCoal[d.do25] = 1700
                priceListOfCoal[d.dpk] = 1905
                priceListOfCoal[d.dp] = 1195
            }
            d.Chernogorskiy -> {
                listCoalForProvider = listOf(d.dpk, d.dp)
                priceListOfCoal[d.dpk] = 1795
                priceListOfCoal[d.dp] = 1205
            }
            d.Izyhskiy -> {
                listCoalForProvider = listOf(d.do25, d.dpk, d.dp)
                priceListOfCoal[d.do25] = 1705
                priceListOfCoal[d.dpk] = 1905
                priceListOfCoal[d.dp] = 1195
            }
            d.Cirbinskiy -> {
                listCoalForProvider = listOf(d.dmsch, d.do25, d.dp)
                priceListOfCoal[d.dmsch] = 1495
                priceListOfCoal[d.do25] = 1695
                priceListOfCoal[d.dp] = 1205
            }
        }
    }

    fun getListCoalForProvider(): List<String>{
        return this.listCoalForProvider
    }
    fun getPriceListOfCoal(): MutableMap<String, Int>{
        return this.priceListOfCoal
    }
}