/**
 * @file NominatimGeocoder.kt
 * @brief Funkcje zawieszające do geokodowania polskich lokalizacji
 *   przez REST API OpenStreetMap Nominatim.
 *
 * Obie funkcje działają na [kotlinx.coroutines.Dispatchers.IO] i zwracają
 * `null` przy błędzie sieci lub pustym wyniku. Nagłówek User-Agent ustawiony
 * na `OtomotUZplus/1.0` zgodnie z polityką użytkowania Nominatim.
 */
package com.example.otomotuzplus.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Zamienia polski kod pocztowy na parę współrzędnych WGS-84.
 *
 * Separator myślnika usuwany przed zapytaniem, więc akceptowane są formaty
 * "00000" i "00-000".
 *
 * @param postalCode Polski kod pocztowy z separatorem lub bez.
 * @return [Pair] (szerokość, długość) lub `null` jeśli kod nie może być
 *   rozpoznany lub wystąpił błąd sieci.
 */
suspend fun geocodePostalCode(postalCode: String): Pair<Double, Double>? =
    withContext(Dispatchers.IO) {
        val clean = postalCode.replace("-", "")
        val conn = URL("https://nominatim.openstreetmap.org/search?postalcode=$clean&country=pl&format=json&limit=1")
            .openConnection() as HttpURLConnection
        try {
            conn.setRequestProperty("User-Agent", "OtomotUZplus/1.0")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val json = JSONArray(conn.inputStream.bufferedReader().use { it.readText() })
            if (json.length() > 0) {
                val obj = json.getJSONObject(0)
                Pair(obj.getString("lat").toDouble(), obj.getString("lon").toDouble())
            } else null
        } catch (_: Exception) { null
        } finally { conn.disconnect() }
    }

/**
 * Zamienia polską nazwę miasta na parę współrzędnych WGS-84.
 *
 * Wyszukiwanie ograniczone do Polski (`countrycodes=pl`). Nazwa miasta
 * kodowana jako URL przed wysłaniem zapytania.
 *
 * @param city Czytelna nazwa miasta (np. "Warszawa", "Kraków").
 * @return [Pair] (szerokość, długość) lub `null` jeśli miasto nie zostało
 *   znalezione lub wystąpił błąd sieci.
 */
suspend fun geocodeCity(city: String): Pair<Double, Double>? =
    withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(city, "UTF-8")
        val conn = URL("https://nominatim.openstreetmap.org/search?q=$encoded&countrycodes=pl&format=json&limit=1")
            .openConnection() as HttpURLConnection
        try {
            conn.setRequestProperty("User-Agent", "OtomotUZplus/1.0")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val json = JSONArray(conn.inputStream.bufferedReader().use { it.readText() })
            if (json.length() > 0) {
                val obj = json.getJSONObject(0)
                Pair(obj.getString("lat").toDouble(), obj.getString("lon").toDouble())
            } else null
        } catch (_: Exception) { null
        } finally { conn.disconnect() }
    }
