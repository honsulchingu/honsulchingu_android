package kr.ac.tukorea.honsulchingu.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.ac.tukorea.honsulchingu.R

class TutorialItemAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val tutorialData = listOf(
        TutorialData(
            title = "친구 같은 AI, 연인 같은 AI를 골라보세요",
            imageResId = R.drawable.tutorial1
        ),
        TutorialData(
            title = "목소리로 이야기하고,\n기록은 채팅으로 남겨요"
        ),
        TutorialData(
            title = "마음에 드는 문장을 저장하고,\n언제든 확인해요",
            imageResId = R.drawable.tutorial4
        )
    )

    override fun getItemCount(): Int = tutorialData.size + 1 // CompleteFragment 포함

    override fun createFragment(position: Int): Fragment {
        return if (position < tutorialData.size) {
            // stepIndex를 함께 전달
            TutorialItemFragment.newInstance(tutorialData[position], position)
        } else {
            CompleteFragment.newInstance()
        }
    }
}
