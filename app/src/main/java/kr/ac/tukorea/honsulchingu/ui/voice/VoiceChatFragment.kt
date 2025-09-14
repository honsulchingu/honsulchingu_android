package kr.ac.tukorea.honsulchingu.ui.voice

import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.graphics.Color
import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.text.method.ScrollingMovementMethod
import android.util.Base64
import android.widget.TextView
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.imageview.ShapeableImageView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.ui.chat.ChatMessage
import kr.ac.tukorea.honsulchingu.ui.chat.ChatFragment
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import kr.ac.tukorea.honsulchingu.navigation.NavAnimationUtil
import org.json.JSONObject
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.io.File
import java.io.RandomAccessFile
import java.io.DataOutputStream
import java.lang.Math.abs
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class VoiceChatFragment : Fragment() {

    private lateinit var chatFragmentContainer: FragmentContainerView
    private lateinit var imageCharacter: ShapeableImageView
    private lateinit var textSpeech: TextView
    private lateinit var buttonMic: ImageButton
    private lateinit var buttonChat: ImageButton
    private lateinit var dimmedView: View
    private lateinit var micLottie: LottieAnimationView
    private lateinit var LodingDotLottie: LottieAnimationView

    private val characterViewModel: CharacterViewModel by activityViewModels()

    private var isChatVisible = false

    @Volatile
    private var isPressed = false

    @Volatile
    private var isPlaying = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_voice_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sharedPreferences_chat = requireContext().getSharedPreferences("prefs_chat", MODE_PRIVATE)

        loadChat()

        imageCharacter = view.findViewById(R.id.imageCharacter)
        textSpeech = view.findViewById(R.id.textSpeech)
        buttonMic = view.findViewById(R.id.buttonMic)
        buttonChat = view.findViewById(R.id.buttonChat)
        chatFragmentContainer = view.findViewById(R.id.chatFragmentContainer)
        dimmedView = view.findViewById(R.id.dimmedView) // dimmedView 연결
        micLottie = view.findViewById(R.id.micLottie)
        LodingDotLottie = view.findViewById(R.id.voiceWaveLottie)

        // 텍스트 스크롤 활성화
        textSpeech.movementMethod = ScrollingMovementMethod()

        // 캐릭터 정보 로딩 및 잠금
        imageCharacter.setImageResource(sharedPreferences_chat.getInt("image", R.drawable.friend_choiminhyeok))
        textSpeech.text = sharedPreferences_chat.getString("greet", "혼술친구를 먼저 정해주세요")
        characterViewModel.greet_live.observe(viewLifecycleOwner) { greet -> textSpeech.text = greet }
        buttonMic.isEnabled = sharedPreferences_chat.getBoolean("isSelected", false)
        buttonChat.isEnabled = sharedPreferences_chat.getBoolean("isSelected", false)

        buttonMic.setOnClickListener {
            // 녹음 상태 → 대기 상태 → 기본 상태 순환
            // TODO: 구글 로그인 구현
            // TODO: 5분마다 분석용 요청 보내기?
            // TODO: tts 생성 시 ( ) 이거 효과 있나?
            // TODO: 대화 너무 늦지 않지?
            if (isPressed) {
                buttonMic.visibility = View.VISIBLE
                micLottie.visibility = View.GONE
                micLottie.cancelAnimation()

                buttonMic.setBackgroundResource(R.drawable.bg_mic_idle)
                buttonMic.setImageResource(R.drawable.ic_mic_idle)
                buttonMic.setColorFilter(Color.parseColor("#5B2DB3"))

                textSpeech.visibility = View.VISIBLE
                textSpeech.text = "혼술 대기 중..."

                LodingDotLottie.visibility = View.GONE
                LodingDotLottie.cancelAnimation()
            }
            send_WAV(requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!)
        }

        micLottie.setOnClickListener {
            isPressed = !isPressed

            buttonMic.visibility = View.VISIBLE
            micLottie.visibility = View.GONE
            micLottie.cancelAnimation()

            buttonMic.setBackgroundResource(R.drawable.bg_mic_idle)
            buttonMic.setImageResource(R.drawable.ic_mic_idle)
            buttonMic.setColorFilter(Color.parseColor("#5B2DB3"))

            textSpeech.visibility = View.VISIBLE
            textSpeech.text = "혼술 대기 중..."

            LodingDotLottie.visibility = View.GONE
            LodingDotLottie.cancelAnimation()
        }

        buttonChat.setOnClickListener {
            if (!isChatVisible) {
                isPressed = !isPressed
                showChatFragment()
            }
            else hideChatFragment()
        }
    }

    private fun send_WAV(externalDir: File) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            return
        }


        val sharedPreferences_setting = requireContext().getSharedPreferences("prefs_setting", MODE_PRIVATE)

        val sharedPreferences_chat = requireContext().getSharedPreferences("prefs_chat", MODE_PRIVATE)


        isPressed = !isPressed

        if (isPressed) {

            if (isPlaying) {

                isPressed = false

                return
            }


            Handler(Looper.getMainLooper()).post {buttonMic.visibility = View.VISIBLE
                micLottie.visibility = View.GONE
                micLottie.cancelAnimation()

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


            Thread {
                val sampleRate = 16000

                val bufferSize = AudioRecord.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val detectData = ByteArray(bufferSize)


                while (isPressed) {

                    if (isPlaying) {

                        Thread.sleep(100)

                        continue
                    }


                    var soundDetected = false

                    val detectRecorder = AudioRecord(
                        MediaRecorder.AudioSource.VOICE_RECOGNITION,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                    )

                    detectRecorder.startRecording()


                    var prevAmplitude = 0.0

                    while (isPressed && !soundDetected) {
                        val read = detectRecorder.read(detectData, 0, bufferSize)
                        var sum = 0.0
                        for (i in 0 until read step 2) {
                            val low = detectData[i].toInt() and 0xff
                            val high = detectData[i + 1].toInt()
                            val sample = (high shl 8) or low
                            sum += abs(sample.toDouble())
                        }
                        val amplitude = sum / (read / 2)
                        if ((amplitude - prevAmplitude) > 500) soundDetected = true // 증폭이 500 이상 증가 시 말을 하는 것으로 판단
                        prevAmplitude = amplitude
                        Log.d("db", "$amplitude")
                    }

                    detectRecorder.stop()
                    detectRecorder.release()


                    val id_user = sharedPreferences_setting.getString("EMAIL", "")
                    val select_user = sharedPreferences_chat.getString("select_user", "")
                    val speak_user = sharedPreferences_chat.getString("speak", "")
                    val time_user = SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(System.currentTimeMillis()))
                    val start_user = sharedPreferences_chat.getString("start_user", "")
                    val gender_user = sharedPreferences_setting.getString("GENDER", "")

                    val wavFile = File(externalDir, "wav_${id_user}_${select_user}_${time_user}_${start_user}.wav")
                    val raf = RandomAccessFile(wavFile, "rw")
                    val byteRate = 16 * sampleRate / 8

                    val totalDataLen = 0

                    raf.writeBytes("RIFF")
                    raf.writeInt(Integer.reverseBytes(totalDataLen + 36))
                    raf.writeBytes("WAVE")
                    raf.writeBytes("fmt ")
                    raf.writeInt(Integer.reverseBytes(16))
                    raf.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt())
                    raf.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt())
                    raf.writeInt(Integer.reverseBytes(sampleRate))
                    raf.writeInt(Integer.reverseBytes(byteRate))
                    raf.writeShort(java.lang.Short.reverseBytes(2.toShort()).toInt())
                    raf.writeShort(java.lang.Short.reverseBytes(16.toShort()).toInt())
                    raf.writeBytes("data")
                    raf.writeInt(0)


                    val audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.VOICE_RECOGNITION,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                    )

                    audioRecord.startRecording()


                    val data = ByteArray(bufferSize)
                    var totalDataWritten = 0
                    var silenceStartTime = 0L
                    var silenceStartAmplitude = 0.0

                    while (isPressed) {
                        val read = audioRecord.read(data, 0, bufferSize)
                        var sum = 0.0
                        for (i in 0 until read step 2) {
                            val low = data[i].toInt() and 0xff
                            val high = data[i + 1].toInt()
                            val sample = (high shl 8) or low
                            sum += abs(sample.toDouble())
                        }
                        raf.write(data, 0, read)
                        totalDataWritten += read
                        val amplitude = sum / (read / 2)
                        if (amplitude < prevAmplitude - 500) {
                            silenceStartTime = System.currentTimeMillis()
                            silenceStartAmplitude = prevAmplitude
                        }
                        if ((amplitude < 500 || amplitude < silenceStartAmplitude - 500) && System.currentTimeMillis() - silenceStartTime > 1000) { // 1. 증폭이 500보다 작거나, 2. 증폭이 500 감소 후 1초 경과 시 말을 그만한 것으로 판단
                            silenceStartTime = 0L
                            silenceStartAmplitude = 0.0
                            break
                        }
                        prevAmplitude = amplitude
                        Log.d("db", "                       $amplitude")
                    }

                    audioRecord.stop()
                    audioRecord.release()


                    raf.seek(4)
                    raf.writeInt(Integer.reverseBytes(36 + totalDataWritten))
                    raf.seek(40)
                    raf.writeInt(Integer.reverseBytes(totalDataWritten))
                    raf.close()


                    if (!isChatVisible && isPressed) {
                        Handler(Looper.getMainLooper()).post {
                            buttonMic.visibility = View.VISIBLE
                            micLottie.visibility = View.GONE
                            micLottie.cancelAnimation()

                            buttonMic.setBackgroundResource(R.drawable.bg_mic_waiting)
                            buttonMic.setImageResource(R.drawable.ic_mic_off)
                            buttonMic.setColorFilter(Color.parseColor("#D0C5F9"))

                            textSpeech.text = ""
                            LodingDotLottie.visibility = View.VISIBLE
                            LodingDotLottie.setAnimation("loading_dot.json")
                            LodingDotLottie.playAnimation()
                        }


                        val boundary = "===" + System.currentTimeMillis() + "==="
                        val lineEnd = "\r\n"
                        val twoHyphens = "--"

                        val url = characterViewModel.updateURL("/analyzation_model")

                        val connection = (url.openConnection() as HttpURLConnection).apply {
                            requestMethod = "POST"
                            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                            doOutput = true
                        }


                        val output = DataOutputStream(connection.outputStream)

                        fun writeFormField(name: String, value: String) {
                            output.writeBytes("$twoHyphens$boundary$lineEnd")
                            output.writeBytes("Content-Disposition: form-data; name=\"$name\"$lineEnd")
                            output.writeBytes("Content-Type: text/plain; charset=UTF-8$lineEnd$lineEnd")
                            output.write(value.toByteArray(Charsets.UTF_8))
                            output.writeBytes(lineEnd)
                        }

                        with(output) {
                            writeFormField("id_user", id_user!!)
                            writeFormField("select_user", select_user!!)
                            writeFormField("speak_user", speak_user!!)
                            writeFormField("time_user", time_user)
                            writeFormField("start_user", start_user!!)
                            writeFormField("gender_user", gender_user!!)

                            writeBytes("$twoHyphens$boundary$lineEnd")
                            writeBytes("Content-Disposition: form-data; name=\"wav_user\"; filename=\"${wavFile.name}\"$lineEnd")
                            writeBytes("Content-Type: audio/wav$lineEnd$lineEnd")
                            write(wavFile.readBytes())
                            writeBytes("$lineEnd$twoHyphens$boundary$twoHyphens$lineEnd")

                            flush()
                            close()
                        }


                        val responseJson = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })

                        val output_ai = responseJson.getString("output_ai")

                        // val tts_ai = responseJson.getString("tts_ai")

                        Handler(Looper.getMainLooper()).post {
                            sharedPreferences_chat.edit().putString("greet", output_ai).apply()
                            characterViewModel.greet_live.value = output_ai

                            buttonMic.visibility = View.VISIBLE
                            micLottie.visibility = View.GONE
                            micLottie.cancelAnimation()

                            buttonMic.visibility = View.GONE
                            micLottie.visibility = View.VISIBLE
                            micLottie.setAnimation("mic_recording.json")
                            micLottie.playAnimation()

                            buttonMic.setBackgroundResource(R.drawable.bg_mic_recording)
                            buttonMic.setImageResource(R.drawable.ic_mic_recording)
                            buttonMic.setColorFilter(Color.WHITE)

                            textSpeech.visibility = View.VISIBLE
                            textSpeech.text = output_ai

                            LodingDotLottie.visibility = View.GONE
                            LodingDotLottie.cancelAnimation()
                        }


//                        val decodedBytes = Base64.decode(tts_ai, Base64.DEFAULT)
//
//                        val ttsFile = File(externalDir, "tts_${id_user}_${select_user}_${time_user}_${start_user}.wav")
//
//                        ttsFile.outputStream().use { it.write(decodedBytes) }
//
//
//                        this@VoiceChatFragment.isPlaying = true
//
//                        var mediaPlayer = MediaPlayer().apply {
//                            setDataSource(ttsFile.absolutePath)
//                            prepare()
//                            setOnCompletionListener { this@VoiceChatFragment.isPlaying = false }
//                            start()
//                        }
                    }
                }
            }.start()
        }
    }

    private fun loadChat() {
        Thread {
            val sharedPreferences_setting = requireContext().getSharedPreferences("prefs_setting", MODE_PRIVATE)

            val sharedPreferences_chat = requireContext().getSharedPreferences("prefs_chat", MODE_PRIVATE)

            val url = characterViewModel.updateURL("/load_chat")

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
            }


            val jsonInput = JSONObject().apply {
                put("id_user", sharedPreferences_setting.getString("EMAIL", ""))
                put("select_user", sharedPreferences_chat.getString("select_user", ""))
                put("input_user", "")
                put("time_user", "")
                put("start_user", sharedPreferences_chat.getString("start_user", ""))
            }

            connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


            val responseJson = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })

            val Messages = responseJson.getJSONArray("chat").let { array ->
                List(array.length()) { i ->
                    val obj = array.getJSONObject(i)
                    val message = obj.getString("text")
                    val isUser = obj.getString("role") == "user"
                    val time = LocalDateTime.parse(obj.getString("time"), DateTimeFormatter.ofPattern("yyyy. MM. dd. HH-mm-ss")).atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli()
                    val isFavorite = obj.getString("favorite") != ""
                    ChatMessage(message, isUser, time, isFavorite)
                }
            }


            if (Messages.isEmpty()) sendToServer("${sharedPreferences_setting.getString("BEGIN", "")}, 사용자의 이름은 \"${sharedPreferences_setting.getString("NICKNAME", "")}\"입니다, 사용자의 나이는 \"${sharedPreferences_setting.getString("AGE", "")}세\"입니다.", System.currentTimeMillis())
        }.start()
    }

    private fun sendToServer(input_user: String, time_user: Long) {
        Thread {
            val sharedPreferences_setting = requireContext().getSharedPreferences("prefs_setting", MODE_PRIVATE)

            val sharedPreferences_chat = requireContext().getSharedPreferences("prefs_chat", MODE_PRIVATE)

            val url = characterViewModel.updateURL("/conversation_model")

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
            }


            if (sharedPreferences_chat.getBoolean("isFirst", true)) {
                Handler(Looper.getMainLooper()).post {
                    sharedPreferences_setting.edit().putInt("CHATCOUNT", sharedPreferences_setting.getInt("CHATCOUNT", 0) + 1).apply()
                    characterViewModel.chatcount_live.value = sharedPreferences_setting.getInt("CHATCOUNT", 0)
                }
            }

            val jsonInput = JSONObject().apply {
                put("id_user", sharedPreferences_setting.getString("EMAIL", ""))
                put("select_user", sharedPreferences_chat.getString("select_user", ""))
                put("input_user", input_user)
                put("time_user", SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(time_user)))
                put("start_user", sharedPreferences_chat.getString("start_user", ""))
            }

            connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


            val responseJson = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })

            val output_ai = responseJson.getString("output_ai")

            // val tts_ai = responseJson.getString("tts_ai")

            Handler(Looper.getMainLooper()).post {
                if (sharedPreferences_chat.getBoolean("isFirst", true)) sharedPreferences_chat.edit().putBoolean("isFirst", false).apply()

                sharedPreferences_chat.edit().putString("greet", output_ai).apply()
                characterViewModel.greet_live.value = output_ai
            }


//            val decodedBytes = Base64.decode(tts_ai, Base64.DEFAULT)
//
//            val ttsFile = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!, "tts.wav")
//
//            ttsFile.outputStream().use { it.write(decodedBytes) }
//
//
//            var mediaPlayer = MediaPlayer().apply {
//                setDataSource(ttsFile.absolutePath)
//                prepare()
//                start()
//            }
        }.start()
    }

    private fun showChatFragment() {
        // 배경 흐림 애니메이션
        dimmedView.visibility = View.VISIBLE
        dimmedView.animate().alpha(1f).setDuration(300).start()

        isChatVisible = true

        findNavController().navigate(R.id.chatFragment, null, NavAnimationUtil.getVoiceChatToChatAnim())
    }

    private fun hideChatFragment() {
        // 흐림 배경 제거 애니메이션
        dimmedView.animate().alpha(0f).setDuration(300).withEndAction { dimmedView.visibility = View.GONE }.start()

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
        }
        else {
            dimmedView.visibility = View.GONE
            dimmedView.alpha = 0f
        }
    }
}
