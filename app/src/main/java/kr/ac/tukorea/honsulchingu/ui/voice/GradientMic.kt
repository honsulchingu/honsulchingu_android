package kr.ac.tukorea.honsulchingu.ui.voice

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable

class GradientMic : Drawable() {

    private val density = Resources.getSystem().displayMetrics.density
    private val strokeDp = 2f * density // 2dp → px

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeDp
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val path1 = Path()
    private val path2 = Path()
    private val linePath = Path()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()

        // 그라디언트는 세로 방향
        paint.shader = LinearGradient(
            0f, 0f, 0f, height,
            intArrayOf(
                Color.parseColor("#A770EF"),
                Color.parseColor("#4A00E0")
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )

        val scaleX = width / 24f
        val scaleY = height / 24f

        // Path 1: 마이크 본체
        path1.reset()
        path1.moveTo(12f * scaleX, 2f * scaleY)
        path1.lineTo(9f * scaleX, 5f * scaleY)
        path1.lineTo(9f * scaleX, 12f * scaleY)
        path1.lineTo(15f * scaleX, 12f * scaleY)
        path1.lineTo(15f * scaleX, 5f * scaleY)
        path1.lineTo(12f * scaleX, 2f * scaleY)
        path1.close()

        // Path 2: 마이크 베이스 (아치형)
        path2.reset()
        path2.moveTo(19f * scaleX, 10f * scaleY)
        path2.lineTo(19f * scaleX, 12f * scaleY)
        val arcRect = RectF(5f * scaleX, 12f * scaleY, 19f * scaleX, 17f * scaleY)
        path2.arcTo(arcRect, 0f, 180f, false)
        path2.lineTo(5f * scaleX, 10f * scaleY)

        // Path 3: 중앙 아래 선
        linePath.reset()
        linePath.moveTo(12f * scaleX, 19f * scaleY)
        linePath.lineTo(12f * scaleX, 22f * scaleY)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path1, paint)
        canvas.drawPath(path2, paint)
        canvas.drawPath(linePath, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun getAlpha(): Int = paint.alpha

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
