package kr.ac.tukorea.honsulchingu.ui.loading

import android.content.SharedPreferences
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AlphaAnimation
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.KakaoSdkError
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.MainActivity
import kr.ac.tukorea.honsulchingu.ui.login.LoginActivity
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import org.json.JSONObject

class LoadingActivity : AppCompatActivity() {

    private val characterViewModel: CharacterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loading)

        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            fillAfter = true
        }

        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) { startMainActivity() }
        })

        mainView.startAnimation(fadeIn)
    }

    private fun startMainActivity() {
        // ✅ 기본 셋팅 값 DB 로딩
        Thread {
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

            Handler(Looper.getMainLooper()).post {
                KakaoSdk.init(this, KAKAO)

                // ✅ 자동 로그인
                if (AuthApiClient.instance.hasToken()) {
                    UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                        if (error != null) {
                            if (error is KakaoSdkError && error.isInvalidTokenError() == true) Log.w("db", "유효하지 않은 토큰 ID: {${tokenInfo?.id}}")
                            else Log.e("db", "토큰 정보 보기 실패", error)

                            requestUserAdditionalScopes(sharedPreferences_setting) {
                                startActivity(Intent(this, LoginActivity::class.java))
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                finish()
                            }
                        }
                        else if (tokenInfo != null) {
                            Log.i("db", "토큰 정보 보기 성공 ID: ${tokenInfo.id} (만료 시간: ${tokenInfo.expiresIn}초)")

                            requestUserAdditionalScopes(sharedPreferences_setting) {}
                        }
                    }
                }
                else {
                    Log.d("db", "토큰 정보 없음")

                    requestUserAdditionalScopes(sharedPreferences_setting) {
                        startActivity(Intent(this, LoginActivity::class.java))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                }
            }
        }.start()
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

                if (user.kakaoAccount?.emailNeedsAgreement == true) { scopes.add("account_email") }
                // if (user.kakaoAccount?.birthdayNeedsAgreement == true) { scopes.add("birthday") }
                // if (user.kakaoAccount?.birthyearNeedsAgreement == true) { scopes.add("birthyear") }
                // if (user.kakaoAccount?.genderNeedsAgreement == true) { scopes.add("gender") }
                // if (user.kakaoAccount?.phoneNumberNeedsAgreement == true) { scopes.add("phone_number") }
                if (user.kakaoAccount?.profileNeedsAgreement == true) { scopes.add("profile") }
                // if (user.kakaoAccount?.ageRangeNeedsAgreement == true) { scopes.add("age_range") }

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

                                    var decision = 0

                                    Thread {
                                        var url = characterViewModel.updateURL("/load_user")

                                        var connection = (url.openConnection() as HttpURLConnection).apply {
                                            requestMethod = "POST"
                                            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                                            doOutput = true
                                        }


                                        var jsonInput = JSONObject().apply {
                                            put("email", "kakao_" + user.kakaoAccount?.email)
                                            put("nickname", "")
                                            put("image", "")
                                            put("age", "")
                                            put("gender", "")
                                            put("startday", "")
                                        }

                                        connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                                        val responseJson = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })

                                        val AGE = responseJson.getString("age")

                                        val GENDER = responseJson.getString("gender")

                                        val STARTDAY = SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(System.currentTimeMillis()))

                                        if (AGE == "" || GENDER == "") decision = 1


                                        url = characterViewModel.updateURL("/add_user")

                                        connection = (url.openConnection() as HttpURLConnection).apply {
                                            requestMethod = "POST"
                                            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                                            doOutput = true
                                        }


                                        jsonInput = JSONObject().apply {
                                            put("email", "kakao_" + user.kakaoAccount?.email)
                                            put("nickname", user.kakaoAccount?.profile?.nickname)
                                            put("image", user.kakaoAccount?.profile?.thumbnailImageUrl)
                                            put("age", AGE)
                                            put("gender", GENDER)
                                            put("startday", STARTDAY)
                                        }

                                        connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                                        connection.inputStream.bufferedReader().use { it.readText() }


                                        sharedPreferences_setting.edit().apply {
                                            putString("EMAIL", "kakao_" + user.kakaoAccount?.email)
                                            putString("NICKNAME", user.kakaoAccount?.profile?.nickname)
                                            putString("IMAGE", user.kakaoAccount?.profile?.thumbnailImageUrl)
                                            putString("AGE", AGE)
                                            putString("GENDER", GENDER)
                                            putString("STARTDAY", STARTDAY)
                                            apply()
                                        }

                                        Log.d("db", "if")
                                        Log.d("db", sharedPreferences_setting.getString("EMAIL", "") ?: "")
                                        Log.d("db", sharedPreferences_setting.getString("NICKNAME", "") ?: "")
                                        Log.d("db", sharedPreferences_setting.getString("IMAGE", "") ?: "")
                                        Log.d("db", sharedPreferences_setting.getString("AGE", "") ?: "")
                                        Log.d("db", sharedPreferences_setting.getString("GENDER", "") ?: "")
                                        Log.d("db", sharedPreferences_setting.getString("STARTDAY", "") ?: "")
                                    }.start()


                                    if (decision == 1) {
                                        startActivity(Intent(this, LoginActivity::class.java)) // TODO: 여기 바꾸기
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                        finish()
                                    }
                                    else if (decision == 0) {
                                        startActivity(Intent(this, MainActivity::class.java))
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                        finish()
                                    }

                                    onComplete()
                                }
                            }
                        }
                    }
                }
                else {
                    var decision = 0

                    Thread {
                        var url = characterViewModel.updateURL("/load_user")

                        var connection = (url.openConnection() as HttpURLConnection).apply {
                            requestMethod = "POST"
                            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                            doOutput = true
                        }


                        var jsonInput = JSONObject().apply {
                            put("email", "kakao_" + user.kakaoAccount?.email)
                            put("nickname", "")
                            put("image", "")
                            put("age", "")
                            put("gender", "")
                            put("startday", "")
                        }

                        connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                        val responseJson = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })

                        val AGE = responseJson.getString("age")

                        val GENDER = responseJson.getString("gender")

                        val STARTDAY = SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(System.currentTimeMillis()))

                        if (AGE == "" || GENDER == "") decision = 1


                        url = characterViewModel.updateURL("/add_user")

                        connection = (url.openConnection() as HttpURLConnection).apply {
                            requestMethod = "POST"
                            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                            doOutput = true
                        }


                        jsonInput = JSONObject().apply {
                            put("email", "kakao_" + user.kakaoAccount?.email)
                            put("nickname", user.kakaoAccount?.profile?.nickname)
                            put("image", user.kakaoAccount?.profile?.thumbnailImageUrl)
                            put("age", AGE)
                            put("gender", GENDER)
                            put("startday", STARTDAY)
                        }

                        connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                        connection.inputStream.bufferedReader().use { it.readText() }


                        sharedPreferences_setting.edit().apply {
                            putString("EMAIL", "kakao_" + user.kakaoAccount?.email)
                            putString("NICKNAME", user.kakaoAccount?.profile?.nickname)
                            putString("IMAGE", user.kakaoAccount?.profile?.thumbnailImageUrl)
                            putString("AGE", AGE)
                            putString("GENDER", GENDER)
                            putString("STARTDAY", STARTDAY)
                            apply()
                        }

                        Log.d("db", "else")
                        Log.d("db", sharedPreferences_setting.getString("EMAIL", "") ?: "")
                        Log.d("db", sharedPreferences_setting.getString("NICKNAME", "") ?: "")
                        Log.d("db", sharedPreferences_setting.getString("IMAGE", "") ?: "")
                        Log.d("db", sharedPreferences_setting.getString("AGE", "") ?: "")
                        Log.d("db", sharedPreferences_setting.getString("GENDER", "") ?: "")
                        Log.d("db", sharedPreferences_setting.getString("STARTDAY", "") ?: "")
                    }.start()


                    if (decision == 1) {
                        startActivity(Intent(this, LoginActivity::class.java)) // TODO: 여기 바꾸기
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                    else if (decision == 0) {
                        startActivity(Intent(this, MainActivity::class.java))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }

                    onComplete()
                }
            }
        }
    }
}
