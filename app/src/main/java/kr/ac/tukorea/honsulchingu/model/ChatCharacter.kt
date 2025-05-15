package kr.ac.tukorea.honsulchingu.model

data class ChatCharacter(
    val id: Int,
    val type: String,  // 필터링에 사용되는 'type' 값이 정확히 설정되어 있는지 확인
    val name: String,
    val message: String,
    val description: String,
    val tags: List<String>,
    val profileImage: Int
)
