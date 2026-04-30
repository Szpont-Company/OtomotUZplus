package com.example.otomotuzplus.ui.screens.search

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import com.example.otomotuzplus.models.CarAd
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class CarClusterOverlay(
    private val cars: List<CarAd>,
    private val onSingleCarTap: (CarAd) -> Unit,
    private val onClusterTap: (List<CarAd>) -> Unit
) : Overlay() {

    data class Cluster(
        val geoCenter: GeoPoint,
        val cars: List<CarAd>,
        var screenX: Float = 0f,
        var screenY: Float = 0f
    )

    private var clusters: List<Cluster> = emptyList()
    private var lastZoom = -1.0

    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#E6BD37")
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#B8942A")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#1E1E1E")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    override fun draw(canvas: Canvas?, mapView: MapView?, shadow: Boolean) {
        if (shadow || canvas == null || mapView == null) return
        val zoom = mapView.zoomLevelDouble
        if (abs(zoom - lastZoom) > 0.4 || lastZoom < 0) {
            clusters = computeClusters(zoom)
            lastZoom = zoom
        }
        val density = mapView.resources.displayMetrics.density
        val projection = mapView.projection
        val pt = android.graphics.Point()

        for (cluster in clusters) {
            projection.toPixels(cluster.geoCenter, pt)
            cluster.screenX = pt.x.toFloat()
            cluster.screenY = pt.y.toFloat()

            if (cluster.cars.size == 1) {
                val r = 14f * density
                canvas.drawCircle(cluster.screenX, cluster.screenY, r, markerPaint)
                canvas.drawCircle(cluster.screenX, cluster.screenY, r, strokePaint)
            } else {
                val r = (10f + minOf(cluster.cars.size.toFloat() * 1.5f, 22f)) * density
                canvas.drawCircle(cluster.screenX, cluster.screenY, r, markerPaint)
                canvas.drawCircle(cluster.screenX, cluster.screenY, r, strokePaint)
                textPaint.textSize = 12f * density
                canvas.drawText(
                    cluster.cars.size.toString(),
                    cluster.screenX,
                    cluster.screenY + textPaint.textSize / 3f,
                    textPaint
                )
            }
        }
    }

    override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
        if (e == null || mapView == null) return false
        val density = mapView.resources.displayMetrics.density
        val tapX = e.x
        val tapY = e.y

        for (cluster in clusters) {
            val r = if (cluster.cars.size == 1) 20f * density
                    else (12f + minOf(cluster.cars.size.toFloat() * 1.5f, 24f)) * density
            val dx = tapX - cluster.screenX
            val dy = tapY - cluster.screenY
            if (sqrt(dx * dx + dy * dy) < r) {
                if (cluster.cars.size == 1) onSingleCarTap(cluster.cars[0])
                else onClusterTap(cluster.cars)
                return true
            }
        }
        return false
    }

    private fun computeClusters(zoom: Double): List<Cluster> {
        val precision = when {
            zoom >= 14 -> 3
            zoom >= 11 -> 2
            zoom >= 8  -> 1
            zoom >= 5  -> 0
            zoom >= 3  -> -1
            else       -> -2
        }
        val factor = 10.0.pow(precision.toDouble())
        val buckets = mutableMapOf<Pair<Long, Long>, MutableList<CarAd>>()

        for (car in cars) {
            if (car.latitude == 0.0 && car.longitude == 0.0) continue
            val key = Pair(
                (car.latitude * factor).toLong(),
                (car.longitude * factor).toLong()
            )
            buckets.getOrPut(key) { mutableListOf() }.add(car)
        }

        return buckets.map { (_, group) ->
            val avgLat = group.sumOf { it.latitude } / group.size
            val avgLng = group.sumOf { it.longitude } / group.size
            Cluster(GeoPoint(avgLat, avgLng), group)
        }
    }
}
