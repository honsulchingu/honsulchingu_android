package kr.ac.tukorea.honsulchingu.ui.character

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class CharacterPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = CharacterListFragment()
        return when (position) {
            0 -> CharacterListFragment.newInstance("friend")
            1 -> CharacterListFragment.newInstance("lover")
            else -> Fragment()
        }
    }
}

