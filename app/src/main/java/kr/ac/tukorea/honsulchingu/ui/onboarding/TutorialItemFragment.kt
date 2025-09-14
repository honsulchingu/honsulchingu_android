package kr.ac.tukorea.honsulchingu.ui.onboarding

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import kr.ac.tukorea.honsulchingu.R

class TutorialItemFragment : Fragment() {

    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var ivExample1: ImageView
    private lateinit var llExample2: LinearLayout
    private lateinit var ivExample2: ImageView
    private lateinit var ivExample3: ImageView

    companion object {
        private const val ARG_TUTORIAL_DATA = "tutorial_data"
        private const val ARG_STEP_INDEX = "step_index"

        fun newInstance(tutorialData: TutorialData, stepIndex: Int): TutorialItemFragment {
            return TutorialItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TUTORIAL_DATA, tutorialData)
                    putInt(ARG_STEP_INDEX, stepIndex)
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tutorial_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tv_title)

        ivExample1 = view.findViewById(R.id.iv_example1)
        llExample2 = view.findViewById(R.id.ll_example2)
        ivExample2 = view.findViewById(R.id.iv_example2)
        ivExample3 = view.findViewById(R.id.iv_example3)

        setupContent()
        animateViewsIn()
    }

    private fun setupContent() {
        val data = arguments?.getSerializable(ARG_TUTORIAL_DATA) as? TutorialData ?: return
        val step = arguments?.getInt(ARG_STEP_INDEX) ?: 0

        tvTitle.text = data.title

        when (step) {
            1 -> { // 2단계: 좌우 이미지
                ivExample1.visibility = View.GONE
                llExample2.visibility = View.VISIBLE
                ivExample2.setImageResource(R.drawable.tutorial2)
                ivExample3.setImageResource(R.drawable.tutorial3)
            }
            else -> { // 1단계, 3단계
                ivExample1.visibility = View.VISIBLE
                llExample2.visibility = View.GONE
                data.imageResId?.let { ivExample1.setImageResource(it) }
            }
        }
    }




    private fun animateViewsIn() {
        val views = listOf(tvTitle)
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 30f
            ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("alpha", 0f, 1f),
                PropertyValuesHolder.ofFloat("translationY", 30f, 0f)
            ).apply {
                duration = 600
                startDelay = index * 150L
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }
}
