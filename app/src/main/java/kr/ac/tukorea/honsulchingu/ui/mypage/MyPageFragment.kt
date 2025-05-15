package kr.ac.tukorea.honsulchingu.ui.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kr.ac.tukorea.honsulchingu.R

class MyPageFragment : Fragment(R.layout.fragment_my_page) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // 프로필 관리 클릭시 이동
        view.findViewById<View>(R.id.layoutProfile).setOnClickListener {
            findNavController().navigate(R.id.action_nav_mypage_to_profileFragment)
        }

        // 도움말 클릭시 이동
        view.findViewById<View>(R.id.layoutHelp).setOnClickListener {
            findNavController().navigate(R.id.action_nav_mypage_to_helpFragment)
        }
    }

}

