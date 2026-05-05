package com.example.otomotuzplus.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otomotuzplus.data.geocodeCity
import com.example.otomotuzplus.models.CarAd
import com.example.otomotuzplus.ui.components.ListingCard
import com.example.otomotuzplus.ui.components.ListingCardData
import com.example.otomotuzplus.ui.components.ScreenHeader
import com.example.otomotuzplus.ui.models.AppStrings
import com.example.otomotuzplus.ui.models.fuelTypeKey
import com.example.otomotuzplus.ui.models.gearboxTypeKey
import com.example.otomotuzplus.ui.models.localizeFuelType
import com.example.otomotuzplus.ui.models.localizeGearboxType
import com.example.otomotuzplus.ui.theme.BrandGold
import com.example.otomotuzplus.ui.theme.Slate400
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

enum class SortOption {
    NAME_ASC, NAME_DESC, PRICE_ASC, PRICE_DESC, YEAR_DESC, MILEAGE_ASC
}

data class SearchFilters(
    val brand: String? = null,
    val modelQuery: String = "",
    val minPrice: String = "",
    val maxPrice: String = "",
    val minYear: String = "",
    val maxYear: String = "",
    val maxMileage: String = "",
    val fuelType: String? = null,
    val transmission: String? = null,
    val location: String = "",
    val radiusKm: Int? = null
)

private enum class ActiveFilterKey {
    QUERY, BRAND, MODEL, PRICE_RANGE, YEAR_RANGE, MAX_MILEAGE, FUEL, TRANSMISSION, LOCATION, CLUSTER_AREA
}

private data class ActiveFilterItem(val key: ActiveFilterKey, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    strings: AppStrings,
    modifier: Modifier = Modifier,
    initialQuery: String? = null,
    initialBrand: String? = null,
    initialShowFilters: Boolean? = null,
    favoriteCars: List<String> = emptyList(),
    onFavoriteToggle: (String) -> Unit = {},
    allCarsFromDb: List<CarAd>,
    onCarClick: (CarAd) -> Unit,
    onInitialFiltersConsumed: () -> Unit = {}
) {
    var query by rememberSaveable { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var showSortMenu by rememberSaveable { mutableStateOf(false) }
    var sortOption by rememberSaveable { mutableStateOf(SortOption.NAME_ASC) }
    var filters by remember { mutableStateOf(SearchFilters()) }
    var showMapView by rememberSaveable { mutableStateOf(false) }

    var geocodedCenter by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var clusterCenter by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var clusterRadiusKm by remember { mutableStateOf<Double?>(null) }

    var visibleCars by remember { mutableStateOf(allCarsFromDb) }

    LaunchedEffect(initialQuery, initialBrand, initialShowFilters) {
        val hasPresetQuery = !initialQuery.isNullOrBlank()
        val hasPresetBrand = !initialBrand.isNullOrBlank()
        if (hasPresetQuery || hasPresetBrand || initialShowFilters != null) {
            query = initialQuery.orEmpty()
            filters = SearchFilters(brand = initialBrand)
            sortOption = SortOption.NAME_ASC
            showFilters = initialShowFilters ?: false
            onInitialFiltersConsumed()
        }
    }

    LaunchedEffect(filters.location, filters.radiusKm) {
        geocodedCenter = null
        clusterCenter = null
        clusterRadiusKm = null
        if (filters.location.isNotBlank() && filters.radiusKm != null) {
            delay(400)
            geocodedCenter = geocodeCity(filters.location)
        }
    }

    LaunchedEffect(query, filters, sortOption, allCarsFromDb, geocodedCenter, clusterCenter, clusterRadiusKm) {
        visibleCars = allCarsFromDb
            .filter { it.matches(query, filters, geocodedCenter, clusterCenter, clusterRadiusKm) }
            .sortedWith(sortComparator(sortOption))
    }

    val activeFilters = remember(query, filters, strings, clusterCenter, clusterRadiusKm) {
        buildActiveFilterItems(query, filters, strings, clusterCenter, clusterRadiusKm)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ScreenHeader(
            title = strings.search,
            trailingWidth = 176.dp
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                IconButton(onClick = {
                    val newShowMap = !showMapView
                    showMapView = newShowMap
                    if (newShowMap) showFilters = false
                }) {
                    Icon(
                        imageVector = if (showMapView) Icons.Default.ViewList else Icons.Default.Map,
                        contentDescription = if (showMapView) strings.listViewToggle else strings.mapViewToggle,
                        tint = if (showMapView) BrandGold else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = strings.filters,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = strings.sortBy,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    sortOptions(strings).forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                sortOption = option.value
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        if (!showMapView) {
            Spacer(modifier = Modifier.height(8.dp))

            val config = LocalConfiguration.current
            val screenWidth = config.screenWidthDp
            val placeholderSize = if (screenWidth < 360) 12.sp else 14.sp
            val textSize = if (screenWidth < 360) 13.sp else 15.sp

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = {
                    Text(strings.searchPlaceholder, color = Slate400, fontSize = placeholderSize)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = textSize),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = BrandGold
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.resultsCount.format(visibleCars.size),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = {
                    query = ""
                    filters = SearchFilters()
                    sortOption = SortOption.NAME_ASC
                    geocodedCenter = null
                    clusterCenter = null
                    clusterRadiusKm = null
                }) {
                    Text(strings.resetFilters)
                }
            }
        }

        if (showFilters) {
            FiltersCard(
                strings = strings,
                filters = filters,
                onFiltersChange = { filters = it }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (activeFilters.isNotEmpty()) {
            ActiveFiltersRow(
                filters = activeFilters,
                onRemove = { key ->
                    when (key) {
                        ActiveFilterKey.QUERY -> query = ""
                        ActiveFilterKey.BRAND -> filters = filters.copy(brand = null)
                        ActiveFilterKey.MODEL -> filters = filters.copy(modelQuery = "")
                        ActiveFilterKey.PRICE_RANGE -> filters = filters.copy(minPrice = "", maxPrice = "")
                        ActiveFilterKey.YEAR_RANGE -> filters = filters.copy(minYear = "", maxYear = "")
                        ActiveFilterKey.MAX_MILEAGE -> filters = filters.copy(maxMileage = "")
                        ActiveFilterKey.FUEL -> filters = filters.copy(fuelType = null)
                        ActiveFilterKey.TRANSMISSION -> filters = filters.copy(transmission = null)
                        ActiveFilterKey.LOCATION -> {
                            filters = filters.copy(location = "", radiusKm = null)
                            geocodedCenter = null
                        }
                        ActiveFilterKey.CLUSTER_AREA -> {
                            clusterCenter = null
                            clusterRadiusKm = null
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (showMapView) {
            Spacer(modifier = Modifier.height(8.dp))
            CarMapView(
                cars = visibleCars,
                onSingleCarTap = onCarClick,
                onClusterTap = { clusterCars ->
                    val avgLat = clusterCars.map { it.latitude }.average()
                    val avgLng = clusterCars.map { it.longitude }.average()
                    val maxDistKm = clusterCars.maxOf {
                        haversineKm(avgLat, avgLng, it.latitude, it.longitude)
                    }
                    clusterCenter = Pair(avgLat, avgLng)
                    clusterRadiusKm = maxOf(maxDistKm * 1.5, 1.0)
                    showMapView = false
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (visibleCars.isEmpty()) {
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
                    items(visibleCars) { car ->
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
                                isFavorite = favoriteCars.contains(carKey),
                                coverImageUrl = car.imageUrls.firstOrNull(),
                                onFavoriteClick = { onFavoriteToggle(carKey) }
                            ),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable { onCarClick(car) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(10.dp)) }
            }
        }
    }
}

@Composable
private fun ActiveFiltersRow(filters: List<ActiveFilterItem>, onRemove: (ActiveFilterKey) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = filter.label,
                        modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = { onRemove(filter.key) }) {
                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltersCard(strings: AppStrings, filters: SearchFilters, onFiltersChange: (SearchFilters) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = strings.filters, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 17.sp)

            LabeledInput(label = strings.brand, value = filters.brand ?: "", onValueChange = { onFiltersChange(filters.copy(brand = it.ifBlank { null })) }, placeholder = "np. BMW")
            LabeledInput(label = strings.model, value = filters.modelQuery, onValueChange = { onFiltersChange(filters.copy(modelQuery = it)) }, placeholder = "np. M4")

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LabeledInput(label = strings.minPrice, value = filters.minPrice, onValueChange = { onFiltersChange(filters.copy(minPrice = it.filter(Char::isDigit))) }, modifier = Modifier.weight(1f))
                LabeledInput(label = strings.maxPrice, value = filters.maxPrice, onValueChange = { onFiltersChange(filters.copy(maxPrice = it.filter(Char::isDigit))) }, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LabeledInput(label = strings.minYear, value = filters.minYear, onValueChange = { onFiltersChange(filters.copy(minYear = it.filter(Char::isDigit).take(4))) }, modifier = Modifier.weight(1f))
                LabeledInput(label = strings.maxYear, value = filters.maxYear, onValueChange = { onFiltersChange(filters.copy(maxYear = it.filter(Char::isDigit).take(4))) }, modifier = Modifier.weight(1f))
            }

            LabeledInput(label = strings.maxMileage, value = filters.maxMileage, onValueChange = { onFiltersChange(filters.copy(maxMileage = it.filter(Char::isDigit))) }, placeholder = "np. 60000")
            LabeledInput(label = strings.location, value = filters.location, onValueChange = {
                onFiltersChange(filters.copy(location = it, radiusKm = if (it.isBlank()) null else filters.radiusKm))
            }, placeholder = "np. Warszawa")

            if (filters.location.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = strings.searchRadius,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val radiusOptions = listOf<Int?>(null, 25, 50, 100, 200)
                        items(radiusOptions.size) { i ->
                            val km = radiusOptions[i]
                            FilterChip(
                                selected = filters.radiusKm == km,
                                onClick = {
                                    onFiltersChange(filters.copy(radiusKm = if (filters.radiusKm == km) null else km))
                                },
                                label = { Text(if (km == null) strings.radiusAny else "$km km") }
                            )
                        }
                    }
                }
            }

            SingleSelectChipRow(
                label = strings.fuelType,
                options = listOf(strings.fuelPetrol, strings.fuelDiesel, strings.fuelElectric, strings.fuelHybrid),
                selected = filters.fuelType,
                onSelectionChange = { onFiltersChange(filters.copy(fuelType = it)) }
            )
            SingleSelectChipRow(
                label = strings.transmission,
                options = listOf(strings.transmissionAutomatic, strings.transmissionManual),
                selected = filters.transmission,
                onSelectionChange = { onFiltersChange(filters.copy(transmission = it)) }
            )
        }
    }
}

@Composable
private fun SingleSelectChipRow(label: String, options: List<String>, selected: String?, onSelectionChange: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { if (selected == option) onSelectionChange(null) else onSelectionChange(option) },
                    label = { Text(option) },
                    leadingIcon = { if (selected == option) { Icon(Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.primary) } }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledInput(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, placeholder: String = "") {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = modifier.fillMaxWidth(), label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) { Text(placeholder) } }, singleLine = true, shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.outline, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

private data class SortLabel(val value: SortOption, val label: String)

private fun sortOptions(strings: AppStrings): List<SortLabel> = listOf(
    SortLabel(SortOption.NAME_ASC, strings.sortNameAsc), SortLabel(SortOption.NAME_DESC, strings.sortNameDesc),
    SortLabel(SortOption.PRICE_ASC, strings.sortPriceAsc), SortLabel(SortOption.PRICE_DESC, strings.sortPriceDesc),
    SortLabel(SortOption.YEAR_DESC, strings.sortYearDesc), SortLabel(SortOption.MILEAGE_ASC, strings.sortMileageAsc)
)

private fun sortComparator(option: SortOption): Comparator<CarAd> = when (option) {
    SortOption.NAME_ASC -> compareBy { it.title.lowercase(Locale.getDefault()) }
    SortOption.NAME_DESC -> compareByDescending { it.title.lowercase(Locale.getDefault()) }
    SortOption.PRICE_ASC -> compareBy { it.priceText.filter(Char::isDigit).toIntOrNull() ?: 0 }
    SortOption.PRICE_DESC -> compareByDescending { it.priceText.filter(Char::isDigit).toIntOrNull() ?: 0 }
    SortOption.YEAR_DESC -> compareByDescending { it.year.toIntOrNull() ?: 0 }
    SortOption.MILEAGE_ASC -> compareBy { it.mileageText.filter(Char::isDigit).toIntOrNull() ?: 0 }
}

private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return R * 2 * asin(sqrt(a))
}

private fun CarAd.matches(
    query: String,
    filters: SearchFilters,
    geocodedCenter: Pair<Double, Double>?,
    clusterCenter: Pair<Double, Double>?,
    clusterRadiusKm: Double?
): Boolean {
    val normalizedQuery = query.trim()
    val minPrice = filters.minPrice.toIntOrNull()
    val maxPrice = filters.maxPrice.toIntOrNull()
    val minYear = filters.minYear.toIntOrNull()
    val maxYear = filters.maxYear.toIntOrNull()
    val maxMileage = filters.maxMileage.toIntOrNull()

    val carPrice = priceText.filter(Char::isDigit).toIntOrNull() ?: 0
    val carYear = year.toIntOrNull() ?: 0
    val carMileage = mileageText.filter(Char::isDigit).toIntOrNull() ?: 0

    val queryMatch = normalizedQuery.isBlank() || title.contains(normalizedQuery, ignoreCase = true)

    val useClusterFilter = clusterCenter != null && clusterRadiusKm != null
    val useGeoFilter = geocodedCenter != null && filters.radiusKm != null

    val locationMatch = when {
        useClusterFilter -> {
            if (latitude == 0.0 && longitude == 0.0) false
            else haversineKm(clusterCenter!!.first, clusterCenter.second, latitude, longitude) <= clusterRadiusKm!!
        }
        useGeoFilter -> {
            if (latitude == 0.0 && longitude == 0.0) false
            else haversineKm(geocodedCenter!!.first, geocodedCenter.second, latitude, longitude) <= filters.radiusKm!!
        }
        filters.location.isNotBlank() -> locationText.contains(filters.location, ignoreCase = true)
        else -> true
    }

    return queryMatch &&
            locationMatch &&
            (filters.brand.isNullOrBlank() || title.contains(filters.brand, ignoreCase = true)) &&
            (filters.modelQuery.isBlank() || title.contains(filters.modelQuery, ignoreCase = true)) &&
            (filters.fuelType == null || fuelTypeKey(fuelText) == fuelTypeKey(filters.fuelType)) &&
            (filters.transmission == null || gearboxTypeKey(gearboxText) == gearboxTypeKey(filters.transmission)) &&
            (minPrice == null || carPrice >= minPrice) &&
            (maxPrice == null || carPrice <= maxPrice) &&
            (minYear == null || carYear >= minYear) &&
            (maxYear == null || carYear <= maxYear) &&
            (maxMileage == null || carMileage <= maxMileage)
}

private fun buildActiveFilterItems(
    query: String,
    filters: SearchFilters,
    strings: AppStrings,
    clusterCenter: Pair<Double, Double>?,
    clusterRadiusKm: Double?
): List<ActiveFilterItem> {
    val labels = mutableListOf<ActiveFilterItem>()
    if (query.isNotBlank()) labels += ActiveFilterItem(ActiveFilterKey.QUERY, "${strings.search}: ${query.trim()}")
    if (!filters.brand.isNullOrBlank()) labels += ActiveFilterItem(ActiveFilterKey.BRAND, "${strings.brand}: ${filters.brand.trim()}")
    if (filters.modelQuery.isNotBlank()) labels += ActiveFilterItem(ActiveFilterKey.MODEL, "${strings.model}: ${filters.modelQuery.trim()}")
    if (filters.minPrice.isNotBlank() || filters.maxPrice.isNotBlank()) {
        labels += ActiveFilterItem(ActiveFilterKey.PRICE_RANGE, "${strings.minPrice}/${strings.maxPrice}: ${filters.minPrice.ifBlank { "-" }}-${filters.maxPrice.ifBlank { "-" }}")
    }
    if (filters.minYear.isNotBlank() || filters.maxYear.isNotBlank()) {
        labels += ActiveFilterItem(ActiveFilterKey.YEAR_RANGE, "${strings.minYear}/${strings.maxYear}: ${filters.minYear.ifBlank { "-" }}-${filters.maxYear.ifBlank { "-" }}")
    }
    if (filters.maxMileage.isNotBlank()) labels += ActiveFilterItem(ActiveFilterKey.MAX_MILEAGE, "${strings.maxMileage}: ${filters.maxMileage}")
    if (!filters.fuelType.isNullOrBlank()) labels += ActiveFilterItem(ActiveFilterKey.FUEL, "${strings.fuelType}: ${filters.fuelType}")
    if (!filters.transmission.isNullOrBlank()) labels += ActiveFilterItem(ActiveFilterKey.TRANSMISSION, "${strings.transmission}: ${filters.transmission}")
    if (filters.location.isNotBlank()) {
        val label = if (filters.radiusKm != null)
            "${strings.location}: ${filters.location.trim()} (${filters.radiusKm} km)"
        else
            "${strings.location}: ${filters.location.trim()}"
        labels += ActiveFilterItem(ActiveFilterKey.LOCATION, label)
    }
    if (clusterCenter != null && filters.location.isBlank()) {
        labels += ActiveFilterItem(
            ActiveFilterKey.CLUSTER_AREA,
            "≤ ${clusterRadiusKm?.toInt() ?: "?"} km"
        )
    }
    return labels
}

private fun String.withSuffix(suffix: String): String {
    val value = trim()
    return if (value.isEmpty()) "" else "$value $suffix"
}
