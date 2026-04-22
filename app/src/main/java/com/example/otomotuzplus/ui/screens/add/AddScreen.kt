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
import java.io.File
import androidx.core.content.FileProvider
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage

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
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            selectedImageUris = selectedImageUris + tempImageUri!!
        }
    }
    val multiplePhotoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = selectedImageUris + uris
        }
    }

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
            onClick = { showPhotoSourceDialog = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.addPhotos, color = MaterialTheme.colorScheme.onSurface)
        }

        if (selectedImageUris.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(selectedImageUris.size) { index ->
                    val uri = selectedImageUris[index]
                    Box {
                        AsyncImage(
                            model = uri,
                            contentDescription = strings.selectedPhotoContentDescription,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImageUris = selectedImageUris - uri },
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = strings.removePhotoContentDescription,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && priceText.isNotBlank()) {
                    isUploading = true

                    repository.uploadImages(selectedImageUris) { uploadedUrls ->

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
                            imageUrls = uploadedUrls,
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
                    }
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

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text(strings.photoSourceTitle) },
            text = { Text(strings.photoSourceMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoSourceDialog = false
                    multiplePhotoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text(strings.photoSourceGallery)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoSourceDialog = false
                    val uri = createTempImageUri(context)
                    tempImageUri = uri
                    cameraLauncher.launch(uri)
                }) {
                    Text(strings.photoSourceCamera)
                }
            }
        )
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
fun createTempImageUri(context: android.content.Context): Uri {
    val tempFile = File.createTempFile("temp_car_image_", ".jpg", context.externalCacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context,
        "com.example.otomotuzplus.provider",
        tempFile
    )
}