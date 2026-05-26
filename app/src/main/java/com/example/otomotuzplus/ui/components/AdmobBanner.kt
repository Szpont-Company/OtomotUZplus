/**
 * @file AdmobBanner.kt
 * @brief Komponent baneru reklamowego AdMob.
 */
package com.example.otomotuzplus.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Wyświetla reklamę banerową Google AdMob przy użyciu opakowania interop [AndroidView].
 *
 * ID jednostki reklamowej ustawione na testowe ID banera AdMob
 * (`ca-app-pub-3940256099942544/6300978111`), aby nie generować prawdziwego ruchu
 * w buildach deweloperskich. Przed wydaniem zastąp produkcyjnym ID.
 *
 * @param modifier Opcjonalny [Modifier] stosowany do opakowania [AndroidView];
 *   baner zawsze wypełnia dostępną szerokość.
 */
@Composable
fun AdmobBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}