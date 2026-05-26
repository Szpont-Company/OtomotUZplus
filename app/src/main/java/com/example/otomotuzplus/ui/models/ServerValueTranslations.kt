/**
 * @file ServerValueTranslations.kt
 * @brief Funkcje pomocnicze mapujące surowe wartości pól Firestore na
 *   zlokalizowane ciągi wyświetlania dla rodzaju paliwa i skrzyni biegów.
 *
 * Dokumenty Firestore przechowują wartości paliwa i skrzyni w języku użytym
 * przez sprzedającego (polskim lub angielskim). Funkcje te normalizują surową
 * wartość do klucza kanonicznego, a następnie rozwiązują ją przez aktywną
 * instancję [AppStrings].
 */
package com.example.otomotuzplus.ui.models

import java.util.Locale

/**
 * Konwertuje surową wartość rodzaju paliwa z Firestore na zlokalizowany ciąg wyświetlania.
 *
 * Konwersja nie rozróżnia wielkości liter i toleruje pisownię polską i angielską.
 * Nieznane wartości zwracane bez zmian.
 *
 * @param rawValue Surowy ciąg rodzaju paliwa z Firestore (np. `"Benzyna"` lub `"petrol"`).
 * @param strings  Aktywna instancja [AppStrings] zawierająca zlokalizowaną etykietę.
 * @return Zlokalizowana etykieta rodzaju paliwa lub [rawValue] jeśli klucz nie został rozpoznany.
 */
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

/**
 * Konwertuje surową wartość skrzyni biegów z Firestore na zlokalizowany ciąg wyświetlania.
 *
 * @param rawValue Surowy ciąg skrzyni biegów (np. `"Manualna"`, `"automatic"`, `"DSG"`).
 * @param strings  Aktywna instancja [AppStrings].
 * @return Zlokalizowana etykieta skrzyni biegów lub [rawValue] jeśli nierozpoznana.
 */
fun localizeGearboxType(rawValue: String, strings: AppStrings): String {
    val value = rawValue.trim()
    return when (gearboxTypeKey(value)) {
        "automatic" -> strings.transmissionAutomatic
        "manual" -> strings.transmissionManual
        else -> value
    }
}

/**
 * Normalizuje surowy ciąg rodzaju paliwa do niezależnego od języka klucza kanonicznego.
 *
 * Rozpoznawane klucze: `"petrol"`, `"diesel"`, `"hybrid"`, `"electric"`.
 * Używane przez [localizeFuelType] i logikę dopasowania filtrów wyszukiwania.
 *
 * @param rawValue Surowa wartość rodzaju paliwa (bez rozróżniania wielkości liter, po polsku lub angielsku).
 * @return Ciąg klucza kanonicznego lub `null` jeśli wartość nierozpoznana.
 */
fun fuelTypeKey(rawValue: String): String? {
    return when (rawValue.trim().lowercase(Locale.ROOT)) {
        "benzyna", "petrol", "gasoline", "benzin" -> "petrol"
        "diesel", "olej napedowy" -> "diesel"
        "hybryda", "hybrid", "phev", "hev" -> "hybrid"
        "elektryczny", "electric", "ev" -> "electric"
        else -> null
    }
}

/**
 * Normalizuje surowy ciąg skrzyni biegów do niezależnego od języka klucza kanonicznego.
 *
 * Rozpoznawane klucze: `"automatic"`, `"manual"`.
 * Obejmuje popularne skróty takie jak `"DSG"`, `"CVT"`, `"MT"`.
 *
 * @param rawValue Surowa wartość skrzyni biegów (bez rozróżniania wielkości liter, po polsku lub angielsku).
 * @return Ciąg klucza kanonicznego lub `null` jeśli wartość nierozpoznana.
 */
fun gearboxTypeKey(rawValue: String): String? {
    return when (rawValue.trim().lowercase(Locale.ROOT)) {
        "automatyczna", "automatic", "auto", "at", "cvt", "dct", "dsg" -> "automatic"
        "manualna", "manual", "man", "mt" -> "manual"
        else -> null
    }
}

