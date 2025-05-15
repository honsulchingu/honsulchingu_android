package kr.ac.tukorea.honsulchingu.ui.character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.FragmentCharacterSelectBinding
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel


class CharacterSelectFragment : Fragment(R.layout.fragment_character_select) {

    private var _binding: FragmentCharacterSelectBinding? = null
    private val binding get() = _binding!!

    private lateinit var characterPagerAdapter: CharacterPagerAdapter
    private lateinit var characterViewModel: CharacterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterSelectBinding.inflate(inflater, container, false)

        characterViewModel = ViewModelProvider(requireActivity())[CharacterViewModel::class.java]
        characterPagerAdapter = CharacterPagerAdapter(this)
        binding.characterViewPager.apply {
            adapter = characterPagerAdapter
            isUserInputEnabled = false
            setCurrentItem(0, false)
            setPageTransformer { page, position ->
                page.alpha = 1 - kotlin.math.abs(position)
                page.translationX = -position * page.width
                page.scaleX = 1 - 0.25f * kotlin.math.abs(position)
                page.scaleY = 1 - 0.25f * kotlin.math.abs(position)
            }
        }

        updateTabColors(isFriend = true)
        characterViewModel.updateCharacters("friend")

        binding.friendTab.setOnClickListener {
            characterViewModel.updateCharacters("friend")
            binding.characterViewPager.setCurrentItem(0, false)
            updateTabColors(isFriend = true)
        }

        binding.loverTab.setOnClickListener {
            characterViewModel.updateCharacters("lover")
            binding.characterViewPager.setCurrentItem(1, false)
            updateTabColors(isFriend = false)
        }

        binding.characterViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                characterViewModel.updateCharacters(if (position == 0) "friend" else "lover")
                updateTabColors(isFriend = (position == 0))
            }
        })

        return binding.root
    }

    private fun updateTabColors(isFriend: Boolean) {
        if (isFriend) {
            // 친구형 탭 선택
            binding.friendTabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple))
            binding.friendTabUnderline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple))

            // 연인형 탭 비선택
            binding.loverTabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            binding.loverTabUnderline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.trans))
        } else {
            // 연인형 탭 선택
            binding.loverTabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple))
            binding.loverTabUnderline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple))

            // 친구형 탭 비선택
            binding.friendTabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            binding.friendTabUnderline.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.trans))
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
