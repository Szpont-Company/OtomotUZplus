package com.example.otomotuzplus.data

import com.example.otomotuzplus.models.CarAd
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

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
            imageUrl = ""
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
}