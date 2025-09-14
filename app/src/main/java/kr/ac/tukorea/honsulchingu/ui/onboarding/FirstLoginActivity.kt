package kr.ac.tukorea.honsulchingu.ui.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.ui.onboarding.TutorialFragment
import kr.ac.tukorea.honsulchingu.ui.onboarding.UserInfoFragment

class FirstLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_login)
//
//        // 최초 진입: UserInfoFragment 표시
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, UserInfoFragment())
//                .commit()
//        }
        // 테스트용: 바로 TutorialFragment 표시
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TutorialFragment())
                .commit()
        }
    }

    // UserInfoFragment에서 호출
    fun goToTutorial() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, TutorialFragment())
            .commit()
    }

    // TutorialFragment에서 호출
    fun goToComplete() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, CompleteFragment())
            .commit()
    }

}
