package kr.ac.tukorea.honsulchingu.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.FragmentHelpBinding


class HelpFragment : Fragment() {

    private lateinit var faqAdapter: FaqAdapter // FaqAdapter
    private lateinit var guideAdapter: GuideAdapter // GuideAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_help, container, false) // View 반환
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FAQ 리스트 (FaqItem 객체를 포함)
        val faqList = listOf(
            FaqItem("혼술친구는 어떤 앱인가요?", "혼술친구는 가상의 친구와 음성 채팅을 통해 편안하게 대화할 수 있는 앱입니다."),
            FaqItem("내 정보는 어디서 수정하나요?", "마이페이지에서 이름, 닉네임, 프로필 사진 등을 수정할 수 있어요."),
            FaqItem("취한 정도는 어떻게 측정하나요?", "음성 분석 기술을 활용해 현재 취한 정도를 판단합니다.")
        )


        // FAQ Adapter 설정
        faqAdapter = FaqAdapter(faqList)
        val rvFaqList = view.findViewById<RecyclerView>(R.id.rv_faq_list)
        rvFaqList.layoutManager = LinearLayoutManager(context)
        rvFaqList.adapter = faqAdapter

        // 가이드 리스트 (간단한 문자열 리스트)
        val guideList = listOf(
            GuideItem("음성 채팅 기능 사용하기", "하단의 마이크 버튼을 눌러 음성 대화를 시작해 보세요."),
            GuideItem("즐겨찾기 관리하기", "대화 내용 중 마음에 드는 부분을 길게 눌러 즐겨찾기에 추가할 수 있어요."),
            GuideItem("캐릭터 변경하기", "캐릭터 탭에서 원하는 친구나 연인형 캐릭터를 선택해 보세요."),
            GuideItem("프로필 수정하기", "마이페이지에서 프로필 정보를 편리하게 변경할 수 있습니다.")
        )


        // Guide Adapter 설정
        guideAdapter = GuideAdapter(guideList)
        val rvGuideList = view.findViewById<RecyclerView>(R.id.rv_guide_list)
        rvGuideList.layoutManager = LinearLayoutManager(context)
        rvGuideList.adapter = guideAdapter
    }

    // onDestroyView는 해당 Fragment가 화면에서 사라질 때 호출됨
    override fun onDestroyView() {
        super.onDestroyView()
    }
}


