/**
 * @file ListingCatalog.kt
 * @brief Katalogi statyczne: marki, modele, rodzaje paliwa; przykładowe dane.
 */
package com.example.otomotuzplus.ui.models

/**
 * Strukturalny model widoku ogłoszenia pojazdu używany w katalogu danych przykładowych.
 *
 * W przeciwieństwie do [com.example.otomotuzplus.models.CarAd], właściwości numeryczne
 * przechowywane jako właściwe typy (Int), ponieważ klasa nigdy nie przechodzi przez
 * Firestore — używana wyłącznie dla statycznych danych przykładowych w [sampleListings].
 *
 * @property name        Czytelny tytuł ogłoszenia.
 * @property brand       Nazwa marki producenta.
 * @property model       Oznaczenie modelu.
 * @property price       Cena wywoławcza w PLN.
 * @property year        Rok produkcji.
 * @property mileageKm   Przebieg w kilometrach.
 * @property fuelType    Klucz rodzaju paliwa (np. `"petrol"`, `"diesel"`, `"electric"`).
 * @property transmission Klucz rodzaju skrzyni biegów (`"automatic"` lub `"manual"`).
 * @property bodyType    Klucz typu nadwozia (np. `"suv"`, `"sedan"`, `"coupe"`).
 * @property driveType   Klucz napędu (`"fwd"`, `"rwd"`, `"awd"`).
 * @property carCondition Klucz stanu (`"new"` lub `"used"`).
 * @property location    Polska nazwa miasta.
 */
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

/**
 * Generuje złożony klucz ulubionych dla [CarListing].
 *
 * Format klucza `"<name>|<year>"` odzwierciedla format używany dla kluczy
 * ulubionych opartych na [CarAd] w [OtomotUZplusApp], umożliwiając
 * koegzystencję obu zbiorów na tej samej liście `favoriteCars`.
 */
fun CarListing.favoriteKey(): String = "$name|$year"

/**
 * Zwraca zakodowaną na stałe listę sześciu przykładowych ogłoszeń pojazdu
 * do podglądów UI i testów deweloperskich. Nie wyświetlana użytkownikowi produkcyjnie.
 *
 * @return Lista obiektów [CarListing] obejmująca paliwo benzynowe, diesel, hybrydę
 *   i elektryczne w różnych przedziałach cenowych i typach nadwozia.
 */
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

