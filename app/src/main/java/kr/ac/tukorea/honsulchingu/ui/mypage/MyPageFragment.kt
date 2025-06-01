package kr.ac.tukorea.honsulchingu.ui.mypage

import            coil.load
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.imageview.ShapeableImageView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.Locale
import org.json.JSONObject

class MyPageFragment : Fragment(R.layout.fragment_my_page) {

    private val characterViewModel: CharacterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        load_user(view)

        // 프로필 정보 표시 설정
        val sharedPreferences_setting = requireContext().getSharedPreferences("prefs_setting", MODE_PRIVATE)

        val profileNickname = view.findViewById<TextView>(R.id.profileNickname)
        val profileEmail = view.findViewById<TextView>(R.id.profileEmail)
        val profileGreet = view.findViewById<TextView>(R.id.profileGreet)
        val profileImage = view.findViewById<ShapeableImageView>(R.id.profileImage)
        // val profileStartDay = view.findViewById<TextView>(R.id.profileStartDay)
        val profileFavoriteCount = view.findViewById<TextView>(R.id.profileFavoriteCount)
        val profileChatCount = view.findViewById<TextView>(R.id.profileChatCount)

        profileEmail.text = sharedPreferences_setting.getString("EMAIL", "")?.substringAfter('_')
        profileNickname.text = sharedPreferences_setting.getString("NICKNAME", "")
        profileGreet.text = "안녕하세요 ${profileNickname.text} 님, 환영합니다."
        profileImage.load(sharedPreferences_setting.getString("IMAGE", "")) {
            crossfade(true) // 부드럽게 나타나도록
            placeholder(R.drawable.ic_profile_placeholder) // 로딩 중 보여줄 이미지
            error(R.drawable.ic_profile_placeholder) // 로딩 실패 시 보여줄 이미지
        }
        profileFavoriteCount.text = sharedPreferences_setting.getInt("FAVORITECOUNT", 0).toString()
        profileChatCount.text = sharedPreferences_setting.getInt("CHATCOUNT", 0).toString()
        characterViewModel.chatcount_live.observe(viewLifecycleOwner) { chatcount -> profileChatCount.text = chatcount.toString() }

        // 프로필 관리 클릭시 이동
        view.findViewById<View>(R.id.layoutProfile).setOnClickListener { findNavController().navigate(R.id.action_nav_mypage_to_profileFragment) }

        // 도움말 클릭시 이동
        view.findViewById<View>(R.id.layoutHelp).setOnClickListener { findNavController().navigate(R.id.action_nav_mypage_to_helpFragment) }
    }

    private fun load_user(view: View) {
        Thread {
            val sharedPreferences_setting = requireContext().getSharedPreferences("prefs_setting", MODE_PRIVATE)

            val url = characterViewModel.updateURL("/load_user")

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


            val responseJson = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })

            // val EMAIL = responseJson.getString("email")

            // val NICKNAME = responseJson.getString("nickname")

            // val IMAGE = responseJson.getString("image")

            val STARTDAY = responseJson.getString("startday")

            sharedPreferences_setting.edit().apply {
                // putString("EMAIL", EMAIL)
                // putString("NICKNAME", NICKNAME)
                // putString("IMAGE", IMAGE)
                putString("STARTDAY", STARTDAY)
                apply()
            }

            Handler(Looper.getMainLooper()).post { view.findViewById<TextView>(R.id.profileStartday).text = SimpleDateFormat("yyyy년 M월", Locale.KOREA).format(SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).parse(sharedPreferences_setting.getString("STARTDAY", ""))) }
        }.start()
    }
}
