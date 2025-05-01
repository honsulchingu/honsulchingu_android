package kr.ac.tukorea.honsulchingu.ui.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.ac.tukorea.honsulchingu.R

class MyPageFragment : Fragment(R.layout.fragment_my_page) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 예: 클릭 이벤트
        view.findViewById<View>(R.id.menuCard).setOnClickListener {
            // TODO: 이동 등 처리
        }
    }
}
