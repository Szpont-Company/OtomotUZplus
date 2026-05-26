/**
 * @file FirebaseRepository.kt
 * @brief Warstwa danych – dostęp do Firestore, Firebase Storage i FCM.
 */
package com.example.otomotuzplus.data

/**
 * @file FirebaseRepository.kt
 * @brief Warstwa dostępu do danych dla Firestore i Firebase Storage.
 */

import com.example.otomotuzplus.models.CarAd
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

/**
 * Centralny obiekt dostępu do danych opakowujący kolekcje Firestore
 * (`listings` / `users` / `notifications`) oraz Firebase Storage (`car_images/`).
 *
 * Wszystkie operacje Firestore są asynchroniczne i przekazują wynik
 * przez lambdy zwrotne zamiast koroutyn, więc wywołujący na wątku głównym
 * nie musi uruchamiać zakresu koroutyn.
 */
class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    /**
     * Zapisuje zakodowane na stałe testowe ogłoszenie do Firestore.
     *
     * Przeznaczone wyłącznie do celów deweloperskich i debugowania.
     *
     * @param onSuccess Wywoływane po pomyślnym zapisaniu dokumentu.
     * @param onFailure Wywoływane z [Exception] gdy zapis się nie powiedzie.
     */
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

    /**
     * Pobiera jednorazowo wszystkie dokumenty z kolekcji `listings`.
     *
     * Preferuj [observeCars] dla ekranów UI wymagających aktualizacji na żywo.
     *
     * @param onSuccess Wywoływane z pełną listą obiektów [CarAd] po sukcesie.
     * @param onFailure Wywoływane z [Exception] w razie błędu.
     */
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

    /**
     * Dodaje nowy dokument ogłoszenia do kolekcji `listings`.
     *
     * @param car Obiekt [CarAd] do zapisania. Pole [CarAd.id] jest ignorowane;
     *   Firestore generuje identyfikator dokumentu automatycznie.
     * @param onSuccess Wywoływane po pomyślnym utworzeniu dokumentu.
     * @param onFailure Wywoływane z [Exception] w razie błędu.
     */
    fun addCar(car: CarAd, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("listings")
            .add(car)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e: Exception -> onFailure(e) }
    }

    /**
     * Dołącza nasłuchiwacz migawek Firestore w czasie rzeczywistym do kolekcji `listings`.
     *
     * Callback wywoływany natychmiast z aktualnym stanem, a następnie przy każdej
     * kolejnej zmianie. Nasłuchiwacz żyje przez cały czas istnienia komponentu;
     * wywołujący odpowiada za jego usunięcie (implementacja nie udostępnia uchwytu —
     * nasłuchiwacz powiązany jest z cyklem życia Activity przez `LaunchedEffect`).
     *
     * @param onCarsChanged Wywoływane na wątku głównym ze zaktualizowaną listą przy
     *   każdej zmianie kolekcji. Wadliwe dokumenty są pomijane.
     */
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

    /**
     * Przesyła partię lokalnych URI obrazów do Firebase Storage w katalogu `car_images/`.
     *
     * Każdy plik przechowywany pod nazwą UUID. Wszystkie przesyłania działają równolegle,
     * a [onComplete] wywoływane jest gdy wszystkie zakończą działanie (sukces lub błąd).
     * Nieudane przesyłania poszczególnych plików są pomijane na liście wynikowej.
     *
     * @param uris Lokalne URI treści (zdjęcia z galerii lub zrobione aparatem przez `FileProvider`).
     * @param onComplete Wywoływane z listą adresów URL pobierania po zakończeniu wszystkich
     *   przesyłań. Zwraca pustą listę gdy [uris] jest puste.
     */
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
    /**
     * Scala bieżący token rejestracji FCM z dokumentem użytkownika w Firestore.
     *
     * Używa `SetOptions.merge()` aby zachować pozostałe pola dokumentu `users`.
     * Wywoływane przez [MyFirebaseMessagingService.onNewToken] przy każdym odświeżeniu tokenu.
     *
     * @param userId UID zalogowanego użytkownika w Firebase Auth.
     * @param token  Nowy token rejestracji FCM.
     */
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
    /**
     * Zapisuje dokument powiadomienia o polubieniu do kolekcji `notifications`.
     *
     * `MainActivity` nasłuchuje tej kolekcji przez listener migawek i wyświetla
     * lokalne powiadomienie gdy nowy dokument zostanie dodany dla bieżącego użytkownika.
     *
     * @param sellerId UID sprzedającego posiadającego polubione ogłoszenie w Firebase Auth.
     * @param carTitle Tytuł polubionego ogłoszenia; umieszczony w treści powiadomienia.
     */
    fun sendLikeNotification(sellerId: String, carTitle: String) {
        val notification = hashMapOf(
            "toUser" to sellerId,
            "carTitle" to carTitle,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        db.collection("notifications").add(notification)
    }

    /**
     * Wysyła powiadomienie push FCM bezpośrednio przez API HTTP v1 w wątku w tle.
     *
     * Pobiera krótkotrwały token dostępu OAuth2 z Firebase Auth, następnie wykonuje
     * żądanie HTTP POST do `fcm.googleapis.com`. Operacja typu fire-and-forget;
     * wynik tylko logowany, nigdy nie przekazywany do UI.
     *
     * @param token Token rejestracji FCM urządzenia odbiorcy. Jeśli null lub pusty,
     *   wywołanie jest ignorowane.
     * @param carTitle Tytuł polubionego ogłoszenia; osadzony w treści wiadomości.
     */
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