package com.example.myapplication.distancematrixtwo

import com.example.myapplication.distancematrix.Elements
import com.example.myapplication.distancematrix.Rows

data class RowsMatrix(
    val destination_addresses: List<String>,
    val origin_addresses: List<String>,
    val rows: List<RowsM>,
    val status: String
)

data class RowsM(
    val elements: List<ElementsModel>
)

data class ElementsModel(
    val distance: DistanceModel,
    val duration: DurationModel,
    val status: String
)
data class DistanceModel(
    val text: String,
    val value: Int
)
data class DurationModel(
    val text: String,
    val value: Int
)