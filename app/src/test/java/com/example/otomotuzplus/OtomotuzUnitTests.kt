package com.example.otomotuzplus

import com.example.otomotuzplus.models.CarAd
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sqrt

/**
 * Zestaw testów jednostkowych dla aplikacji OtomotUZ++
 * Pokrywa testami modele danych, walidację formularzy oraz logikę sensorów.
 */
class OtomotuzUnitTests {

    // TESTY MODELU DANYCH (Firestore)

    @Test
    fun `CarAd creates with default empty values`() {
        val ad = CarAd()
        assertEquals("", ad.id)
        assertEquals("", ad.title)
        assertEquals("", ad.sellerEmail)
        assertTrue(ad.imageUrls.isEmpty())
    }

    @Test
    fun `CarAd saves correct seller information`() {
        val testUid = "T8eL35t55iN58OMsVOGSb9sqEEQ2"
        val testEmail = "test@otomotuz.pl"

        val ad = CarAd(sellerId = testUid, sellerEmail = testEmail)

        assertEquals("T8eL35t55iN58OMsVOGSb9sqEEQ2", ad.sellerId)
        assertEquals("test@otomotuz.pl", ad.sellerEmail)
    }

    @Test
    fun `CarAd assigns images list correctly`() {
        val images = listOf("url1.jpg", "url2.jpg")
        val ad = CarAd(imageUrls = images)

        assertEquals(2, ad.imageUrls.size)
        assertEquals("url1.jpg", ad.imageUrls[0])
    }

    // TESTY WALIDACJI FORMULARZA I TEKSTÓW

    @Test
    fun `Phone number validation accepts correct format`() {
        val phone = "+48 123 456 789"
        val filtered = phone.filter { it.isDigit() || it == '+' }
        assertEquals("+48123456789", filtered)
    }

    @Test
    fun `Phone number validation removes illegal characters`() {
        val phone = "500-123-123 xyz!"
        val filtered = phone.filter { it.isDigit() || it == '+' }
        assertEquals("500123123", filtered)
    }

    @Test
    fun `Price text removes letters and keeps only digits`() {
        val rawInput = "15 000 PLN"
        val filtered = rawInput.filter { it.isDigit() }
        assertEquals("15000", filtered)
    }

    @Test
    fun `Postal code formatter handles short input`() {
        val input = "65"
        val formatted = if (input.length <= 2) input else "${input.take(2)}-${input.drop(2)}"
        assertEquals("65", formatted)
    }

    @Test
    fun `Postal code formatter correctly adds dash`() {
        val input = "65001"
        val formatted = if (input.length <= 2) input else "${input.take(2)}-${input.drop(2)}"
        assertEquals("65-001", formatted)
    }

    // TESTY LOGIKI AKCELEROMETRU I FALLBACKU

    private fun calculateAcceleration(x: Float, y: Float, z: Float): Double {
        return sqrt((x * x + y * y + z * z).toDouble()) - 9.81
    }

    @Test
    fun `Phone is resting - no acceleration detected`() {
        // Telefon leży płasko na stole: X=0, Y=0, Z=9.81 (czysta grawitacja)
        val accel = calculateAcceleration(0f, 0f, 9.81f)
        assertTrue("Acceleration should be near 0", accel < 0.1)
    }

    @Test
    fun `Phone is shaken lightly - threshold not met`() {
        val accel = calculateAcceleration(3f, 4f, 10f)
        assertFalse("Should not unlock contact", accel > 5.0)
    }

    @Test
    fun `Phone is shaken firmly - contact unlocked`() {
        val accel = calculateAcceleration(15f, 10f, 5f)
        assertTrue("Should unlock contact", accel > 5.0)
    }

    @Test
    fun `Fallback logic works for old ads without email`() {
        val oldAd = CarAd(sellerId = "OLD_ID", sellerEmail = "")
        val displayedSeller = oldAd.sellerEmail.ifEmpty { oldAd.sellerId }

        assertEquals("OLD_ID", displayedSeller)
    }

    // TESTY (EDGE CASES I LOGIKA KOTLINA)

    @Test
    fun `Price text with only letters returns empty string`() {
        val rawInput = "PLN TYLKO"
        val filtered = rawInput.filter { it.isDigit() }
        assertEquals("Should strip all letters and return empty", "", filtered)
    }

    @Test
    fun `Phone number with completely empty input returns empty`() {
        val phone = ""
        val filtered = phone.filter { it.isDigit() || it == '+' }
        assertEquals("", filtered)
    }

    @Test
    fun `CarAd location coordinates initialize to zero`() {
        val ad = CarAd()
        assertEquals(0.0, ad.latitude, 0.001)
        assertEquals(0.0, ad.longitude, 0.001)
    }

    @Test
    fun `Acceleration math handles negative sensor vectors correctly`() {
        val accel = calculateAcceleration(-15f, -10f, 5f)
        assertTrue("Negative vectors should still trigger unlock due to absolute force", accel > 5.0)
    }

    @Test
    fun `Postal code formatter ignores letters completely`() {
        val input = "65A0B1"
        val digitsOnly = input.filter { it.isDigit() }.take(5)
        val formatted = if (digitsOnly.length <= 2) digitsOnly else "${digitsOnly.take(2)}-${digitsOnly.drop(2)}"
        assertEquals("65-01", formatted)
    }

    @Test
    fun `Title whitespace is trimmed correctly to save database space`() {
        val rawTitle = "   Toyota Avensis   "
        assertEquals("Toyota Avensis", rawTitle.trim())
    }

    @Test
    fun `Fallback logic handles completely blank email spaces as empty`() {
        val blankEmail = "   "
        val cleanEmail = blankEmail.trim()
        val oldAd = CarAd(sellerId = "ID_123", sellerEmail = cleanEmail)

        val displayedSeller = oldAd.sellerEmail.ifEmpty { oldAd.sellerId }
        assertEquals("ID_123", displayedSeller)
    }

    @Test
    fun `Data class copy modifies properties without changing original`() {
        val original = CarAd(title = "Auto", priceText = "1000")
        val modified = original.copy(priceText = "2000")

        assertEquals("Auto", modified.title)
        assertEquals("2000", modified.priceText)
        assertEquals("1000", original.priceText)
    }

    private fun String.withSuffix(suffix: String): String {
        val value = trim()
        return if (value.isEmpty()) "" else "$value $suffix"
    }

    @Test
    fun `Suffix helper avoids adding unit to empty string`() {
        val input = ""
        assertEquals("", input.withSuffix("km"))
    }

    @Test
    fun `Suffix helper correctly formats valid string`() {
        val input = "200000"
        assertEquals("200000 km", input.withSuffix("km"))
    }
}