package kr.ac.tukorea.honsulchingu.ui.voice

import android.graphics.Color
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
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.imageview.ShapeableImageView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.navigation.NavAnimationUtil
import kr.ac.tukorea.honsulchingu.ui.chat.ChatFragment

class VoiceChatFragment : Fragment() {

    private lateinit var chatFragmentContainer: FragmentContainerView
    private lateinit var imageCharacter: ShapeableImageView
    private lateinit var textSpeech: TextView
    private lateinit var buttonMic: ImageButton
    private lateinit var buttonChat: ImageButton
    private lateinit var dimmedView: View
    private lateinit var micLottie: LottieAnimationView
    private lateinit var LodingDotLottie: LottieAnimationView

    private var isRecording = false
    private var isWaitingForAnswer = false
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
        micLottie = view.findViewById(R.id.micLottie)
        LodingDotLottie = view.findViewById(R.id.voiceWaveLottie)

        // 텍스트 스크롤
        textSpeech.movementMethod = ScrollingMovementMethod()

        // 캐릭터 정보 불러오기 - 추후 ViewModel 연동
        imageCharacter.setImageResource(R.drawable.friend1)

        // 버튼 이벤트
        buttonMic.setOnClickListener {
            // 테스트용: 녹음 상태 → 대기 상태 → 기본 상태 순환
            // 대기 상태는 AI 응답 시간에 쓰일 예정
            when {
                !isRecording && !isWaitingForAnswer -> {
                    isRecording = true
                    isWaitingForAnswer = false
                }
                isRecording -> {
                    isRecording = false
                    isWaitingForAnswer = true
                }
                else -> {
                    isRecording = false
                    isWaitingForAnswer = false
                }
            }
            updateMicUI()
        }


        micLottie.setOnClickListener {
            if (isRecording) {
                isRecording = false
                isWaitingForAnswer = true
                updateMicUI()
            }
        }

        buttonChat.setOnClickListener {
            if (!isChatVisible) showChatFragment()
            else hideChatFragment()
        }
    }


    private fun updateMicUI() {
        when {
            isRecording -> {
                buttonMic.visibility = View.GONE
                micLottie.visibility = View.VISIBLE
                micLottie.setAnimation("mic_recording.json")
                micLottie.playAnimation()

                buttonMic.setBackgroundResource(R.drawable.bg_mic_recording)
                buttonMic.setImageResource(R.drawable.ic_mic_recording)
                buttonMic.setColorFilter(Color.WHITE)

                textSpeech.visibility = View.VISIBLE
                textSpeech.text = "술 이야기 듣는 중..."

                LodingDotLottie.visibility = View.GONE
                LodingDotLottie.cancelAnimation()
            }


            // 이부분은 나중에 AI 응답 대기할때 쓸 것
            isWaitingForAnswer -> {
                buttonMic.visibility = View.VISIBLE
                micLottie.visibility = View.GONE
                micLottie.cancelAnimation()

                buttonMic.setBackgroundResource(R.drawable.bg_mic_waiting)
                buttonMic.setImageResource(R.drawable.ic_mic_off)
                buttonMic.setColorFilter(Color.parseColor("#D0C5F9"))

                // 대기 상태일 때 빈 텍스트
                textSpeech.text = ""

                LodingDotLottie.visibility = View.VISIBLE
                LodingDotLottie.setAnimation("loading_dot.json")
                LodingDotLottie.playAnimation()
            }

            else -> {
                buttonMic.visibility = View.VISIBLE
                micLottie.visibility = View.GONE
                micLottie.cancelAnimation()

                buttonMic.setBackgroundResource(R.drawable.bg_mic_idle)
                buttonMic.setImageResource(R.drawable.ic_mic_idle)
                buttonMic.setColorFilter(Color.parseColor("#5B2DB3"))

                textSpeech.visibility = View.VISIBLE
                textSpeech.text = "혼술 대기 중"

                LodingDotLottie.visibility = View.GONE
                LodingDotLottie.cancelAnimation()
            }
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