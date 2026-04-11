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
import com.example.otomotuzplus.ui.theme.BrandGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var mileageText by remember { mutableStateOf("") }
    var fuelText by remember { mutableStateOf("Benzyna") }
    var gearboxText by remember { mutableStateOf("Manualna") }
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
            text = "Dodaj ogłoszenie",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, top = 24.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Marka i model") },
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
                label = { Text("Cena") },
                suffix = { Text("zł") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = customTextFieldColors()
            )
            OutlinedTextField(
                value = year,
                onValueChange = { year = it.filter { char -> char.isDigit() } },
                label = { Text("Rocznik") },
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
                label = { Text("Przebieg") },
                suffix = { Text("km") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = customTextFieldColors()
            )
            OutlinedTextField(
                value = locationText,
                onValueChange = { locationText = it },
                label = { Text("Miasto") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = customTextFieldColors()
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Rodzaj paliwa",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isPetrol = fuelText == "Benzyna"
                Button(
                    onClick = { fuelText = "Benzyna" },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPetrol) BrandGold else MaterialTheme.colorScheme.surface,
                        contentColor = if (isPetrol) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Benzyna", fontWeight = if (isPetrol) FontWeight.Bold else FontWeight.Normal)
                }

                val isDiesel = fuelText == "Diesel"
                Button(
                    onClick = { fuelText = "Diesel" },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDiesel) BrandGold else MaterialTheme.colorScheme.surface,
                        contentColor = if (isDiesel) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Diesel", fontWeight = if (isDiesel) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Skrzynia biegów",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isManual = gearboxText == "Manualna"
                Button(
                    onClick = { gearboxText = "Manualna" },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isManual) BrandGold else MaterialTheme.colorScheme.surface,
                        contentColor = if (isManual) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Manualna", fontWeight = if (isManual) FontWeight.Bold else FontWeight.Normal)
                }

                val isAuto = gearboxText == "Automatyczna"
                Button(
                    onClick = { gearboxText = "Automatyczna" },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAuto) BrandGold else MaterialTheme.colorScheme.surface,
                        contentColor = if (isAuto) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Automatyczna", fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Normal)
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
                label = { Text("Pojemność") },
                suffix = { Text("cm3") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = customTextFieldColors()
            )
            OutlinedTextField(
                value = powerText,
                onValueChange = { powerText = it.filter { char -> char.isDigit() } },
                label = { Text("Moc") },
                suffix = { Text("KM") },
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
                    val currentUserEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "brak emaila"
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
                            Toast.makeText(context, "Ogłoszenie dodane!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        },
                        onFailure = { e ->
                            isUploading = false
                            Toast.makeText(context, "Błąd: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Podaj przynajmniej markę i cenę!", Toast.LENGTH_SHORT).show()
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
                text = if (isUploading) "Wysyłanie..." else "Dodaj ogłoszenie",
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