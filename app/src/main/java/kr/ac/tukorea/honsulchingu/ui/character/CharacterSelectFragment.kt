package kr.ac.tukorea.honsulchingu.ui.character

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import kr.ac.tukorea.honsulchingu.databinding.FragmentCharacterSelectBinding

class CharacterSelectFragment : Fragment() {

    private var _binding: FragmentCharacterSelectBinding? = null
    private val binding get() = _binding!!
    private lateinit var characterPagerAdapter: CharacterPagerAdapter

    private lateinit var characterViewModel: CharacterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterSelectBinding.inflate(inflater, container, false)

        // ViewModel 초기화
        characterViewModel = ViewModelProvider(requireActivity()).get(CharacterViewModel::class.java)

        // ViewPager2 어댑터 설정
        characterPagerAdapter = CharacterPagerAdapter(this)
        binding.characterViewPager.adapter = characterPagerAdapter

        // PageTransformer로 애니메이션 제어 (부드럽게 전환)
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

        // 초기 탭을 친구형으로 설정
        characterViewModel.loadCharacters(requireContext()) {
            characterViewModel.updateCharacters("친구")
            updateTabColors(isFriend = true)
        }

        // 탭 클릭 시 캐릭터 필터링 및 ViewPager2 페이지 전환
        binding.friendTab.setOnClickListener {
            characterViewModel.updateCharacters("친구")
            binding.characterViewPager.setCurrentItem(0, false)
            updateTabColors(isFriend = true)
        }

        binding.loverTab.setOnClickListener {
            characterViewModel.updateCharacters("연인")
            binding.characterViewPager.setCurrentItem(1, false)
            updateTabColors(isFriend = false)
        }

        // 💡 스와이프 시에도 ViewModel 데이터 갱신하도록 수정
        binding.characterViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                characterViewModel.updateCharacters(if (position == 0) "친구" else "연인")
                updateTabColors(isFriend = (position == 0))
            }
        })

        return binding.root
    }

    // 탭 색상 업데이트 함수
    private fun updateTabColors(isFriend: Boolean) {
        val binding = _binding ?: return

        val context = requireContext()

        if (isFriend) {
            // 친구형 탭 선택
            binding.friendTabText.setTextColor(ContextCompat.getColor(context, R.color.purple))
            binding.friendTabUnderline.setBackgroundColor(ContextCompat.getColor(context, R.color.purple))

            // 연인형 탭 비선택
            binding.loverTabText.setTextColor(ContextCompat.getColor(context, R.color.gray))
            binding.loverTabUnderline.setBackgroundColor(ContextCompat.getColor(context, R.color.trans))
        }
        else {
            // 연인형 탭 선택
            binding.loverTabText.setTextColor(ContextCompat.getColor(context, R.color.purple))
            binding.loverTabUnderline.setBackgroundColor(ContextCompat.getColor(context, R.color.purple))

            // 친구형 탭 비선택
            binding.friendTabText.setTextColor(ContextCompat.getColor(context, R.color.gray))
            binding.friendTabUnderline.setBackgroundColor(ContextCompat.getColor(context, R.color.trans))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
