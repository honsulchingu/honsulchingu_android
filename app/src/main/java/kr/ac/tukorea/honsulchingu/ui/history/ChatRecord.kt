package kr.ac.tukorea.honsulchingu.ui.history

data class ChatRecord(
    val name: String,
    val last_chat: String,
    val last_time: Long,
    val start_time: String,
    val tag: List<String>,
    val profileImageRes: Int
)
