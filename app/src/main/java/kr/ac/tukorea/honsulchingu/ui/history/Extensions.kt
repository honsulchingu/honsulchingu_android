package kr.ac.tukorea.honsulchingu.ui.history

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


fun Date.toSmartDateString(): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { time = this@toSmartDateString }

    val diffMillis = now.timeInMillis - time
    val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

    return when (diffDays) {
        0 -> {
            val amPm = if (target.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
            val hour = target.get(Calendar.HOUR)
            val minute = target.get(Calendar.MINUTE)
            "오늘 $amPm ${hour.ifZeroThenTwelve()}:${minute.toString().padStart(2, '0')}"
        }
        1 -> {
            val amPm = if (target.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
            val hour = target.get(Calendar.HOUR)
            val minute = target.get(Calendar.MINUTE)
            "어제 $amPm ${hour.ifZeroThenTwelve()}:${minute.toString().padStart(2, '0')}"
        }
        in 2..7 -> {
            "${diffDays}일 전"
        }
        else -> {
            val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            formatter.format(this@toSmartDateString)
        }
    }
}

private fun Int.ifZeroThenTwelve(): Int = if (this == 0) 12 else this
