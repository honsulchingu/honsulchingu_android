package kr.ac.tukorea.honsulchingu.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.Dispatchers.Main
import kr.ac.tukorea.honsulchingu.MainActivity
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.ui.onboarding.FirstLoginActivity
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import org.json.JSONObject
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private val characterViewModel: CharacterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // ✅ 안전 영역 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ 로그인 카드, 텍스트, 버튼 초기 설정
        val loginCard = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.loginCard)
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val kakaoLoginButton = findViewById<ImageButton>(R.id.kakaoLoginButton)

        // ✅ 카드, 텍스트, 버튼 초기 설정: 아래로 내려가게, 텍스트와 버튼은 투명
        loginCard.translationY = 1000f
        welcomeText.alpha = 0f
        kakaoLoginButton.alpha = 0f

        // ✅ 화면에 뷰가 제대로 그려진 후 애니메이션 시작
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

        // ✅ 기본 셋팅 값 DB 로딩
        Thread {
            Thread.sleep(800) // 800ms 지연, 애니메이션 전환

            val sharedPreferences_setting = getSharedPreferences("prefs_setting", MODE_PRIVATE)

            val url = characterViewModel.updateURL("/load_setting")

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
            }


            val jsonInput = JSONObject()

            connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


            val responseJson = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })

            val KAKAO = responseJson.getString("kakao")

            val BEGIN = responseJson.getString("begin")

            val TAG = responseJson.getString("tag")

            sharedPreferences_setting.edit().apply {
                putString("BEGIN", BEGIN)
                putString("TAG", TAG)
                apply()
            }
        }.start()

        kakaoLoginButton.setOnClickListener {
            val sharedPreferences_setting = getSharedPreferences("prefs_setting", MODE_PRIVATE)

            // ✅ 로그인
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e("db", "카카오계정으로 로그인 실패", error)
                }
                else if (token != null) {
                    Log.i("db", "카카오계정으로 로그인 성공 ID: ${token.accessToken}")

                    requestUserAdditionalScopes(sharedPreferences_setting) {
                        startActivity(Intent(this, FirstLoginActivity::class.java))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                }
            }

            // + https://developers.kakao.com/docs/latest/ko/kakaologin/android (서비스 약관 선택해 동의 받기)
            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Log.e("db", "카카오톡으로 로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우, 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) return@loginWithKakaoTalk

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                    }
                    else if (token != null) {
                        Log.i("db", "카카오톡으로 로그인 성공 ID: ${token.accessToken}")

                        requestUserAdditionalScopes(sharedPreferences_setting) {
                            startActivity(Intent(this, FirstLoginActivity::class.java))
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            finish()
                        }
                    }
                }
            }
            else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

        // ✅ 구글 로그인
    }

    // ✅ 사용자 정보 요청
    private fun requestUserAdditionalScopes(sharedPreferences_setting: SharedPreferences, onComplete: () -> Unit) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("db", "사용자 정보 요청 실패", error)

                onComplete()
            }
            else if (user != null) {
                val scopes = mutableListOf<String>()

                if (user.kakaoAccount?.emailNeedsAgreement == true) scopes.add("account_email")
                if (user.kakaoAccount?.profileNeedsAgreement == true) scopes.add("profile")

                if (scopes.isNotEmpty()) {
                    scopes.add("openid")
                    UserApiClient.instance.loginWithNewScopes(this, scopes) { token, error ->
                        if (error != null) {
                            Log.e("db", "사용자 추가 동의 실패", error)

                            onComplete()
                        }
                        else {
                            Log.d("db", "허용된 동의 항목: ${token?.scopes}")

                            UserApiClient.instance.me { user, error ->
                                if (error != null) {
                                    Log.e("db", "사용자 정보 요청 실패", error)

                                    onComplete()
                                }
                                else if (user != null) {
                                    Log.d("db", "사용자 정보 요청 성공")

                                    Thread {
                                        val url = characterViewModel.updateURL("/add_user")

                                        val connection = (url.openConnection() as HttpURLConnection).apply {
                                                requestMethod = "POST"
                                                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                                                doOutput = true
                                        }


                                        val jsonInput = JSONObject().apply {
                                            put("email", "kakao_" + user.kakaoAccount?.email)
                                            put("nickname", user.kakaoAccount?.profile?.nickname)
                                            put("image", user.kakaoAccount?.profile?.thumbnailImageUrl)
                                            put("startday", SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(System.currentTimeMillis())))
                                        }

                                        connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                                        connection.inputStream.bufferedReader().use { it.readText() }

                                        sharedPreferences_setting.edit().apply {
                                            putString("EMAIL", "kakao_" + user.kakaoAccount?.email)
                                            putString("NICKNAME", user.kakaoAccount?.profile?.nickname)
                                            putString("IMAGE", user.kakaoAccount?.profile?.thumbnailImageUrl)
                                            apply()
                                        }

                                        Log.d("db", "if")
                                        Log.d("db", sharedPreferences_setting.getString("EMAIL", "") ?: "")
                                        Log.d("db", sharedPreferences_setting.getString("NICKNAME", "") ?: "")
                                        Log.d("db", sharedPreferences_setting.getString("IMAGE", "") ?: "")
                                    }.start()

                                    onComplete()
                                }
                            }
                        }
                    }
                }
                else {
                    Thread {
                        val url = characterViewModel.updateURL("/add_user")

                        val connection = (url.openConnection() as HttpURLConnection).apply {
                            requestMethod = "POST"
                            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                            doOutput = true
                        }


                        val jsonInput = JSONObject().apply {
                            put("email", "kakao_" + user.kakaoAccount?.email)
                            put("nickname", user.kakaoAccount?.profile?.nickname)
                            put("image", user.kakaoAccount?.profile?.thumbnailImageUrl)
                            put("startday", SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(System.currentTimeMillis())))
                        }

                        connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                        connection.inputStream.bufferedReader().use { it.readText() }

                        sharedPreferences_setting.edit().apply {
                            putString("EMAIL", "kakao_" + user.kakaoAccount?.email)
                            putString("NICKNAME", user.kakaoAccount?.profile?.nickname)
                            putString("IMAGE", user.kakaoAccount?.profile?.thumbnailImageUrl)
                            apply()
                        }

                        Log.d("db", "else")
                        Log.d("db", sharedPreferences_setting.getString("EMAIL", "") ?: "")
                        Log.d("db", sharedPreferences_setting.getString("NICKNAME", "") ?: "")
                        Log.d("db", sharedPreferences_setting.getString("IMAGE", "") ?: "")
                    }.start()

                    onComplete()
                }
            }
        }
    }
}
