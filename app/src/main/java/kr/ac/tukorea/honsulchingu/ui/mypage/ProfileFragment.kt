package kr.ac.tukorea.honsulchingu.ui.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.ac.tukorea.honsulchingu.R
import com.google.android.material.button.MaterialButton
import kr.ac.tukorea.honsulchingu.ui.DialogUtil

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        val btnWithdraw = view.findViewById<MaterialButton>(R.id.btnWithdraw)

        btnLogout.setOnClickListener {
            DialogUtil.showHonsulDialog(
                context = requireContext(),
                title = "로그아웃 하시겠어요?",
                message = "혼술친구를 안전하게 종료하시려면\n로그아웃해주세요.",
                iconRes = R.drawable.ic_logout,
                positiveText = "로그아웃",
                negativeText = "취소"
            ) {
                // 로그아웃 처리
            }

        }


        btnWithdraw.setOnClickListener {
            DialogUtil.showHonsulDialog(
                context = requireContext(),
                title = "회원 탈퇴 하시겠어요?",
                message = "탈퇴하시면 지금까지의 모든 대화와 정보가 삭제됩니다./n정말로 탈퇴를 진행하시겠습니까?",
                iconRes = R.drawable.ic_logout,
                positiveText = "탈퇴하기",
                negativeText = "취소"
            ) {
                // 탈퇴 처리
            }
        }


        return view
    }
}
