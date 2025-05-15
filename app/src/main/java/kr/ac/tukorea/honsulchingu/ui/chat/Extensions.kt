package kr.ac.tukorea.honsulchingu.ui.chat

import android.graphics.BitmapShader
import android.graphics.Shader
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration




fun Long.toTimeString(): String {
    val date = Date(this)
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}

// Drawable을 BitmapShader로 변환하는 확장 함수
fun Drawable.toBitmapShader(): Shader {
    val bitmap = this.toBitmap()
    return BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
}

// 날짜(Date)를 채팅용 날짜 포맷으로 변환하는 확장 함수
fun Date.toChatDateString(): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { time = this@toChatDateString }

    return when {
        now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) -> {
            "오늘"
        }
        now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) - 1 == target.get(Calendar.DAY_OF_YEAR) -> {
            "어제"
        }
        else -> {
            SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(this@toChatDateString)
        }
    }
}
