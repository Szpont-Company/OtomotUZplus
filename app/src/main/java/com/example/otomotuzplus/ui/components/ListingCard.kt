package com.example.otomotuzplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
    val onFavoriteClick: () -> Unit
)

@Composable
fun ListingCard(
    item: ListingCardData,
    modifier: Modifier = Modifier
) {
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
                Text(
                    text = item.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${item.year} • ${item.mileageText} • ${item.fuelText}",
                    color = Slate400,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${item.bodyTypeText} • ${item.driveTypeText} • ${item.locationText}",
                    color = Slate400,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = item.priceText,
                    color = BrandGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp
                )
            }
        }
    }
}

