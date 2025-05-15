package kr.ac.tukorea.honsulchingu.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kr.ac.tukorea.honsulchingu.MainActivity
import kr.ac.tukorea.honsulchingu.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // 로그인 카드, 텍스트, 버튼 초기 설정
        val loginCard = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.loginCard)
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val kakaoLoginButton = findViewById<ImageButton>(R.id.kakaoLoginButton)

        // 카드, 텍스트, 버튼 초기 설정: 아래로 내려가게, 텍스트와 버튼은 투명
        loginCard.translationY = 1000f
        welcomeText.alpha = 0f
        kakaoLoginButton.alpha = 0f

        // 화면에 뷰가 제대로 그려진 후 애니메이션 시작
        val rootView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.login)
        rootView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // 애니메이션 시작
                val slideIn = ObjectAnimator.ofFloat(loginCard, "translationY", 1000f, 0f)
                slideIn.duration = 800 // 애니메이션 지속 시간 (800ms)

                val fadeInWelcome = ObjectAnimator.ofFloat(welcomeText, "alpha", 0f, 1f)
                fadeInWelcome.duration = 800 // "환영합니다" 텍스트 페이드 인 애니메이션

                val fadeInKakaoButton = ObjectAnimator.ofFloat(kakaoLoginButton, "alpha", 0f, 1f)
                fadeInKakaoButton.duration = 800 // 카카오 로그인 버튼 페이드 인 애니메이션

                // 모든 애니메이션을 동시에 실행
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(slideIn, fadeInWelcome, fadeInKakaoButton)
                animatorSet.start()

                rootView.viewTreeObserver.removeOnPreDrawListener(this) // 애니메이션 실행 후 리스너 제거
                return true
            }
        })

        // ✅ 로그인 버튼 클릭 시 메인으로 이동
        kakaoLoginButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }




        // 안전 영역 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
