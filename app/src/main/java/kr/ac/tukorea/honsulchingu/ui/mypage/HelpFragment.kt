package kr.ac.tukorea.honsulchingu.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kr.ac.tukorea.honsulchingu.databinding.FragmentHelpBinding


class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    private lateinit var faqAdapter: FaqAdapter // FaqAdapter로 변경
    private lateinit var guideAdapter: GuideAdapter // GuideAdapter로 변경

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FAQ 리스트 (FaqItem 객체를 포함)
        val faqList = listOf(
            FaqItem("혼술친구는 어떤 앱인가요?", "혼술친구는 가상의 친구와 음성 채팅을 할 수 있는 앱입니다."),
            FaqItem("내 정보는 어디서 수정하나요?", "마이페이지에서 수정 가능합니다."),
            FaqItem("취한 정도는 어떻게 측정되나요?", "음성, 채팅, 카메라 데이터를 종합 분석해 측정합니다.")
        )

        // FAQ Adapter 설정
        faqAdapter = FaqAdapter(faqList)
        binding.rvFaqList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = faqAdapter
        }

        // 가이드 리스트 (간단한 문자열 리스트)
        val guideList = listOf(
            "음성 채팅 기능 사용하기",
            "즐겨찾기 관리하기",
            "캐릭터 변경하기",
            "프로필 수정하기"
        )

        // 가이드 Adapter 설정
        guideAdapter = GuideAdapter(guideList)
        binding.rvGuideList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = guideAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

