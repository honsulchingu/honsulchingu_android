package kr.ac.tukorea.honsulchingu.ui.loading

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import kr.ac.tukorea.honsulchingu.R

class LoadingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // fragment_loading.xml 이름으로 저장했다고 가정
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}
