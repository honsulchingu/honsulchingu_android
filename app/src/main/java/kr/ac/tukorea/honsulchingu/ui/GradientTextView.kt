package kr.ac.tukorea.honsulchingu.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class GradientTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    private var gradientShader: Shader? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0) {
            gradientShader = LinearGradient(
                0f, 0f, w.toFloat(), 0f, // 가로 방향 그라데이션
                intArrayOf(
                    Color.parseColor("#7c2ae8"),  // 더 짙은 보라 (Purple 700)
                    Color.parseColor("#9333ea"),  // 중간 자주색 (Purple 800)
                    Color.parseColor("#6f22c9")   // 어두운 자주색 (Purple 900)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = gradientShader
        }
    }

    override fun onDraw(canvas: Canvas) {
        paint.shader = gradientShader
        super.onDraw(canvas)
    }
}
