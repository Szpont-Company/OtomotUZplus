package com.example.otomotuzplus.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.otomotuzplus.models.CarAd
import com.example.otomotuzplus.ui.components.ListingCard
import com.example.otomotuzplus.ui.components.ListingCardData
import com.example.otomotuzplus.ui.components.ScreenHeader
import com.example.otomotuzplus.ui.models.AppStrings
import com.example.otomotuzplus.ui.models.localizeFuelType
import com.example.otomotuzplus.ui.models.localizeGearboxType
import com.example.otomotuzplus.ui.theme.Slate400

@Composable
fun FavoritesScreen(
    strings: AppStrings,
    favoriteCars: List<String>,
    onFavoriteToggle: (String) -> Unit,
    allCarsFromDb: List<CarAd>,
    onCarClick: (CarAd) -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteItems = allCarsFromDb.filter { car ->
        val key = "${car.id}|${car.title}"
        favoriteCars.contains(key)
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
                text = strings.resultsCount.format(favoriteItems.size),
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Slate400,
                fontWeight = FontWeight.Medium
            )
        }

        if (favoriteItems.isEmpty()) {
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
            items(favoriteItems) { car ->
                val carKey = "${car.id}|${car.title}"

                ListingCard(
                    item = ListingCardData(
                        title = car.title,
                        year = car.year,
                        mileageText = car.mileageText.withSuffix(strings.unitKm),
                        fuelText = localizeFuelType(car.fuelText, strings),
                        bodyTypeText = localizeGearboxType(car.gearboxText, strings),
                        driveTypeText = car.engineCapacity.withSuffix(strings.unitCm3),
                        locationText = car.locationText,
                        priceText = car.priceText.withSuffix(strings.unitCurrency),
                        coverImageUrl = car.imageUrls.firstOrNull(),
                        isFavorite = true,
                        onFavoriteClick = { onFavoriteToggle(carKey) }
                    ),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable { onCarClick(car) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

private fun String.withSuffix(suffix: String): String {
    val value = trim()
    return if (value.isEmpty()) "" else "$value $suffix"
}

