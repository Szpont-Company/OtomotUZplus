package com.example.otomotuzplus.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import com.example.otomotuzplus.ui.components.ListingCard
import com.example.otomotuzplus.ui.components.ListingCardData
import com.example.otomotuzplus.ui.components.ScreenHeader
import com.example.otomotuzplus.ui.models.AppStrings
import com.example.otomotuzplus.ui.models.CarListing
import com.example.otomotuzplus.ui.models.favoriteKey
import com.example.otomotuzplus.ui.models.sampleListings
import com.example.otomotuzplus.ui.theme.BrandGold
import com.example.otomotuzplus.ui.theme.Slate400
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun FavoritesScreen(
    strings: AppStrings,
    favoriteCars: List<String>,
    onFavoriteToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val detailsByKey = sampleListings().associateBy { it.favoriteKey() }
    val items = favoriteCars.asReversed().map { key ->
        detailsByKey[key]?.toFavoriteItemUi() ?: parseFavoriteKey(key)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            ScreenHeader(title = strings.favorites)
        }
        item {
            Text(
                text = strings.resultsCount.format(items.size),
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Slate400,
                fontWeight = FontWeight.Medium
            )
        }

        if (items.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = strings.noSearchResults,
                        modifier = Modifier.padding(20.dp),
                        color = Slate400
                    )
                }
            }
        } else {
            items(items) { item ->
                ListingCard(
                    item = ListingCardData(
                        title = item.name,
                        year = item.year,
                        mileageText = "${item.mileageKm / 1000}k km",
                        fuelText = fuelLabel(item.fuelType, strings),
                        bodyTypeText = bodyTypeLabel(item.bodyType, strings),
                        driveTypeText = driveTypeLabel(item.driveType, strings),
                        locationText = item.location,
                        priceText = formatPrice(item.price),
                        isFavorite = true,
                        onFavoriteClick = { onFavoriteToggle(item.key) }
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

private data class FavoriteItemUi(
    val key: String,
    val name: String,
    val year: String,
    val mileageKm: Int,
    val fuelType: String,
    val transmission: String,
    val bodyType: String,
    val driveType: String,
    val location: String,
    val price: Int
)

private const val FUEL_PETROL = "petrol"
private const val FUEL_DIESEL = "diesel"
private const val FUEL_HYBRID = "hybrid"
private const val FUEL_ELECTRIC = "electric"

private const val TRANSMISSION_AUTOMATIC = "automatic"

private const val BODY_SEDAN = "sedan"
private const val BODY_SUV = "suv"
private const val BODY_COUPE = "coupe"
private const val BODY_WAGON = "wagon"

private const val DRIVE_FWD = "fwd"
private const val DRIVE_RWD = "rwd"
private const val DRIVE_AWD = "awd"

private fun parseFavoriteKey(key: String): FavoriteItemUi {
    val parts = key.split("|")
    val name = parts.getOrNull(0).orEmpty().ifBlank { "-" }
    val year = parts.getOrNull(1).orEmpty().ifBlank { "-" }
    return FavoriteItemUi(
        key = key,
        name = name,
        year = year,
        mileageKm = 0,
        fuelType = "",
        transmission = "",
        bodyType = "",
        driveType = "",
        location = "-",
        price = 0
    )
}

private fun CarListing.toFavoriteItemUi(): FavoriteItemUi = FavoriteItemUi(
    key = "$name|$year",
    name = name,
    year = year.toString(),
    mileageKm = mileageKm,
    fuelType = fuelType,
    transmission = transmission,
    bodyType = bodyType,
    driveType = driveType,
    location = location,
    price = price
)

private fun fuelLabel(value: String, strings: AppStrings): String = when (value.lowercase(Locale.getDefault())) {
    FUEL_PETROL -> strings.fuelPetrol
    FUEL_DIESEL -> strings.fuelDiesel
    FUEL_HYBRID -> strings.fuelHybrid
    FUEL_ELECTRIC -> strings.fuelElectric
    else -> "-"
}

private fun transmissionLabel(value: String, strings: AppStrings): String = when (value.lowercase(Locale.getDefault())) {
    TRANSMISSION_AUTOMATIC -> strings.transmissionAutomatic
    else -> "-"
}

private fun bodyTypeLabel(value: String, strings: AppStrings): String = when (value.lowercase(Locale.getDefault())) {
    BODY_SEDAN -> strings.bodySedan
    BODY_SUV -> strings.bodySuv
    BODY_COUPE -> strings.bodyCoupe
    BODY_WAGON -> strings.bodyWagon
    else -> "-"
}

private fun driveTypeLabel(value: String, strings: AppStrings): String = when (value.lowercase(Locale.getDefault())) {
    DRIVE_FWD -> strings.driveFwd
    DRIVE_RWD -> strings.driveRwd
    DRIVE_AWD -> strings.driveAwd
    else -> "-"
}

private fun formatPrice(price: Int): String {
    if (price <= 0) return "-"
    val symbols = DecimalFormatSymbols(Locale.forLanguageTag("pl-PL")).apply {
        groupingSeparator = ' '
    }
    val formatter = DecimalFormat("#,###", symbols)
    return "${formatter.format(price)} zl"
}




