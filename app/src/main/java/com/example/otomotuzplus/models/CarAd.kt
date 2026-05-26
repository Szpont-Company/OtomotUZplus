/**
 * @file CarAd.kt
 * @brief Model dokumentu Firestore reprezentujący ogłoszenie pojazdu.
 */
package com.example.otomotuzplus.models

/**
 * Reprezentuje pojedyncze ogłoszenie pojazdu przechowywane w Firestore.
 *
 * Instancje tworzone przez deserializację dokumentów Firestore za pomocą
 * `document.toObject(CarAd::class.java).copy(id = document.id)`.
 * Wszystkie pola tekstowe odzwierciedlają surowe dane użytkownika; wartości
 * numeryczne (cena, rok, przebieg) przechowywane jako ciągi znaków, aby
 * zachować częściowe lub dowolne dane wejściowe.
 *
 * @property id Identyfikator dokumentu Firestore; pusty ciąg do momentu odczytu.
 * @property title Tytuł ogłoszenia wpisany przez sprzedającego (np. "BMW M4 2022").
 * @property priceText Surowy ciąg ceny wpisany przez sprzedającego (np. "429 000").
 * @property locationText Czytelna nazwa miasta lub obszaru (np. "Kraków").
 * @property postalCode Polski kod pocztowy w formacie "XX-XXX"; używany do geokodowania.
 * @property latitude Szerokość geograficzna WGS-84 wyznaczona z [postalCode] przez Nominatim; 0.0 jeśli niedostępna.
 * @property longitude Długość geograficzna WGS-84 wyznaczona z [postalCode] przez Nominatim; 0.0 jeśli niedostępna.
 * @property year Rok produkcji jako czterocyfrowy ciąg (np. "2022").
 * @property mileageText Przebieg wpisany przez sprzedającego (np. "15000"); jednostka dodawana przez UI.
 * @property fuelText Surowa wartość rodzaju paliwa z Firestore (może być po polsku lub angielsku).
 * @property gearboxText Surowa wartość skrzyni biegów z Firestore (może być po polsku lub angielsku).
 * @property engineCapacity Pojemność silnika (np. "1998"); jednostka dodawana przez UI.
 * @property powerText Moc silnika (np. "306"); jednostka dodawana przez UI.
 * @property imageUrls Uporządkowana lista adresów URL pobierania zdjęć z Firebase Storage.
 * @property sellerId UID konta sprzedającego w Firebase Auth.
 * @property sellerEmail Adres e-mail konta sprzedającego.
 * @property phoneNumber Numer telefonu kontaktowego sprzedającego; ukryty za gestem potrząśnięcia.
 */
data class CarAd(
    val id: String = "",
    val title: String = "",
    val priceText: String = "",
    val locationText: String = "",
    val postalCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val year: String = "",
    val mileageText: String = "",
    val fuelText: String = "",
    val gearboxText: String = "",
    val engineCapacity: String = "",
    val powerText: String = "",
    val imageUrls: List<String> = emptyList(),
    val sellerId: String = "",
    val sellerEmail: String = "",
    val phoneNumber: String = ""
)