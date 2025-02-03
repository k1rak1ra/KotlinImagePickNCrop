package net.k1ra.kotlin_image_pick_n_crop.imagecropper

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path

@Stable
fun interface ImageCropShape {
    fun asPath(rect: Rect): Path
}

@Stable
val RectImgCropShape = ImageCropShape { rect -> Path().apply { addRect(rect) } }

@Stable
val CircleImgCropShape = ImageCropShape { rect -> Path().apply { addOval(rect) } }

@Stable
val TriangleImgCropShape = ImageCropShape { rect ->
    Path().apply {
        moveTo(rect.left, rect.bottom)
        lineTo(rect.center.x, rect.top)
        lineTo(rect.right, rect.bottom)
        close()
    }
}

val StarImgCropShape = ImageCropShape { rect ->
    polygonPath(
        tx = rect.left, ty = rect.top,
        sx = rect.width / 32, sy = rect.height / 32,
        points = floatArrayOf(
            31.95f, 12.418856f,
            20.63289f, 11.223692f,
            16f, 0.83228856f,
            11.367113f, 11.223692f,
            0.05000003f, 12.418856f,
            8.503064f, 20.03748f,
            6.1431603f, 31.167711f,
            16f, 25.48308f,
            25.85684f, 31.167711f,
            23.496937f, 20.03748f
        )
    )
}

data class RoundRectImgCropShape(private val cornersPercent: Int) : ImageCropShape {
    override fun asPath(rect: Rect): Path {
        val radius = CornerRadius(rect.minDimension * cornersPercent / 100f)
        return Path().apply { addRoundRect(RoundRect(rect = rect, radius)) }
    }
}

internal val DefaultCropShapes = listOf(
    RectImgCropShape, CircleImgCropShape, RoundRectImgCropShape(15),
    StarImgCropShape, TriangleImgCropShape
)
