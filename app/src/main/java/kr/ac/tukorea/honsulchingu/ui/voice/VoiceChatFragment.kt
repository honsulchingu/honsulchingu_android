package kr.ac.tukorea.honsulchingu.ui.voice

import ChatAdapter
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.findNavController
import com.google.android.material.imageview.ShapeableImageView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.navigation.NavAnimationUtil
import kr.ac.tukorea.honsulchingu.ui.chat.ChatFragment
import kr.ac.tukorea.honsulchingu.ui.chat.ChatMessage

class VoiceChatFragment : Fragment() {

    private lateinit var chatFragmentContainer: FragmentContainerView
    private lateinit var imageCharacter: ShapeableImageView
    private lateinit var textSpeech: TextView
    private lateinit var buttonMic: ImageButton
    private lateinit var buttonChat: ImageButton
    private lateinit var dimmedView: View
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatMessages: MutableList<ChatMessage>

    private var isMicOn = false
    private var isChatVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_voice_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imageCharacter = view.findViewById(R.id.imageCharacter)
        textSpeech = view.findViewById(R.id.textSpeech)
        buttonMic = view.findViewById(R.id.buttonMic)
        buttonChat = view.findViewById(R.id.buttonChat)
        chatFragmentContainer = view.findViewById(R.id.chatFragmentContainer)
        dimmedView = view.findViewById(R.id.dimmedView) // dimmedView 연결


        // 텍스트 스크롤
        textSpeech.movementMethod = ScrollingMovementMethod()

        // 캐릭터 정보 불러오기 - 추후 ViewModel 연동
        imageCharacter.setImageResource(R.drawable.friend1)

        // 버튼 이벤트
        buttonMic.setOnClickListener {
            isMicOn = !isMicOn
            updateMicUI()
        }

        buttonChat.setOnClickListener {
            if (!isChatVisible) showChatFragment()
            else hideChatFragment()
        }
    }


    private fun updateMicUI() {
        if (isMicOn) {
            buttonMic.setBackgroundResource(R.drawable.bg_button_circle_large)
            buttonMic.setImageResource(R.drawable.ic_mic2)
            textSpeech.text = "음성 채팅 시작..."
        } else {
            buttonMic.setBackgroundResource(R.drawable.bg_off_button_circle_large)
            buttonMic.setImageResource(R.drawable.ic_mic_off)
            textSpeech.text = "AI 대답 위치"
        }
    }

    private fun showChatFragment() {
        val navController = findNavController()
        navController.navigate(
            R.id.chatFragment,
            null,
            NavAnimationUtil.getVoiceChatToChatAnim()
        )

        // 배경 흐림 애니메이션
        dimmedView.visibility = View.VISIBLE
        dimmedView.animate().alpha(1f).setDuration(300).start()

        isChatVisible = true
    }


    private fun hideChatFragment() {
        // 흐림 배경 제거 애니메이션
        dimmedView.animate().alpha(0f).setDuration(300).withEndAction {
            dimmedView.visibility = View.GONE
        }.start()

        // ChatFragment를 스택에서 제거하여 숨김
        parentFragmentManager.popBackStack()
        isChatVisible = false
    }

    override fun onResume() {
        super.onResume()
        // 현재 ChatFragment가 백스택에 남아 있는지 확인
        val currentFragment = parentFragmentManager.findFragmentById(R.id.fragment_container)
        isChatVisible = currentFragment is ChatFragment

        // 화면 복귀 시에도 dimmed 상태 유지
        if (isChatVisible) {
            dimmedView.visibility = View.VISIBLE
            dimmedView.alpha = 1f
        } else {
            dimmedView.visibility = View.GONE
            dimmedView.alpha = 0f
        }
    }
}
