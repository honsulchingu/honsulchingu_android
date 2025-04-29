package kr.ac.tukorea.honsulchingu.ui.chat


sealed class ChatItem {
    data class MessageItem(val chatMessage: ChatMessage) : ChatItem()
    data class DateDividerItem(val dateText: String) : ChatItem()
}
