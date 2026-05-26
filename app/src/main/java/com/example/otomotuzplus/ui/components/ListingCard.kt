/**
 * @file ListingCard.kt
 * @brief Karta ogłoszenia pojazdu używana na ekranach Home, Search i Favorites.
 */
package com.example.otomotuzplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otomotuzplus.ui.theme.BrandGold
import com.example.otomotuzplus.ui.theme.Slate400
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

/**
 * Model widoku dla pojedynczej karty ogłoszenia.
 *
 * Wszystkie pola tekstowe powinny już zawierać sufiksy jednostek
 * (np. `"98 400 km"`, `"19 000 zł"`), ponieważ sufiks rozwiązywany jest
 * z [AppStrings] przed zbudowaniem karty.
 *
 * @property title        Tytuł ogłoszenia (np. "Opel Corsa 1.2").
 * @property year         Ciąg roku produkcji.
 * @property mileageText  Przebieg z sufiksem jednostki.
 * @property fuelText     Zlokalizowana etykieta rodzaju paliwa.
 * @property bodyTypeText Zlokalizowana etykieta skrzyni biegów (ponownie użyta w układzie karty).
 * @property driveTypeText Ciąg pojemności silnika z sufiksem jednostki (ponownie użyty).
 * @property locationText Czytelna nazwa miasta lub obszaru.
 * @property priceText    Cena z sufiksem waluty.
 * @property isFavorite   Czy bieżący użytkownik dodał ogłoszenie do ulubionych.
 * @property onFavoriteClick Callback wywoływany gdy użytkownik kliknie ikonę serduszka.
 * @property coverImageUrl Opcjonalny URL Firebase Storage dla pierwszego zdjęcia ogłoszenia.
 */
data class ListingCardData(
    val title: String,
    val year: String,
    val mileageText: String,
    val fuelText: String,
    val bodyTypeText: String,
    val driveTypeText: String,
    val locationText: String,
    val priceText: String,
    val isFavorite: Boolean,
    val onFavoriteClick: () -> Unit,
    val coverImageUrl: String? = null
)

/**
 * Karta Material3 wyświetlająca skrócone podsumowanie ogłoszenia pojazdu.
 *
 * Karta pokazuje zdjęcie okładki (lub tło zastępcze), przycisk przełącznika
 * ulubionych w prawym górnym rogu i dwa wiersze metadanych pod tytułem.
 *
 * @param item     Dane ogłoszenia do wyświetlenia.
 * @param modifier Opcjonalny [Modifier] stosowany do zewnętrznej [Card].
 */
@Composable
fun ListingCard(
    item: ListingCardData,
    modifier: Modifier = Modifier
) {
    val title = item.title.trim()
    val metadataLine1 = listOf(item.year, item.mileageText, item.fuelText)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" • ")
    val metadataLine2 = listOf(item.bodyTypeText, item.driveTypeText, item.locationText)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" • ")
    val price = item.priceText.trim()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (!item.coverImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = item.coverImageUrl,
                        contentDescription = "Okładka ogłoszenia",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    IconButton(onClick = item.onFavoriteClick) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (item.isFavorite) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (metadataLine1.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = metadataLine1,
                        color = Slate400,
                        fontSize = 14.sp
                    )
                }

                if (metadataLine2.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = metadataLine2,
                        color = Slate400,
                        fontSize = 14.sp
                    )
                }

                if (price.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = price,
                        color = BrandGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    )
                }
            }
        }
    }
}

