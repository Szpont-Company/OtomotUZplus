/**
 * @file AdDetailScreen.kt
 * @brief Pełnoekranowy widok szczegółów ogłoszenia z gestowym ujawnianiem kontaktu.
 */
package com.example.otomotuzplus.ui.screens.details

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.otomotuzplus.models.CarAd
import com.example.otomotuzplus.ui.models.AppStrings
import com.example.otomotuzplus.ui.models.localizeFuelType
import com.example.otomotuzplus.ui.models.localizeGearboxType
import com.example.otomotuzplus.ui.theme.BrandGold
import kotlin.math.sqrt

/**
 * Pełnoekranowy widok szczegółów pojedynczego ogłoszenia pojazdu.
 *
 * ## Układ
 * 1. Przezroczysty [TopAppBar] ze strzałką powrotu.
 * 2. Poziomy pager obrazów ([HorizontalPager]) gdy dostępne są zdjęcia, lub
 *    ikona zastępcza.
 * 3. Zaokrąglona karta zawierająca:
 *    - Etykietę "Zweryfikowane ogłoszenie".
 *    - Tytuł, lokalizację i cenę.
 *    - Siatkę specyfikacji ([SpecItem]): rok, przebieg, paliwo, skrzynia, silnik, moc.
 *    - Wiersz identyfikatora sprzedawcy.
 *    - Sekcję kontaktową (patrz poniżej).
 *
 * ## Dotknięcie pokrętłem (shake) do ujawnienia kontaktu
 * Numer telefonu sprzedawcy jest ukryty za gestem potrząśnięcia. [SensorEventListener]
 * zarejestrowany na [Sensor.TYPE_ACCELEROMETER] monitoruje przyspieszenie netto
 * (z wyłączeniem grawitacji). Gdy wartość przekroczy **5 m/s²** przyciski telefonu
 * i SMS są ujawniane. Czujnik jest wyrejestrowany w `onDispose` aby zapobiec
 * nadmiernemu zużyciu baterii.
 *
 * @param car         Obiekt [CarAd] do wyświetlenia.
 * @param strings     Aktywne [AppStrings] dla zlokalizowanych etykiet.
 * @param onBackClick Callback wywoływany po dotknięciu strzałki powrotu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdDetailScreen(
    car: CarAd,
    strings: AppStrings,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val title = car.title.trim()
    val location = car.locationText.trim()
    val price = car.priceText.trim()
    val seller = car.sellerEmail.ifEmpty { car.sellerId }.trim()
    val phone = car.phoneNumber.filter { it.isDigit() || it == '+' }

    var isContactVisible by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val acceleration = sqrt((x * x + y * y + z * z).toDouble()) - 9.81
                if (acceleration > 5) {
                    isContactVisible = true
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

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
                modifier = Modifier.fillMaxWidth().height(250.dp)
            ) { page ->
                AsyncImage(
                    model = car.imageUrls[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.Gray
                )
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
                        Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    if (location.isNotEmpty()) {
                        Text(text = location, color = Color.Gray, fontSize = 14.sp)
                    }
                }
                if (price.isNotEmpty()) {
                    Text(
                        text = "$price ${strings.unitCurrency}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandGold
                    )
                }
            }

            if (specs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                specs.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { spec ->
                            SpecItem(icon = spec.icon, title = spec.title, subtitle = spec.subtitle, modifier = Modifier.weight(1f))
                        }
                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    if (rowIndex < specs.chunked(2).lastIndex) Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (seller.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = strings.seller,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = seller,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (phone.isNotEmpty()) {
                if (isContactVisible) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                                } catch (_: Exception) { }
                            },
                            modifier = Modifier.weight(1f).height(55.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.callSeller, color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("sms:$phone")))
                                } catch (_: Exception) { }
                            },
                            modifier = Modifier.weight(1f).height(55.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.messageSeller, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrandGold.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.shakePhone,
                            color = BrandGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noPhoneNumber,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Pojedynczy wiersz w siatce specyfikacji pojazdu.
 *
 * Wyświetla wiodącą ikonę, pogrubioną wartość główną ([title]) oraz etykietę
 * pomocniczą ([subtitle]) poniżej.
 *
 * @param icon     Ikona reprezentująca kategorię specyfikacji.
 * @param title    Wartość główna (np. "15 000 km").
 * @param subtitle Etykieta kategorii (np. "Przebieg").
 * @param modifier Opcjonalny [Modifier] stosowany do kontenera wiersza.
 */
@Composable
fun SpecItem(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

private data class DetailSpec(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)