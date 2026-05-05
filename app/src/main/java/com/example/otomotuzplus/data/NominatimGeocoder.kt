package com.example.otomotuzplus.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

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
