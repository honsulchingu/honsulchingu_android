package kr.ac.tukorea.honsulchingu.history

import java.util.Date

data class ChatRecord(
    val name: String,
    val time: Long,
    val last_message: String,
    val tags: List<String>,
    val profileImageRes: Int
)
