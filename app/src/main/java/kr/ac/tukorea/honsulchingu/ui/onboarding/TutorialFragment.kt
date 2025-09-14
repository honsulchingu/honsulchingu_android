package kr.ac.tukorea.honsulchingu.ui.onboarding

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import kr.ac.tukorea.honsulchingu.R

class TutorialFragment : Fragment(R.layout.fragment_tutorial) {

    private lateinit var tutorialViewPager: ViewPager2
    private lateinit var btnSkip: TextView
    private lateinit var indicators: List<View>
    private lateinit var tutorialAdapter: TutorialItemAdapter

    var currentTutorialStep = 0
        private set

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tutorialViewPager = view.findViewById(R.id.viewPager)
        btnSkip = view.findViewById(R.id.btn_skip) // XML에 존재하는 스킵 버튼 ID

        indicators = listOf(
            view.findViewById(R.id.dot1),
            view.findViewById(R.id.dot2),
            view.findViewById(R.id.dot3)
        )

        setupTutorialViewPager()
        setupClickListeners()
        updateIndicators()
        animateViewsIn()
    }

    private fun setupTutorialViewPager() {
        tutorialAdapter = TutorialItemAdapter(this)
        tutorialViewPager.adapter = tutorialAdapter

        tutorialViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentTutorialStep = position
                updateIndicators()
                animatePageChange()
            }
        })
    }

    private fun setupClickListeners() {
        btnSkip.setOnClickListener {
            goToComplete() // 스킵 버튼 클릭 시 마지막 페이지로 이동
        }
    }

    private fun updateIndicators() {
        val isLastPage = currentTutorialStep == tutorialAdapter.itemCount - 1

        // 도트 표시 여부
        indicators.forEachIndexed { index, indicator ->
            if (isLastPage) {
                indicator.visibility = View.GONE
            } else {
                indicator.visibility = View.VISIBLE

                val isActive = index == currentTutorialStep

                // 선택 상태 애니메이션
                indicator.isSelected = isActive
                animateIndicator(indicator, isActive)

                // 배경 변경
                indicator.setBackgroundResource(
                    if (isActive) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive
                )
            }
        }

        // 스킵 버튼 표시 여부
        btnSkip.visibility = if (isLastPage) View.INVISIBLE else View.VISIBLE
    }



    private fun goToComplete() {
        tutorialViewPager.setCurrentItem(tutorialAdapter.itemCount - 1, true)
    }

    private fun animateViewsIn() {
        val views = listOf<View>(tutorialViewPager, btnSkip) + indicators
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 50f
            ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("alpha", 0f, 1f),
                PropertyValuesHolder.ofFloat("translationY", 50f, 0f)
            ).apply {
                duration = 500
                startDelay = index * 100L
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }

    private fun animateIndicator(indicator: View, isSelected: Boolean) {
        val scale = if (isSelected) 1.2f else 1f
        ObjectAnimator.ofPropertyValuesHolder(
            indicator,
            PropertyValuesHolder.ofFloat("scaleX", indicator.scaleX, scale),
            PropertyValuesHolder.ofFloat("scaleY", indicator.scaleY, scale)
        ).apply { duration = 200; start() }
    }

    private fun animatePageChange() {
        ObjectAnimator.ofPropertyValuesHolder(
            tutorialViewPager,
            PropertyValuesHolder.ofFloat("alpha", 0.7f, 1f)
        ).apply { duration = 300; start() }
    }
}
