package com.example.otomotuzplus.data

import com.example.otomotuzplus.models.CarAd
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun uploadTestCar(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val testCar = CarAd(
            title = "Opel Corsa",
            priceText = "19 000 zł",
            locationText = "Kraków",
            year = "2019",
            mileageText = "98 400 km",
            fuelText = "Benzyna",
            gearboxText = "Manualna",
            engineCapacity = "1200 cm3",
            powerText = "100 KM",
            imageUrls = emptyList()
        )

        db.collection("listings")
            .add(testCar)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e: Exception -> onFailure(e) }
    }

    fun getAllCars(onSuccess: (List<CarAd>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("listings")
            .get()
            .addOnSuccessListener { result ->
                val carsList = mutableListOf<CarAd>()
                for (document in result) {
                    val car = document.toObject(CarAd::class.java).copy(id = document.id)
                    carsList.add(car)
                }
                onSuccess(carsList)
            }
            .addOnFailureListener { e: Exception ->
                onFailure(e)
            }
    }

    fun addCar(car: CarAd, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("listings")
            .add(car)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e: Exception -> onFailure(e) }
    }

    fun observeCars(onCarsChanged: (List<CarAd>) -> Unit) {
        db.collection("listings")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    println("BŁĄD NASŁUCHU FIREBASE: ${e?.message}")
                    return@addSnapshotListener
                }
                val cars = snapshot.documents.mapNotNull { doc ->
                    try {
                        val car = doc.toObject(CarAd::class.java)
                        car?.copy(id = doc.id)
                    } catch (ex: Exception) {
                        println("BŁĄD PARSOWANIA AUTA: ${ex.message}")
                        null
                    }
                }
                onCarsChanged(cars)
            }
    }

    fun uploadImages(uris: List<Uri>, onComplete: (List<String>) -> Unit) {
        if (uris.isEmpty()) {
            onComplete(emptyList())
            return
        }

        val uploadedUrls = mutableListOf<String>()
        var uploadCount = 0

        for (uri in uris) {
            val fileName = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("car_images/$fileName")

            storageRef.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    uploadedUrls.add(downloadUri.toString())
                }

                uploadCount++
                if (uploadCount == uris.size) {
                    onComplete(uploadedUrls)
                }
            }
        }
    }
    fun updateFcmToken(userId: String, token: String) {
        val tokenData = mapOf("fcmToken" to token)
        db.collection("users")
            .document(userId)
            .set(
                tokenData,
                com.google.firebase.firestore.SetOptions.merge()
            )
            .addOnSuccessListener {
                android.util.Log.d(
                    "FirebaseRepository",
                    "Token FCM zaktualizowany pomyślnie w Firestore."
                )
            }
    }
    fun sendLikeNotification(sellerId: String, carTitle: String) {
        val notification = hashMapOf(
            "toUser" to sellerId,
            "carTitle" to carTitle,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        db.collection("notifications").add(notification)
    }

    fun proceedWithSending(token: String?, carTitle: String) {
        if (!token.isNullOrEmpty()) {
            Thread {
                try {
                    android.util.Log.d("FCM_HTTP_V1", "Uruchamiam wątek wysyłania requestu HTTP...")
                    val internalTokenTask = com.google.firebase.auth.FirebaseAuth.getInstance()
                        .getAccessToken(false)
                    val accessToken = com.google.android.gms.tasks.Tasks.await(internalTokenTask).token

                    if (accessToken.isNullOrEmpty()) {
                        android.util.Log.e("FCM_HTTP_V1", "BŁĄD: Access Token z Firebase Auth jest pusty!")
                        return@Thread
                    }

                    val url = java.net.URL("https://fcm.googleapis.com/v1/projects/otomotuzplus/messages:send")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.setRequestProperty("Authorization", "Bearer $accessToken")
                    conn.doOutput = true

                    val jsonRequest = """
                        {
                          "message": {
                            "token": "$token",
                            "data": {
                              "title": "Ktoś polubił Twoje ogłoszenie!",
                              "body": "Twoje auto $carTitle spodobało się nowemu użytkownikowi."
                            }
                          }
                        }
                    """.trimIndent()

                    conn.outputStream.use { os ->
                        os.write(jsonRequest.toByteArray(Charsets.UTF_8))
                    }

                    val responseCode = conn.responseCode
                    android.util.Log.d("FCM_HTTP_V1", "KOD ODPOWIEDZI HTTP SERWERA GOOGLE: $responseCode")
                } catch (e: Exception) {
                    android.util.Log.e("FCM_HTTP_V1", "Błąd wysyłania przez HTTP v1", e)
                }
            }.start()
        } else {
            android.util.Log.w("FCM_HTTP_V1", "PRZERWANO: Nie udało się przypisać żadnego tokenu FCM dla tego sprzedawcy.")
        }
    }
}