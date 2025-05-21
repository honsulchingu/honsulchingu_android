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
    ): View? {
        _binding = FragmentCharacterSelectBinding.inflate(inflater, container, false)

        // ViewModel 초기화
        characterViewModel = ViewModelProvider(requireActivity()).get(CharacterViewModel::class.java)

        // ViewPager2 어댑터 설정
        characterPagerAdapter = CharacterPagerAdapter(this)
        binding.characterViewPager.adapter = characterPagerAdapter

        // PageTransformer로 애니메이션 제어 (부드럽게 전환)
        binding.characterViewPager.setPageTransformer { page, position ->
            page.alpha = 1 - Math.abs(position)  // 페이드 효과
            page.translationX = -position * page.width // 스와이프 효과
            page.scaleX = 1 - 0.25f * Math.abs(position)  // 크기 변화로 부드러운 전환
            page.scaleY = 1 - 0.25f * Math.abs(position)  // 크기 변화
        }

        // 초기 탭 색상 설정
        updateTabColors(isFriend = true)

        // 초기 ViewPager2 페이지 설정 (친구형)
        binding.characterViewPager.setCurrentItem(0, false)  // 애니메이션 없이 페이지 전환

        // 탭 클릭 시 캐릭터 필터링 및 ViewPager2 페이지 전환
        binding.friendTab.setOnClickListener {
            // 데이터 갱신
            characterViewModel.updateCharacters("friend")
            // 어댑터 데이터 갱신
            characterPagerAdapter.notifyDataSetChanged()
            // 애니메이션 없는 전환
            binding.characterViewPager.setCurrentItem(0, false)  // 부드럽게 애니메이션
            updateTabColors(isFriend = true)
        }

        binding.loverTab.setOnClickListener {
            // 데이터 갱신
            characterViewModel.updateCharacters("lover")
            // 어댑터 데이터 갱신
            characterPagerAdapter.notifyDataSetChanged()
            // 애니메이션 없는 전환
            binding.characterViewPager.setCurrentItem(1, false)  // 부드럽게 애니메이션
            updateTabColors(isFriend = false)
        }

        // 💡 스와이프 시에도 ViewModel 데이터 갱신하도록 수정
        binding.characterViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == 0) {
                    // 데이터 갱신
                    characterViewModel.updateCharacters("friend")
                    // 어댑터 데이터 갱신
                    characterPagerAdapter.notifyDataSetChanged()
                    // 애니메이션 없는 전환
                    binding.characterViewPager.setCurrentItem(0, true)  // 부드럽게 애니메이션
                    updateTabColors(isFriend = true)
                } else {
                    characterViewModel.updateCharacters("lover")
                    // 어댑터 데이터 갱신
                    characterPagerAdapter.notifyDataSetChanged()
                    // 애니메이션 없는 전환
                    binding.characterViewPager.setCurrentItem(1, true)  // 부드럽게 애니메이션
                    updateTabColors(isFriend = false)
                }
            }
        })

        // ViewPager2 페이지 변경 시 탭 색상 업데이트
        binding.characterViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    updateTabColors(isFriend = true)
                } else {
                    updateTabColors(isFriend = false)
                }
            }
        })

        return binding.root
    }

    // 탭 색상 업데이트 함수
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
