package com.xiannn.ledsync.system.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.xiannn.ledsync.system.ui.theme.Primary
import com.xiannn.ledsync.system.ui.theme.SecondaryContainer
import com.xiannn.ledsync.system.ui.theme.Surface1
import com.xiannn.ledsync.system.ui.theme.Surface3
import com.xiannn.ledsync.system.ui.theme.TertiaryContainer

@Composable
fun DeviceGraphic(modifier: Modifier = Modifier, isActive: Boolean = true) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Scaling factors based on SVG viewBox="39 90 193 91"
        val sx = w / 193f
        val sy = h / 91f

        fun tx(x: Float) = (x - 39f) * sx
        fun ty(y: Float) = (y - 90f) * sy

        // Background polygon 1
        val p1 = Path().apply {
            moveTo(tx(30f), ty(161f))
            lineTo(tx(139f), ty(125f))
            lineTo(tx(139f), ty(80f))
            lineTo(tx(30f), ty(80f))
            close()
        }
        drawPath(p1, color = Surface3)

        // Background polygon 2
        val p2 = Path().apply {
            moveTo(tx(139f), ty(125f))
            lineTo(tx(139f), ty(80f))
            lineTo(tx(260f), ty(80f))
            lineTo(tx(260f), ty(301f))
            lineTo(tx(139f), ty(157f))
            close()
        }
        drawPath(p2, color = SecondaryContainer)

        // Background polygon 3
        val p3 = Path().apply {
            moveTo(tx(30f), ty(161f))
            lineTo(tx(139f), ty(125f))
            lineTo(tx(139f), ty(157f))
            lineTo(tx(260f), ty(301f))
            lineTo(tx(260f), ty(330f))
            lineTo(tx(30f), ty(330f))
            close()
        }
        drawPath(p3, color = Surface1)

        // Accent polygon
        val p4 = Path().apply {
            moveTo(tx(139f), ty(125f))
            lineTo(tx(118f), ty(132f))
            lineTo(tx(139f), ty(157f))
            close()
        }
        drawPath(p4, color = TertiaryContainer)

        // LED Strip lines (Glow + Core)
        val ledPath = Path().apply {
            moveTo(tx(139f), ty(37f))
            lineTo(tx(139f), ty(157f))
            moveTo(tx(30f), ty(161f))
            lineTo(tx(139f), ty(125f))
            moveTo(tx(118f), ty(132f))
            lineTo(tx(185f), ty(212f))
        }

        val glowColor = if (isActive) Primary.copy(alpha = 0.35f) else Color.Gray.copy(alpha = 0.15f)
        val coreColor = if (isActive) Primary else Color.Gray.copy(alpha = 0.5f)

        drawPath(
            ledPath,
            color = glowColor,
            style = Stroke(width = 12f * sx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            ledPath,
            color = coreColor,
            style = Stroke(width = 5f * sx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
