package kr.ac.tukorea.honsulchingu.ui.voice

import android.graphics.*
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.View
import android.content.Context
import android.animation.ValueAnimator
import androidx.core.content.ContextCompat
import kr.ac.tukorea.honsulchingu.R

class MicWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val baseColor = Color.parseColor("#9333EA")
    private val wavePaints = List(4) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            style = Paint.Style.FILL
        }
    }

    private val baseCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = baseColor
        style = Paint.Style.FILL
    }

    private val micDrawable = ContextCompat.getDrawable(context, R.drawable.ic_mic_idle) as? VectorDrawable

    private val baseRadius = 80f
    private val waveDuration = 1000L

    private var centerX = 0f
    private var centerY = 0f

    private val waves = List(4) { index ->
        val alpha = when (index) {
            0 -> 255
            1 -> 180
            2 -> 120
            else -> 80
        }

        val minScale = 1.2f + index * 0.05f
        val maxScale = 1.5f + index * 0.1f

        Wave(
            delay = index * 250L,
            alpha = alpha,
            paint = wavePaints[index],
            minScale = minScale,
            maxScale = maxScale
        )
    }

    init { waves.forEach { it.animator.start() } }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        waves.forEach { it.animator.cancel() }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        centerX = width / 2f
        centerY = height / 2f

        waves.forEach { wave ->
            wave.paint.alpha = wave.alpha
            val radius = baseRadius * wave.currentScale
            canvas.drawCircle(centerX, centerY, radius, wave.paint)
        }

        canvas.drawCircle(centerX, centerY, baseRadius, baseCirclePaint)

        micDrawable?.let {
            val size = width.coerceAtMost(height) / 3
            val left = (centerX - size / 2).toInt()
            val top = (centerY - size / 2).toInt()
            val right = (centerX + size / 2).toInt()
            val bottom = (centerY + size / 2).toInt()
            it.setBounds(left, top, right, bottom)
            it.draw(canvas)
        }

        invalidate()
    }

    inner class Wave(
            delay: Long,
        val alpha: Int,
        val paint: Paint,

        val minScale: Float,

        val maxScale: Float
    ) {
        var currentScale = minScale

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = waveDuration
            startDelay = delay
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                val progress = it.animatedValue as Float
                currentScale = minScale + (maxScale - minScale) * progress
            }
        }
    }
}
