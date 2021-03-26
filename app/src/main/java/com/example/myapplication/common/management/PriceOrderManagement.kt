package com.example.myapplication.common.management

import com.example.myapplication.common.management.calculate.PriceOrder

interface PriceOrderManagement {
    fun calculatePriceDelivery(distance: Float, requiredMass: Int): String{
        return PriceOrder.calcPriceDelivery(distance, requiredMass)
    }
    fun calculatePriceOrder(priceCoal: Int, requiredMass: Int, deliveryPrice: Float): String{
        return PriceOrder.calcPriceOrder(priceCoal, requiredMass, deliveryPrice)
    }
}