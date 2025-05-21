package kr.ac.tukorea.honsulchingu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.model.ChatCharacter

class CharacterViewModel : ViewModel() {

    private val _filteredCharacters = MutableLiveData<List<ChatCharacter>>()
    val filteredCharacters: LiveData<List<ChatCharacter>> get() = _filteredCharacters

    private val allCharacters = listOf(
        ChatCharacter(1, "friend", "친구_이나경", "기본 메시지", listOf("친절함", "편안함", "가나다라"), "항상 편안한 친구 같은 느낌!", R.drawable.friend1),
        ChatCharacter(2, "friend", "친구_최민혁", "안녕, 오늘 어땠어?", listOf("유쾌함", "활발함", "가나다라"), "활발하고 털털한 스타일", R.drawable.friend2),
        ChatCharacter(3, "lover", "연인_김세희", "기다리고 있었어", listOf("다정함", "로맨틱", "가나다라"), "부드럽고 다정한 연인 스타일", R.drawable.ic_profile_placeholder),
        ChatCharacter(4, "lover", "연인_송하린", "나랑 술 한잔 할래?", listOf("시크함", "츤데레", "가나다라"),"시크하지만 마음은 따뜻한 타입", R.drawable.ic_profile_placeholder)
    )

    // 특정 타입의 캐릭터 업데이트
    fun updateCharacters(type: String) {
        val filteredList = allCharacters.filter { it.type == type }
        _filteredCharacters.value = filteredList
    }
}
