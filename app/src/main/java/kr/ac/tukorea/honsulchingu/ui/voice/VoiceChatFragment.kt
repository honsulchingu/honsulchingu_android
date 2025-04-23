package kr.ac.tukorea.honsulchingu.ui.voice

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.imageview.ShapeableImageView
import kr.ac.tukorea.honsulchingu.R

class VoiceChatFragment : Fragment() {

    private lateinit var imageCharacter: ShapeableImageView
    private lateinit var textSpeech: TextView
    private lateinit var buttonMic: ImageButton
    private lateinit var buttonCamera: ImageButton
    private lateinit var buttonChat: ImageButton
    private var isMicOn = true
    private var isCameraOn = true

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
        buttonCamera = view.findViewById(R.id.buttonCamera)
        buttonChat = view.findViewById(R.id.buttonChat)
        textSpeech = view.findViewById(R.id.textSpeech)

        // 텍스트 스크롤
        textSpeech.movementMethod = ScrollingMovementMethod()

        // 캐릭터 정보 불러오기 - 추후 ViewModel 연동
        imageCharacter.setImageResource(R.drawable.friend1)

        // 버튼 이벤트
        buttonMic.setOnClickListener {
            isMicOn = !isMicOn
            updateMicUI()
        }


        buttonCamera.setOnClickListener {
            isCameraOn = !isCameraOn
            updateCameraUI()
        }


//        buttonChat.setOnClickListener {
//            // 오른쪽으로 채팅 슬라이드 오픈
//            findNavController().navigate(R.id.action_voiceChatFragment_to_textChatFragment)
//        }
    }

    private fun startVoiceChat() {
        // 음성 채팅 시작 (예시: 음성 인식 등)
        textSpeech.text = "음성 채팅을 시작합니다..."
    }

    private fun updateCameraUI() {
        if (isCameraOn) {
            buttonCamera.setBackgroundResource(R.drawable.bg_button_circle)
            buttonCamera.setImageResource(R.drawable.ic_camera)
            textSpeech.text = "카메라가 켜졌어요"
        } else {
            buttonCamera.setBackgroundResource(R.drawable.bg_off_button_circle)
            buttonCamera.setImageResource(R.drawable.ic_camera_off)
            textSpeech.text = "카메라가 꺼졌어요"
        }
    }


    private fun updateMicUI() {
        if (isMicOn) {
            buttonMic.setBackgroundResource(R.drawable.bg_button_circle_large)
            buttonMic.setImageResource(R.drawable.ic_mic)
            textSpeech.text = "음성 채팅을 시작합니다..."
        } else {
            buttonMic.setBackgroundResource(R.drawable.bg_off_button_circle_large)
            buttonMic.setImageResource(R.drawable.ic_mic_off)
            textSpeech.text = "마이크 꺼졌어요"
        }
    }

}
