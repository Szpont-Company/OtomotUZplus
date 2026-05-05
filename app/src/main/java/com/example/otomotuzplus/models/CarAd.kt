package com.example.otomotuzplus.models

data class CarAd(
    val id: String = "",
    val title: String = "",
    val priceText: String = "",
    val locationText: String = "",
    val postalCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val year: String = "",
    val mileageText: String = "",
    val fuelText: String = "",
    val gearboxText: String = "",
    val engineCapacity: String = "",
    val powerText: String = "",
    val imageUrls: List<String> = emptyList(),
    val sellerId: String = "",
    val phoneNumber: String = ""
)