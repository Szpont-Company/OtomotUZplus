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
}