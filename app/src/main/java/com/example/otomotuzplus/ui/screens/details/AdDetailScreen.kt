package com.example.otomotuzplus.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.otomotuzplus.models.CarAd
import com.example.otomotuzplus.ui.models.AppStrings
import com.example.otomotuzplus.ui.models.localizeFuelType
import com.example.otomotuzplus.ui.models.localizeGearboxType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdDetailScreen(
    car: CarAd,
    strings: AppStrings,
    onBackClick: () -> Unit
) {
    val title = car.title.trim()
    val location = car.locationText.trim()
    val price = car.priceText.trim()
    val seller = car.sellerId.trim()

    val specs = buildList {
        if (car.year.isNotBlank()) add(DetailSpec(Icons.Default.DateRange, car.year.trim(), strings.yearProduction))
        if (car.mileageText.isNotBlank()) add(DetailSpec(Icons.Default.Speed, "${car.mileageText.trim()} ${strings.unitKm}", strings.mileage))
        if (car.fuelText.isNotBlank()) add(DetailSpec(Icons.Default.LocalGasStation, localizeFuelType(car.fuelText, strings), strings.fuel))
        if (car.gearboxText.isNotBlank()) add(DetailSpec(Icons.Default.Settings, localizeGearboxType(car.gearboxText, strings), strings.gearbox))
        if (car.engineCapacity.isNotBlank()) add(DetailSpec(Icons.Default.Build, "${car.engineCapacity.trim()} ${strings.unitCm3}", strings.engineCapacityLabel))
        if (car.powerText.isNotBlank()) add(DetailSpec(Icons.Default.ElectricBolt, "${car.powerText.trim()} ${strings.unitPower}", strings.power))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = strings.back, tint = MaterialTheme.colorScheme.onBackground)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (car.imageUrls.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { car.imageUrls.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) { page ->
                AsyncImage(
                    model = car.imageUrls[page],
                    contentDescription = "Zdjęcie samochodu",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(text = strings.verifiedListing, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (title.isNotEmpty()) {
                        Text(text = title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    if (location.isNotEmpty()) {
                        Text(text = location, color = Color.Gray, fontSize = 14.sp)
                    }
                }

                if (price.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "$price ${strings.unitCurrency}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (specs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                specs.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { spec ->
                            SpecItem(icon = spec.icon, title = spec.title, subtitle = spec.subtitle, modifier = Modifier.weight(1f))
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    if (rowIndex < specs.chunked(2).lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (seller.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(strings.seller, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = seller,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private data class DetailSpec(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

@Composable
fun SpecItem(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}