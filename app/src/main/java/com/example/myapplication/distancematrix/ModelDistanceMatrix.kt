package com.example.myapplication.distancematrix

data class ModelDistanceMatrix(
    val origin_addresses: List<String>,
    val destination_addresses: List<String>,
    val rows: List<Rows>,
    val status: String
)

