package kr.ac.tukorea.honsulchingu.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import kr.ac.tukorea.honsulchingu.MainActivity
import kr.ac.tukorea.honsulchingu.R

class CompleteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_complete, container, false)

        val btnStart = view.findViewById<MaterialButton>(R.id.btn_start)
        btnStart.setOnClickListener {
            // MainActivity로 이동
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)

            // FirstLoginActivity 종료
            requireActivity().finish()
        }

        return view
    }

    companion object {
        fun newInstance(): CompleteFragment {
            return CompleteFragment()
        }
    }
}
