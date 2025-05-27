package kr.ac.tukorea.honsulchingu.ui.mypage

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import coil.load
import com.kakao.sdk.user.UserApiClient
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.ui.login.LoginActivity
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import org.json.JSONObject
import java.net.HttpURLConnection
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import kr.ac.tukorea.honsulchingu.ui.DialogUtil

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val characterViewModel: CharacterViewModel by activityViewModels()

        val sharedPreferences_setting = requireContext().getSharedPreferences("prefs_setting", MODE_PRIVATE)
        val sharedPreferences_history = requireContext().getSharedPreferences("prefs_history", MODE_PRIVATE)
        val sharedPreferences_chat = requireContext().getSharedPreferences("prefs_chat", MODE_PRIVATE)

        val profileImage = view.findViewById<ShapeableImageView>(R.id.profileImage)
        val profileNickname = view.findViewById<TextView>(R.id.profileNickname)
        val profileEmail = view.findViewById<TextView>(R.id.profileEmail)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        val btnWithdraw = view.findViewById<MaterialButton>(R.id.btnWithdraw)

        profileImage.load(sharedPreferences_setting.getString("IMAGE", "")) {
            crossfade(true) // 부드럽게 나타나도록
            placeholder(R.drawable.ic_profile_placeholder) // 로딩 중 보여줄 이미지
            error(R.drawable.ic_profile_placeholder) // 로딩 실패 시 보여줄 이미지
        }
        profileNickname.text = sharedPreferences_setting.getString("NICKNAME", "")
        profileEmail.text = sharedPreferences_setting.getString("EMAIL", "")?.substringAfter('_')

        btnLogout.setOnClickListener {
            DialogUtil.showHonsulDialog(
                context = requireContext(),
                title = "로그아웃 하시겠어요?",
                message = "혼술친구를 안전하게 종료하시려면\n로그아웃해주세요.",
                iconRes = R.drawable.ic_logout,
                positiveText = "로그아웃",
                negativeText = "취소",
                onPositiveClick = {
                    if (sharedPreferences_setting.getString("EMAIL", "")?.substringBefore('_') == "kakao") {
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
                    else if (sharedPreferences_setting.getString("EMAIL", "")?.substringBefore('_') == "google") {
                        // 구글 로그아웃
                    }
                }
            )
        }

        btnWithdraw.setOnClickListener {
            DialogUtil.showHonsulDialog(
                context = requireContext(),
                title = "회원 탈퇴 하시겠어요?",
                message = "탈퇴하시면 지금까지의 모든 대화와 정보가 삭제됩니다.\n정말로 탈퇴를 진행하시겠습니까?",
                iconRes = R.drawable.ic_close,
                positiveText = "탈퇴하기",
                negativeText = "취소",
                onPositiveClick = {
                    if (sharedPreferences_setting.getString("EMAIL", "")?.substringBefore('_') == "kakao") {
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

                                    sharedPreferences_setting.edit().clear().apply()
                                    sharedPreferences_history.edit().clear().apply()
                                    sharedPreferences_chat.edit().clear().apply()
                                }.start()

                                val intent = Intent(requireContext(), LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)

                                requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                requireActivity().finish()
                            }
                        }
                    }
                    else if (sharedPreferences_setting.getString("EMAIL", "")?.substringBefore('_') == "google") {
                        // 구글 탈퇴
                    }
                }
            )
        }

        return view
    }
}
