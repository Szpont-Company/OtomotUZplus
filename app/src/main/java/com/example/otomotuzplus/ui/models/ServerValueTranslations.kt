package com.example.otomotuzplus.ui.models

import java.util.Locale

fun localizeFuelType(rawValue: String, strings: AppStrings): String {
    val value = rawValue.trim()
    return when (fuelTypeKey(value)) {
        "petrol" -> strings.fuelPetrol
        "diesel" -> strings.fuelDiesel
        "hybrid" -> strings.fuelHybrid
        "electric" -> strings.fuelElectric
        else -> value
    }
}

fun localizeGearboxType(rawValue: String, strings: AppStrings): String {
    val value = rawValue.trim()
    return when (gearboxTypeKey(value)) {
        "automatic" -> strings.transmissionAutomatic
        "manual" -> strings.transmissionManual
        else -> value
    }
}

fun fuelTypeKey(rawValue: String): String? {
    return when (rawValue.trim().lowercase(Locale.ROOT)) {
        "benzyna", "petrol", "gasoline", "benzin" -> "petrol"
        "diesel", "olej napedowy" -> "diesel"
        "hybryda", "hybrid", "phev", "hev" -> "hybrid"
        "elektryczny", "electric", "ev" -> "electric"
        else -> null
    }
}

fun gearboxTypeKey(rawValue: String): String? {
    return when (rawValue.trim().lowercase(Locale.ROOT)) {
        "automatyczna", "automatic", "auto", "at", "cvt", "dct", "dsg" -> "automatic"
        "manualna", "manual", "man", "mt" -> "manual"
        else -> null
    }
}

