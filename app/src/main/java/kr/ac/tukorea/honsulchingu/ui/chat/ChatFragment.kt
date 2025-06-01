package kr.ac.tukorea.honsulchingu.ui.chat

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.animation.ObjectAnimator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import kr.ac.tukorea.honsulchingu.databinding.FragmentChatBinding
import org.json.JSONObject
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.Date
import kotlin.random.Random

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var chatAdapter: ChatAdapter

    private val characterViewModel: CharacterViewModel by activityViewModels()

    private val chatItems = mutableListOf<ChatItem>()

    private val loadingMessages = listOf(
        "대화를 꺼내는 중이에요…",
        "생각의 스위치를 켜는 중이에요.",
        "기억과 술 사이를 잇는 중이에요.",
        "조용히 대화를 깨우는 중이에요.",
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰가 attach된 후 리스너 설정
        view.post { ViewCompat.requestApplyInsets(binding.chatRoot) } // 인셋 요청 (리스너 설정 후)

        // 1. 로딩 애니메이션 보이기 및 입력창, 보내기 버튼 잠금
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.loadingText.visibility = View.VISIBLE
        binding.recyclerViewChat.visibility = View.GONE
        binding.editTextMessage.isEnabled = false
        binding.buttonSend.isEnabled = false

        // 2. 로딩 텍스트 주기적 변경 시작
        handler.post(loadingTextRunnable)

        // 3. 리사이클러뷰 세팅
        setupRecyclerView()

        // 4. 무한대 로딩 UI 표시
        handler.postDelayed({ if (!isAdded || _binding == null) return@postDelayed }, Integer.MAX_VALUE.toLong()) // 무한대 대기

        // 5. 채팅 로딩
        loadChat()

        // btnCloseChat 버튼 클릭 시 VoiceChatFragment로 돌아가기
        binding.btnCloseChat.setOnClickListener {
            val navController = findNavController()
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_left)
                .setExitAnim(R.anim.slide_out_right)
                .setPopEnterAnim(R.anim.slide_in_right)
                .setPopExitAnim(R.anim.slide_out_left)
                .build()

            navController.navigate(R.id.nav_voiceChat, null, navOptions)
        }

        // 키보드가 올라왔을 때 입력창 마진을 동적으로 설정
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            // 키보드(IME) 높이 얻기
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val imeHeight = imeInsets.bottom

            val isKeyboardVisible = imeHeight > 0
            if (isKeyboardVisible) {
                // 디바이스 화면 높이 및 밀도 고려
                val screenHeight = resources.displayMetrics.heightPixels
                val screenDensity = resources.displayMetrics.density

                // 밀도 보정 적용한 오프셋 설정
                val offsetDp = 100
                val offsetPx = (offsetDp * screenDensity).toInt()
                val translation = imeHeight - offsetPx

                binding.containerUI.translationY = -translation.coerceAtLeast(0).toFloat()
            }
            else {
                binding.containerUI.translationY = 0f
            }

            // 시스템 바 인셋 적용
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                systemBars.right,
                view.paddingBottom
            )

            insets
        }

        // 키보드 올라갈 때 스크롤 자동화 및 플릭커 방지
        binding.recyclerViewChat.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) { // 화면이 위로 밀렸을 때 (키보드가 올라갔을 때)
                binding.recyclerViewChat.post {
                    val position = chatItems.size - 1
                    binding.recyclerViewChat.smoothScrollToPosition(position) // 부드럽게 맨 아래로 스크롤
                }
            }
        }

        binding.buttonSend.setOnClickListener {
            sendToServer(binding.editTextMessage.text.toString().trim(), System.currentTimeMillis())
            binding.editTextMessage.text.clear()
            binding.buttonSend.isEnabled = false
        }
    }

    private fun sendToServer(input_user: String, time_user: Long) {
        Thread {
            val startTime = System.currentTimeMillis()


            val context = context ?: return@Thread

            val sharedPreferences_setting = context.getSharedPreferences("prefs_setting", MODE_PRIVATE)

            val sharedPreferences_chat = context.getSharedPreferences("prefs_chat", MODE_PRIVATE)

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
            else {
                Handler(Looper.getMainLooper()).post { sendMessage(input_user, true, time_user) }
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

            val time_ai = LocalDateTime.parse(responseJson.getString("time_ai"), DateTimeFormatter.ofPattern("yyyy. MM. dd. HH-mm-ss")).atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli()

            val elapsedTime = System.currentTimeMillis() - startTime

            val delay = maxOf(0L, 3000L - elapsedTime)

            Handler(Looper.getMainLooper()).postDelayed({
                if (sharedPreferences_chat.getBoolean("isFirst", true)) sharedPreferences_chat.edit().putBoolean("isFirst", false).apply()

                sharedPreferences_chat.edit().putString("greet", output_ai).apply()
                characterViewModel.greet_live.value = output_ai

                if (!isAdded || _binding == null) return@postDelayed

                binding.buttonSend.isEnabled = true
                // TODO: 응답 중 ... 애니메이션 띄우기, sendButton에도 애니메이션 띄우기?

                sendMessage(output_ai, false, time_ai)
            }, delay) // 최소 3초 로딩 애니메이션 보장
        }.start()
    }

    private fun sendMessage(message: String, isUser: Boolean, timestamp: Long) {
        // 마지막 MessageItem 찾기 (날짜 구분선 제외)
        val lastMessageItem = chatItems.lastOrNull { it is ChatItem.MessageItem } as? ChatItem.MessageItem
        val previousMessage = lastMessageItem?.chatMessage

        val newMessage = ChatMessage(
            message = message,
            isUser = isUser,
            timestamp = timestamp
        )

        // 날짜 구분선 추가
        val lastDate = previousMessage?.let { formatDate(it.timestamp) }
        val currentDate = formatDate(newMessage.timestamp)
        if (lastDate == null || lastDate != currentDate) chatItems.add(ChatItem.DateDividerItem(currentDate))

        // 채팅 하단 자동 스크롤
        chatItems.add(ChatItem.MessageItem(newMessage))
        chatAdapter.submitList(chatItems.toList()) {
            val position = chatItems.size - 1
            if (position >= 0) binding.recyclerViewChat.scrollToPosition(position)
        }

        // 애니메이션 효과 추가
        binding.recyclerViewChat.post {
            val lastPosition = chatItems.size - 1
            val viewHolder = binding.recyclerViewChat.findViewHolderForAdapterPosition(lastPosition) as? ChatAdapter.UserViewHolder
            viewHolder?.let {
                ObjectAnimator.ofFloat(it.itemView, "alpha", 0f, 1f).apply {
                    duration = 300
                    start()
                }
            }
        }
    }

    private fun loadChat() {
        Thread {
            val startTime = System.currentTimeMillis()


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


            if (Messages.isEmpty()) sendToServer(sharedPreferences_setting.getString("BEGIN", "") ?: "", System.currentTimeMillis())


            var lastDate: String? = null

            for (message in Messages) {
                val currentDate = formatDate(message.timestamp)
                if (lastDate != currentDate) {
                    lastDate = currentDate
                    chatItems.add(ChatItem.DateDividerItem(currentDate))
                }
                chatItems.add(ChatItem.MessageItem(message))
            }

            val elapsedTime = System.currentTimeMillis() - startTime

            val delay = maxOf(0L, 3000L - elapsedTime)

            Handler(Looper.getMainLooper()).postDelayed({
                sharedPreferences_chat.edit().putString("greet", Messages.lastOrNull()?.message).apply()
                characterViewModel.greet_live.value = Messages.lastOrNull()?.message

                if (!isAdded || _binding == null) return@postDelayed

                binding.loadingAnimation.visibility = View.GONE
                binding.loadingText.visibility = View.GONE
                binding.recyclerViewChat.visibility = View.VISIBLE
                binding.editTextMessage.isEnabled = true
                binding.buttonSend.isEnabled = true
                handler.removeCallbacks(loadingTextRunnable)

                chatAdapter.submitList(chatItems.toList()) { binding.recyclerViewChat.scrollToPosition(chatItems.size - 1) }
            }, delay) // 최소 3초 로딩 애니메이션 보장
        }.start()
    }

    private val loadingTextRunnable = object : Runnable {
        override fun run() {
            binding.loadingText.text = loadingMessages[Random.nextInt(loadingMessages.size)]
            handler.postDelayed(this, 3500) // 3.5초 후 메시지 전환
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean { return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) }

    private fun formatDate(timestamp: Long): String {
        val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return when {
            isSameDay(messageDate, today) -> "오늘"
            isSameDay(messageDate, yesterday) -> "어제"
            else -> {
                val sdf = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        chatAdapter = ChatAdapter(binding.root.context, characterViewModel)
        binding.recyclerViewChat.adapter = chatAdapter
        binding.recyclerViewChat.layoutManager = layoutManager
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(loadingTextRunnable)
    }
}
