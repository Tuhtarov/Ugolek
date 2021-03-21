package com.example.myapplication.common

data class MainActivityData(
    //TODO имена поставщиков
    val Arschanovr: String = "Аршановский разрез",
    val Cirbinskiy: String = "Кирбинский разрез",
    val Chernogorskiy: String = "Черногорский разрез",
    val Izyhskiy: String = "Изыхский разрез",

    //TODO доступные марки угля для поставщиков
    val do25: String = "ДМСШ 0–25",
    val dp: String = "ДО 25–50",
    val dpk: String = "ДПК 50–200",
    val dmsch: String = "ДР 0–300",

    val listNumber: List<String> = listOf("1", "2", "3", "4", "5", "6", "7", "8",
        "9", "10", "11", "12", "13", "14",
        "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27",
        "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "")

)

