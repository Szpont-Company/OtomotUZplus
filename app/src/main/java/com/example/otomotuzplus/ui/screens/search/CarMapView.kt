/**
 * @file CarMapView.kt
 * @brief Komponent Compose opakowujący mapę OSMDroid ze zgrupowanymi znacznikami.
 */
package com.example.otomotuzplus.ui.screens.search

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.otomotuzplus.models.CarAd
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * Komponent Compose opakowujący OSMDroid [MapView] wyświetlający ogłoszenia pojazdów
 * jako zgrupowane znaczniki na mapie.
 *
 * ## Konfiguracja mapy
 * - Źródło kafelków: MAPNIK (standard OpenStreetMap).
 * - Zoom wielodotykowy włączony; wbudowane kontrolki zoomu wyłączone (zamiast nich własne FABs).
 * - Granice przewijania ograniczone do Europy.
 * - Centrum początkowe: Polska (52°N, 20°E) na zoomie 6.
 *
 * ## Uprawnienia
 * `ACCESS_FINE_LOCATION` jest żądane podczas kompozycji jeśli nie zostało jeszcze przyznane.
 * Po przyznaniu mapa jest przesuwana do ostatniej znanej lokalizacji urządzenia i dodawany
 * jest [MyLocationNewOverlay].
 *
 * ## Cykl życia
 * [DisposableEffect] wywołuje `onResume` / `onPause` / `onDetach` na bazowym [MapView]
 * zgodnie z cyklem życia Compose i wyłącza nakładkę lokalizacji przy usuwaniu.
 *
 * @param cars            Przefiltrowana lista obiektów [CarAd] do wyświetlenia jako znaczniki.
 * @param onSingleCarTap  Callback wywoływany gdy użytkownik dotknie znacznika jednego samochodu.
 * @param onClusterTap    Callback wywoływany ze wszystkimi samochodami w klastrze po dotknięciu.
 * @param modifier        Opcjonalny zewnętrzny [Modifier].
 */
@Composable
fun CarMapView(
    cars: List<CarAd>,
    onSingleCarTap: (CarAd) -> Unit,
    onClusterTap: (List<CarAd>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val mapView = remember {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = "OtomotUZplus/1.0"
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(false)
            isTilesScaledToDpi = true
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
            minZoomLevel = 4.0
            maxZoomLevel = 19.0
            setScrollableAreaLimitDouble(BoundingBox(72.0, 50.0, 34.0, -25.0))
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            controller.setZoom(6.0)
            controller.setCenter(GeoPoint(52.0, 20.0))
        }
    }

    val myLocationOverlayHolder = remember { arrayOfNulls<MyLocationNewOverlay>(1) }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                @Suppress("MissingPermission")
                val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                if (loc != null) {
                    mapView.controller.setCenter(GeoPoint(loc.latitude, loc.longitude))
                    mapView.controller.setZoom(12.0)
                }
            } catch (_: SecurityException) {}
        }
    }

    LaunchedEffect(cars, hasLocationPermission) {
        mapView.overlays.clear()
        myLocationOverlayHolder[0]?.disableMyLocation()
        myLocationOverlayHolder[0] = null

        mapView.overlays.add(CarClusterOverlay(cars, onSingleCarTap, onClusterTap))

        if (hasLocationPermission) {
            val locationBitmap = createUserLocationBitmap(context)
            val overlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
            overlay.setPersonIcon(locationBitmap)
            overlay.setPersonHotspot(locationBitmap.width / 2f, locationBitmap.height / 2f)
            overlay.enableMyLocation()
            myLocationOverlayHolder[0] = overlay
            mapView.overlays.add(overlay)
        }
        mapView.invalidate()
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            myLocationOverlayHolder[0]?.disableMyLocation()
            mapView.onPause()
            mapView.onDetach()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier
                .fillMaxSize()
                .clip(RectangleShape)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        ) {
            FloatingActionButton(
                onClick = { mapView.controller.zoomIn() },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
            Spacer(Modifier.height(16.dp))
            FloatingActionButton(
                onClick = { mapView.controller.zoomOut() },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Remove, contentDescription = null)
            }
        }
    }
}

private fun createUserLocationBitmap(context: Context): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (density * 20).toInt().coerceAtLeast(16)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#1565C0")
        style = Paint.Style.FILL
    }
    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = density * 2.5f
    }
    val cx = size / 2f
    val cy = size / 2f
    val r = size / 2f - density * 1.5f
    canvas.drawCircle(cx, cy, r, fill)
    canvas.drawCircle(cx, cy, r, ring)
    return bmp
}
