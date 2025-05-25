package kr.ac.tukorea.honsulchingu.model

data class ChatCharacter(
    val id: Int,
    val type: String,
    val name: String,
    val greet: String,
    val tag: List<String>,
    val description: String,
    val image: Int
)
