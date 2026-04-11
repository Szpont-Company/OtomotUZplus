package com.example.otomotuzplus.ui.screens.add

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otomotuzplus.data.FirebaseRepository
import com.example.otomotuzplus.models.CarAd
import com.example.otomotuzplus.ui.models.AppStrings
import com.example.otomotuzplus.ui.theme.BrandGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    strings: AppStrings,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var mileageText by remember { mutableStateOf("") }
    var fuelText by remember(strings) { mutableStateOf(strings.fuelPetrol) }
    var gearboxText by remember(strings) { mutableStateOf(strings.transmissionManual) }
    var engineCapacity by remember { mutableStateOf("") }
    var powerText by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = strings.addListingTitle,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, top = 24.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(strings.brandAndModel) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = customTextFieldColors()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it.filter { char -> char.isDigit() } },
                label = { Text(strings.price) },
                suffix = { Text(strings.unitCurrency) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = customTextFieldColors()
            )
            OutlinedTextField(
                value = year,
                onValueChange = { year = it.filter { char -> char.isDigit() } },
                label = { Text(strings.year) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = customTextFieldColors()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = mileageText,
                onValueChange = { mileageText = it.filter { char -> char.isDigit() } },
                label = { Text(strings.mileage) },
                suffix = { Text(strings.unitKm) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = customTextFieldColors()
            )
            OutlinedTextField(
                value = locationText,
                onValueChange = { locationText = it },
                label = { Text(strings.city) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = customTextFieldColors()
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = strings.fuelType,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isPetrol = fuelText == strings.fuelPetrol
                Button(
                    onClick = { fuelText = strings.fuelPetrol },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPetrol) BrandGold else MaterialTheme.colorScheme.surface,
                        contentColor = if (isPetrol) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.fuelPetrol, fontWeight = if (isPetrol) FontWeight.Bold else FontWeight.Normal)
                }

                val isDiesel = fuelText == strings.fuelDiesel
                Button(
                    onClick = { fuelText = strings.fuelDiesel },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDiesel) BrandGold else MaterialTheme.colorScheme.surface,
                        contentColor = if (isDiesel) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.fuelDiesel, fontWeight = if (isDiesel) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = strings.transmission,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isManual = gearboxText == strings.transmissionManual
                Button(
                    onClick = { gearboxText = strings.transmissionManual },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isManual) BrandGold else MaterialTheme.colorScheme.surface,
                        contentColor = if (isManual) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.transmissionManual, fontWeight = if (isManual) FontWeight.Bold else FontWeight.Normal)
                }

                val isAuto = gearboxText == strings.transmissionAutomatic
                Button(
                    onClick = { gearboxText = strings.transmissionAutomatic },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAuto) BrandGold else MaterialTheme.colorScheme.surface,
                        contentColor = if (isAuto) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.transmissionAutomatic, fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = engineCapacity,
                onValueChange = { engineCapacity = it.filter { char -> char.isDigit() } },
                label = { Text(strings.engineCapacityLabel) },
                suffix = { Text(strings.unitCm3) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = customTextFieldColors()
            )
            OutlinedTextField(
                value = powerText,
                onValueChange = { powerText = it.filter { char -> char.isDigit() } },
                label = { Text(strings.power) },
                suffix = { Text(strings.unitPower) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = customTextFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && priceText.isNotBlank()) {
                    isUploading = true
                    val currentUserEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: strings.noEmail
                    val newCar = CarAd(
                        title = title,
                        priceText = priceText,
                        locationText = locationText,
                        year = year,
                        mileageText = mileageText,
                        fuelText = fuelText,
                        gearboxText = gearboxText,
                        engineCapacity = engineCapacity,
                        powerText = powerText,
                        imageUrl = "",
                        sellerId = currentUserEmail
                    )

                    repository.addCar(
                        car = newCar,
                        onSuccess = {
                            isUploading = false
                            Toast.makeText(context, strings.addListingSuccess, Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        },
                        onFailure = { e ->
                            isUploading = false
                            Toast.makeText(context, strings.addListingError.format(e.message ?: ""), Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    Toast.makeText(context, strings.addListingRequiredFields, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
            enabled = !isUploading
        ) {
            Text(
                text = if (isUploading) strings.uploading else strings.addListingTitle,
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandGold,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = BrandGold
)