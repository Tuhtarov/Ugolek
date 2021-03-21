//package com.example.myapplication.common.management
//
//import android.widget.Toast
//import com.example.myapplication.R
//
//open class Provider() {
//    var listPriceCoal = mutableMapOf<String, Int>()
//
//    //TODO имена поставщиков
//    private val Arschanovr = "Аршановский разрез"
//    private val Cirbinskiy = "Кирбинский разрез"
//    private val Chernogorskiy = "Черногорский разрез"
//    private val Izyhskiy = "Изыхский разрез"
//
//    //TODO доступные марки угля
//    private val do25 = "ДМСШ 0–25"
//    private val dp = "ДО 25–50"
//    private val dpk = "ДПК 50–200"
//    private val dmsch = "ДР 0–300"
//
//
//    fun determineListCoalForProvider(providerName: String) {
//        listPriceCoal.clear()
//        when (providerName) {
//            Cirbinskiy -> {
//                //я тебя вижу друг
//                //лист цен для кирбинского разреза
//                listPriceCoal[dmsch] = 1495
//                listPriceCoal[do25] = 1705
//                listPriceCoal[dp] = 1205
//            }
//            Chernogorskiy -> {
//                //лист цен для черногорского разреза
//                listPriceCoal[dpk] = 1795
//                listPriceCoal[dp] = 1205
//            }
//            Izyhskiy -> {
//                //лист цен для изыхского разреза
//                listPriceCoal[do25] = 1705
//                listPriceCoal[dpk] = 1905
//                listPriceCoal[dp] = 1195
//            }
//            Arschanovr -> {
//                //лист цен для аршановского разреза
//                listPriceCoal[dmsch] = 1495
//                listPriceCoal[do25] = 1700
//                listPriceCoal[dpk] = 1905
//                listPriceCoal[dp] = 1195
//            }
//        }
//    }
//
//
//
//}
//
//open class CalculatePriceCoal {
//    val TAG = "CalculatePriceClass"
//
//    fun calculateTotalPriceCoal(quantity: Int, priceCoal: Int) {
//        totalPriceCoal = quantity * priceCoal
//    }
//
//
//    //подсчёт стоимости одной тонны на основании выбора марки угля определённого поставщика
//    fun countPriceCoal(mark: String) {
//        for ((coal, price) in Provider().listPriceCoal) {
//            coal.also {
//                if (it == mark) {
//                    b.fieldPriceCoal.text = "$price"
//                }
//            }
//        }
//    }
//
//
//    private var listPriceCoal = mutableMapOf<String, Int>()
//
//    //здесь определяется список угля и цен на них для выбранного поставщика
//    private fun determineListCoalForProvider(providerName: String) {
//        listPriceCoal.clear()
//        when (providerName) {
//            Cirbinskiy -> {
//                //я тебя вижу друг
//                //лист цен для кирбинского разреза
//                listPriceCoal[dmsch] = 1495
//                listPriceCoal[do25] = 1705
//                listPriceCoal[dp] = 1205
//            }
//            Chernogorskiy -> {
//                //лист цен для черногорского разреза
//                listPriceCoal[dpk] = 1795
//                listPriceCoal[dp] = 1205
//            }
//            Izyhskiy -> {
//                //лист цен для изыхского разреза
//                listPriceCoal[do25] = 1705
//                listPriceCoal[dpk] = 1905
//                listPriceCoal[dp] = 1195
//            }
//            Arschanovr -> {
//                //лист цен для аршановского разреза
//                listPriceCoal[dmsch] = 1495
//                listPriceCoal[do25] = 1700
//                listPriceCoal[dpk] = 1905
//                listPriceCoal[dp] = 1195
//            }
//        }
//    }
//
//}
//
//open class CalculatePriceDelivery(
//    private var distanceBetween: String,
//    private var quantityCoal: String
//) {
//    fun calculatePriceDelivery(): String {
//        var priceDeliveryForQuantitiesOfCoal = 0
//
//        var distance = distanceBetween.replace("км", "").trim()
//        when (quantityCoal.toInt()) {
//            in 1..3 -> {
//                priceDeliveryForQuantitiesOfCoal = 10
//            }
//            in 4..7 -> {
//                priceDeliveryForQuantitiesOfCoal = 15
//            }
//            in 8..20 -> {
//                priceDeliveryForQuantitiesOfCoal = 35
//            }
//            in 21..40 -> {
//                priceDeliveryForQuantitiesOfCoal = 80
//            }
//        }
//        return (priceDeliveryForQuantitiesOfCoal * distance.toInt()).toString()
//    }
//}
//
//open class CalculatePriceOrder {
//    fun calculateAllPrice() {
//        b.fieldAllPrice.text = totalPriceCoal?.plus(totalPriceDelivery!!).toString()
//        b.fieldAllPrice.append(" руб.")
//    }
//}