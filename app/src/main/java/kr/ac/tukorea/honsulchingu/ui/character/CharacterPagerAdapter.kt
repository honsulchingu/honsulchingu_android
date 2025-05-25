package kr.ac.tukorea.honsulchingu.ui.character

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class CharacterPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CharacterListFragment.newInstance("친구")
            1 -> CharacterListFragment.newInstance("연인")
            else -> Fragment()
        }
    }
}
