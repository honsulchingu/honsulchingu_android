package kr.ac.tukorea.honsulchingu.ui.chat

data class ChatMessage(
    val message: String = "", // 채팅 내용
    val isUser: Boolean = false, // true: 내가 보낸 메시지
    val timestamp: Long = 0, // 보내진 시간
    var isFavorite: Boolean = false // true: 내가 즐겨찾기한 메시지
)
