package kr.ac.tukorea.honsulchingu.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.model.ChatCharacter

class CharacterViewModel : ViewModel() {

    private val _filteredCharacters = MutableLiveData<List<ChatCharacter>>()
    val filteredCharacters: LiveData<List<ChatCharacter>> get() = _filteredCharacters

    private val allCharacters = listOf(
        ChatCharacter(1, "friend", "친구형 A", "기본 메시지", "항상 편안한 친구 같은 느낌!", listOf("친절함", "편안함"), R.drawable.ic_profile_placeholder),
        ChatCharacter(2, "friend", "친구형 B", "안녕, 오늘 어땠어?", "활발하고 털털한 스타일", listOf("유쾌함", "활발함"), R.drawable.ic_profile_placeholder),
        ChatCharacter(3, "lover", "연인형 A", "기다리고 있었어", "부드럽고 다정한 연인 스타일", listOf("다정함", "로맨틱"), R.drawable.ic_profile_placeholder),
        ChatCharacter(4, "lover", "연인형 B", "나랑 술 한잔 할래?", "시크하지만 마음은 따뜻한 타입", listOf("시크함", "츤데레"), R.drawable.ic_profile_placeholder)
    )

    // 캐릭터 로드 메서드
    fun loadCharacters() {
        Log.d("CharacterViewModel", "모든 캐릭터 로딩: ${allCharacters.size}개")
        _filteredCharacters.value = allCharacters
    }

    // 특정 타입의 캐릭터 업데이트
    fun updateCharacters(type: String) {
        val filteredList = allCharacters.filter { it.type == type }
        _filteredCharacters.value = filteredList
    }


}
