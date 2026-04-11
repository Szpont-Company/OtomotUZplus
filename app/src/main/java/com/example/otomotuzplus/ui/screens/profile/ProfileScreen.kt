package com.example.otomotuzplus.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otomotuzplus.models.CarAd
import com.example.otomotuzplus.ui.models.AppStrings
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    strings: AppStrings,
    allCarsFromDb: List<CarAd>,
    onCarClick: (CarAd) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(strings.myListings, strings.history)

    val user = FirebaseAuth.getInstance().currentUser
    val userEmail = user?.email ?: "Brak emaila"

    val creationTimestamp = user?.metadata?.creationTimestamp ?: 0L
    val memberYear = if (creationTimestamp > 0) {
        Calendar.getInstance().apply { timeInMillis = creationTimestamp }.get(Calendar.YEAR).toString()
    } else {
        "2026"
    }

    val myCars = allCarsFromDb.filter { it.sellerId == userEmail }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userEmail,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.memberSince.format(memberYear),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSettingsClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = strings.settings,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = strings.settings,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Gray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            MyListingsSection(strings = strings, myCars = myCars, onCarClick = onCarClick)
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(strings.historyEmpty, color = Color.Gray)
            }
        }
    }
}

@Composable
fun MyListingsSection(strings: AppStrings, myCars: List<CarAd>, onCarClick: (CarAd) -> Unit) {
    if (myCars.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = strings.noListingsYet, color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(myCars) { car ->
                CarListingCard(car = car, strings = strings, onClick = { onCarClick(car) })
            }
        }
    }
}

@Composable
fun CarListingCard(car: CarAd, strings: AppStrings, onClick: () -> Unit) {
    val title = car.title.trim()
    val price = car.priceText.withSuffix(strings.unitCurrency)
    val specsLine = listOf(
        car.year.trim(),
        car.mileageText.withSuffix(strings.unitKm)
    ).filter { it.isNotEmpty() }
        .joinToString(", ")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                if (price.isNotEmpty()) {
                    Text(
                        text = price,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (specsLine.isNotEmpty()) {
                    Text(
                        text = specsLine,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = strings.listingsStatus,
                    fontSize = 12.sp,
                    color = Color(0xFF10B981),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun String.withSuffix(suffix: String): String {
    val value = trim()
    return if (value.isEmpty()) "" else "$value $suffix"
}

