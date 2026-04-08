package com.example.otomotuzplus.ui.models

data class CarListing(
    val name: String,
    val brand: String,
    val model: String,
    val price: Int,
    val year: Int,
    val mileageKm: Int,
    val fuelType: String,
    val transmission: String,
    val bodyType: String,
    val driveType: String,
    val carCondition: String,
    val location: String
)

fun CarListing.favoriteKey(): String = "$name|$year"

fun sampleListings(): List<CarListing> = listOf(
    CarListing(
        name = "Audi RS6 Avant",
        brand = "Audi",
        model = "RS6",
        price = 690000,
        year = 2023,
        mileageKm = 8500,
        fuelType = "petrol",
        transmission = "automatic",
        bodyType = "wagon",
        driveType = "awd",
        carCondition = "used",
        location = "Warszawa"
    ),
    CarListing(
        name = "BMW M4 Competition xDrive",
        brand = "BMW",
        model = "M4",
        price = 429000,
        year = 2022,
        mileageKm = 15000,
        fuelType = "petrol",
        transmission = "automatic",
        bodyType = "coupe",
        driveType = "awd",
        carCondition = "used",
        location = "Krakow"
    ),
    CarListing(
        name = "Mercedes C 300",
        brand = "Mercedes",
        model = "C 300",
        price = 268000,
        year = 2021,
        mileageKm = 39000,
        fuelType = "hybrid",
        transmission = "automatic",
        bodyType = "sedan",
        driveType = "rwd",
        carCondition = "used",
        location = "Wroclaw"
    ),
    CarListing(
        name = "Porsche 911 Carrera S",
        brand = "Porsche",
        model = "911",
        price = 585000,
        year = 2020,
        mileageKm = 42000,
        fuelType = "petrol",
        transmission = "automatic",
        bodyType = "coupe",
        driveType = "rwd",
        carCondition = "used",
        location = "Poznan"
    ),
    CarListing(
        name = "Volkswagen Tiguan 2.0 TDI",
        brand = "VW",
        model = "Tiguan",
        price = 156000,
        year = 2022,
        mileageKm = 27000,
        fuelType = "diesel",
        transmission = "automatic",
        bodyType = "suv",
        driveType = "fwd",
        carCondition = "used",
        location = "Gdansk"
    ),
    CarListing(
        name = "Tesla Model Y Performance",
        brand = "Tesla",
        model = "Model Y",
        price = 312000,
        year = 2024,
        mileageKm = 5000,
        fuelType = "electric",
        transmission = "automatic",
        bodyType = "suv",
        driveType = "awd",
        carCondition = "new",
        location = "Lodz"
    )
)

