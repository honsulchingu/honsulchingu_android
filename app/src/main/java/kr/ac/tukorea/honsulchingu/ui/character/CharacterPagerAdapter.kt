package kr.ac.tukorea.honsulchingu.ui.character

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.ac.tukorea.honsulchingu.model.ChatCharacter

class CharacterPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private var currentCharacterList: List<ChatCharacter> = listOf()

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CharacterListFragment.newInstance("friend")
            1 -> CharacterListFragment.newInstance("lover")
            else -> Fragment()
        }
    }

    // 새로운 리스트로 어댑터 데이터 갱신
    fun updateList(characterList: List<ChatCharacter>) {
        currentCharacterList = characterList
        notifyDataSetChanged() // 어댑터 데이터를 갱신한 후 화면에 반영
    }
}
