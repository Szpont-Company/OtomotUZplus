package com.example.otomotuzplus.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otomotuzplus.ui.components.ScreenHeader
import com.example.otomotuzplus.ui.components.ListingCard
import com.example.otomotuzplus.ui.components.ListingCardData
import com.example.otomotuzplus.ui.models.AppStrings
import com.example.otomotuzplus.ui.models.localizeFuelType
import com.example.otomotuzplus.ui.models.localizeGearboxType
import com.example.otomotuzplus.ui.models.sampleListings
import com.example.otomotuzplus.ui.theme.BrandGold
import com.example.otomotuzplus.ui.theme.DarkSlate800
import com.example.otomotuzplus.ui.theme.Slate400

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    strings: AppStrings,
    carsFromDb: List<com.example.otomotuzplus.models.CarAd>,
    modifier: Modifier = Modifier,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToAdd: () -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    onBrandSelect: (String) -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    favoriteCars: List<String> = emptyList(),
    onFavoriteToggle: (String) -> Unit = {},
    onCarClick: (com.example.otomotuzplus.models.CarAd) -> Unit = {}
) {
    var query by rememberSaveable { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current


    val brands = remember {
        listOf("Porsche", "BMW", "Mercedes", "Audi", "VW")
    }

    val arrivals = remember(carsFromDb, strings) {
        carsFromDb.map { listing ->
            ListingCardData(
                title = listing.title,
                year = listing.year,
                mileageText = listing.mileageText.withSuffix(strings.unitKm),
                fuelText = localizeFuelType(listing.fuelText, strings),
                bodyTypeText = localizeGearboxType(listing.gearboxText, strings),
                driveTypeText = listing.engineCapacity.withSuffix(strings.unitCm3),
                locationText = listing.locationText,
                priceText = listing.priceText.withSuffix(strings.unitCurrency),
                coverImageUrl = listing.imageUrls.firstOrNull(),
                isFavorite = false,
                onFavoriteClick = {}
            )
        }
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            ScreenHeader(title = "OtoMotUZ++") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 1.dp
                ) {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                placeholder = strings.searchPlaceholder,
                onSubmit = onSearchSubmit
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ActionTile(
                    modifier = Modifier.weight(1f),
                    title = strings.buy,
                    subtitle = strings.findCar,
                    icon = Icons.Default.ShoppingCart,
                    background = BrandGold,
                    foreground = Color(0xFF111111),
                    onClick = onNavigateToSearch
                )
                ActionTile(
                    modifier = Modifier.weight(1f),
                    title = strings.sell,
                    subtitle = strings.listYourCar,
                    icon = Icons.Default.AddCircle,
                    background = DarkSlate800,
                    foreground = Color.White,
                    onClick = onNavigateToAdd
                )
            }
        }

        item {
            SectionHeader(
                title = strings.popularBrands,
                actionLabel = strings.seeAll,
                onActionClick = onSeeAllClick
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(brands) { brand ->
                    BrandPlaceholderTile(
                        brand = brand,
                        onClick = { onBrandSelect(brand) }
                    )
                }
            }
        }

        item {
            SectionHeader(title = strings.freshArrivals, actionLabel = null)
        }

        itemsIndexed(arrivals) { index, carData ->
            val originalCarFromDb = carsFromDb[index]

            val carKey = "${originalCarFromDb.id}|${originalCarFromDb.title}"

            ListingCard(
                item = carData.copy(
                    isFavorite = favoriteCars.contains(carKey),
                    onFavoriteClick = { onFavoriteToggle(carKey) }
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable {
                        onCarClick(originalCarFromDb)
                    }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BrandPlaceholderTile(
    brand: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.size(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = brand.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = brand,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onSubmit: (String) -> Unit
) {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp
    val placeholderSize = if (screenWidth < 360) 12.sp else 14.sp
    val textSize = if (screenWidth < 360) 13.sp else 15.sp

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text(text = placeholder, color = Slate400, fontSize = placeholderSize) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = textSize),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { onSubmit(query.trim()) }
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = BrandGold
        )
    )
}

@Composable
private fun ActionTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    foreground: Color,
    onClick: () -> Unit = {}
) {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp

    val tileHeight: androidx.compose.ui.unit.Dp
    val padding: androidx.compose.ui.unit.Dp
    val titleSize: androidx.compose.ui.unit.TextUnit
    val subtitleSize: androidx.compose.ui.unit.TextUnit
    val spacingIconToTitle: androidx.compose.ui.unit.Dp
    val spacingTitleToSubtitle: androidx.compose.ui.unit.Dp

    when {
        screenWidth < 360 -> {
            // Very small screens
            tileHeight = 85.dp
            padding = 8.dp
            titleSize = 10.sp
            subtitleSize = 14.sp
            spacingIconToTitle = 4.dp
            spacingTitleToSubtitle = 2.dp
        }
        screenWidth < 380 -> {
            // Small screens (Realme RMX2202 ~360-380dp)
            tileHeight = 100.dp
            padding = 10.dp
            titleSize = 11.sp
            subtitleSize = 15.sp
            spacingIconToTitle = 6.dp
            spacingTitleToSubtitle = 2.dp
        }
        screenWidth < 420 -> {
            // Medium-small (Nothing Phone 2 ~380-410dp)
            tileHeight = 125.dp
            padding = 12.dp
            titleSize = 13.sp
            subtitleSize = 18.sp
            spacingIconToTitle = 8.dp
            spacingTitleToSubtitle = 2.dp
        }
        screenWidth < 600 -> {
            // Medium screens
            tileHeight = 120.dp
            padding = 11.dp
            titleSize = 14.sp
            subtitleSize = 19.sp
            spacingIconToTitle = 8.dp
            spacingTitleToSubtitle = 2.dp
        }
        else -> {
            // Large screens (tablets)
            tileHeight = 150.dp
            padding = 14.dp
            titleSize = 13.sp
            subtitleSize = 18.sp
            spacingIconToTitle = 10.dp
            spacingTitleToSubtitle = 3.dp
        }
    }

    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = tileHeight),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = CircleShape,
                color = foreground.copy(alpha = 0.18f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = foreground,
                    modifier = Modifier.padding(6.dp)
                )
            }
            Spacer(modifier = Modifier.height(spacingIconToTitle))
            Column {
                Text(
                    text = title,
                    color = foreground.copy(alpha = 0.72f),
                    fontSize = titleSize,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(spacingTitleToSubtitle))
                Text(
                    text = subtitle,
                    color = foreground,
                    fontSize = subtitleSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String?,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
        if (actionLabel != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionLabel,
                    color = BrandGold,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun String.withSuffix(suffix: String): String {
    val value = trim()
    return if (value.isEmpty()) "" else "$value $suffix"
}




