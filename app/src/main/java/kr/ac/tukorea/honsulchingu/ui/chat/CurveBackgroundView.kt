package kr.ac.tukorea.honsulchingu.ui.chat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kr.ac.tukorea.honsulchingu.R

class CurveBackgroundView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val purplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.bg_color)
        style = Paint.Style.FILL
    }
    private var gradientShader: LinearGradient? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // 그라데이션 수정: 위에서 아래 방향 (270도)
        gradientShader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            Color.parseColor("#9333EA"), // 위쪽 연보라
            Color.parseColor("#3730A3"), // 아래쪽 진보라
            Shader.TileMode.CLAMP
        )
        purplePaint.shader = gradientShader
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val purpleAreaWidth = width * 0.20f // 보라색 영역 차지 비율 (20%)
        val whiteAreaWidth = width - purpleAreaWidth // 하얀색 영역 차지 비율 (80%)

        // 보라색 영역 (전체 20% 차지)
        canvas.drawRect(0f, 0f, purpleAreaWidth, height, purplePaint)

        // 하얀색 영역 (전체 80% 차지)
        canvas.drawRect(purpleAreaWidth, 0f, width, height, whitePaint)

        // 원의 중심을 하얀 배경 쪽으로 옮김
        val circleCenterX = purpleAreaWidth + (whiteAreaWidth * 0.30f) // 하얀 배경의 30% 지점
        val curveHeight = height * 0.44f // 부드럽게 연결하는 원 위치

        // 보라-하양 부드러운 연결용 하얀색 원
        val circleRadius = width * 0.35f
        canvas.drawCircle(circleCenterX, curveHeight, circleRadius, whitePaint)
    }
}
