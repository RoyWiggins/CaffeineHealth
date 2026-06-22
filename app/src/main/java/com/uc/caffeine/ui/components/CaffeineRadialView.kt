package com.uc.caffeine.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uc.caffeine.R
import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.util.RadialCaffeineData
import com.uc.caffeine.util.resolvedZoneId
import androidx.compose.ui.res.stringResource
import java.time.Instant
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * The last seven days of caffeine concentration wrapped around a single
 * 24-hour clock — midnight at the top, noon at the bottom, going clockwise.
 * Each day is a polar line; today is the most prominent, older days fade out.
 */
@Composable
fun CaffeineRadialView(
    data: RadialCaffeineData,
    userSettings: UserSettings,
    nowMillis: Long,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val nowColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
    val labelArgb = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val zoneId = remember(userSettings.timeZoneId) { userSettings.resolvedZoneId() }
    val nowFraction = remember(nowMillis, zoneId) {
        val zdt = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        (zdt.hour * 3600 + zdt.minute * 60 + zdt.second) / 86_400.0
    }
    val use24h = userSettings.use24HourClock

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val outerR = min(size.width, size.height) / 2f - 18.dp.toPx()
                    val baseR = outerR * 0.10f
                    val span = outerR - baseR
                    val maxConc = data.maxConcentrationMg.takeIf { it > 0.0 } ?: 1.0

                    fun pointAt(fraction: Double, value: Double): Offset {
                        val r = baseR + (value / maxConc).toFloat().coerceIn(0f, 1f) * span
                        val theta = Math.toRadians(fraction * 360.0 - 90.0)
                        return Offset(cx + r * cos(theta).toFloat(), cy + r * sin(theta).toFloat())
                    }

                    // Concentric grid rings.
                    listOf(0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                        drawCircle(
                            color = gridColor,
                            radius = baseR + span * frac,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.dp.toPx()),
                        )
                    }
                    // Spokes every 3 hours.
                    for (h in 0 until 24 step 3) {
                        val theta = Math.toRadians(h / 24.0 * 360.0 - 90.0)
                        val isCardinal = h % 6 == 0
                        drawLine(
                            color = gridColor.copy(alpha = if (isCardinal) 0.7f else 0.35f),
                            start = Offset(cx + baseR * cos(theta).toFloat(), cy + baseR * sin(theta).toFloat()),
                            end = Offset(cx + outerR * cos(theta).toFloat(), cy + outerR * sin(theta).toFloat()),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }

                    // Day traces — oldest first so today is drawn on top.
                    data.traces.sortedByDescending { it.dayOffset }.forEach { trace ->
                        if (trace.samples.isEmpty()) return@forEach
                        val alpha = lerp(0.22f, 1f, (6 - trace.dayOffset) / 6f)
                        val strokeWidth = if (trace.dayOffset == 0) 2.5.dp.toPx() else 1.5.dp.toPx()
                        val path = Path()
                        trace.samples.forEachIndexed { i, value ->
                            val fraction = i.toDouble() / trace.samples.size
                            val p = pointAt(fraction, value)
                            if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                        }
                        path.close()  // wrap 23:30 back around to 00:00
                        drawPath(
                            path = path,
                            color = primary.copy(alpha = alpha),
                            style = Stroke(width = strokeWidth),
                        )
                    }

                    // "Now" spoke.
                    val nowTheta = Math.toRadians(nowFraction * 360.0 - 90.0)
                    drawLine(
                        color = nowColor,
                        start = Offset(cx, cy),
                        end = Offset(cx + outerR * cos(nowTheta).toFloat(), cy + outerR * sin(nowTheta).toFloat()),
                        strokeWidth = 1.5.dp.toPx(),
                    )

                    // Cardinal hour labels.
                    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = labelArgb
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 11.dp.toPx()
                    }
                    val labelR = outerR + 10.dp.toPx()
                    val yOff = (paint.descent() + paint.ascent()) / 2f
                    listOf(0, 6, 12, 18).forEach { h ->
                        val theta = Math.toRadians(h / 24.0 * 360.0 - 90.0)
                        val lx = cx + labelR * cos(theta).toFloat()
                        val ly = cy + labelR * sin(theta).toFloat() - yOff
                        drawContext.canvas.nativeCanvas.drawText(hourLabel(h, use24h), lx, ly, paint)
                    }
                }

                if (data.maxConcentrationMg <= 0.0) {
                    Text(
                        text = stringResource(R.string.home_radial_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariant,
                    )
                }
            }

            // Legend: day-of-week initials tinted to match each trace.
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            ) {
                data.traces.forEach { trace ->
                    val alpha = lerp(0.22f, 1f, (6 - trace.dayOffset) / 6f)
                    Text(
                        text = trace.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (trace.dayOffset == 0) FontWeight.Bold else FontWeight.Normal,
                        color = primary.copy(alpha = alpha),
                    )
                }
            }
        }
    }
}

private fun hourLabel(hour: Int, use24h: Boolean): String {
    if (use24h) return hour.toString()
    return when (hour) {
        0 -> "12a"
        12 -> "12p"
        in 1..11 -> "${hour}a"
        else -> "${hour - 12}p"
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction.coerceIn(0f, 1f)
