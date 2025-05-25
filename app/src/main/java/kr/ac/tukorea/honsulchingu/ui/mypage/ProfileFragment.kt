package kr.ac.tukorea.honsulchingu.ui.mypage

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import com.kakao.sdk.user.UserApiClient
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.ui.login.LoginActivity
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import org.json.JSONObject
import java.net.HttpURLConnection

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment() {

    private val characterViewModel: CharacterViewModel by activityViewModels()

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences_history = requireContext().getSharedPreferences("prefs_history", MODE_PRIVATE)
        val sharedPreferences_setting = requireContext().getSharedPreferences("prefs_setting", MODE_PRIVATE)

        val logoutButton = view.findViewById<Button>(R.id.btnLogout)
        logoutButton.setOnClickListener {
            if (sharedPreferences_setting.getString("EMAIL", "")?.substringBefore("_") == "kakao") {
                UserApiClient.instance.logout { error ->
                    if (error != null) {
                        Log.e("db", "로그아웃 실패", error)
                    }
                    else {
                        Log.i("db", "로그아웃 성공 (SDK에서 토큰 폐기 됨)")

                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        requireActivity().finish()
                    }
                }
            }
            else {
                // 구글 로그아웃
            }
        }

        val withdrawButton = view.findViewById<Button>(R.id.btnWithdraw)
        withdrawButton.setOnClickListener {
            if (sharedPreferences_setting.getString("EMAIL", "")?.substringBefore("_") == "kakao") {
                UserApiClient.instance.unlink { error ->
                    if (error != null) {
                        Log.e("db", "연결 끊기 실패", error)
                    }
                    else {
                        Log.i("db", "연결 끊기 성공 (SDK에서 토큰 폐기 됨)")

                        Thread {
                            val url = characterViewModel.updateURL("/delete_user")

                            val connection = (url.openConnection() as HttpURLConnection).apply {
                                requestMethod = "POST"
                                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                                doOutput = true
                            }


                            val jsonInput = JSONObject().apply {
                                put("email", sharedPreferences_setting.getString("EMAIL", ""))
                                put("nickname", "")
                                put("image", "")
                                put("startday", "")
                            }

                            connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                            connection.inputStream.bufferedReader().use { it.readText() }
                        }.start()


                        sharedPreferences_history.edit().remove("savedChatList").apply()

                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        requireActivity().finish()
                    }
                }
            }
            else {
                // 구글 탈퇴
            }
        }
    }
}
