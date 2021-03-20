package com.example.myapplication.distancematrix

data class Rows(
    val elements: List<Elements>
)

data class Distance(
    val text: String,
    val value: Int
)

data class Duration(
    val text: String,
    val value: Int
)

data class Elements(
    val distance: Distance,
    val duration: Duration,
    val status: String
)