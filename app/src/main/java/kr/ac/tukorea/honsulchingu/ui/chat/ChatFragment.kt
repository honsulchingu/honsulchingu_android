package kr.ac.tukorea.honsulchingu.ui.chat

import android.animation.ObjectAnimator
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.FragmentChatBinding
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import org.json.JSONObject
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var defaultBottomMargin: Int = 0

    private lateinit var chatAdapter: ChatAdapter
    private val chatItems = mutableListOf<ChatItem>()
    private val characterViewModel: CharacterViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        val sharedPreferences_chat = requireContext().getSharedPreferences("prefs_chat", MODE_PRIVATE)

        binding.editTextMessage.isEnabled = !sharedPreferences_chat.getBoolean("isFirst", true)
        binding.buttonSend.isEnabled = !sharedPreferences_chat.getBoolean("isFirst", true)
        binding.btnCloseChat.isEnabled = !sharedPreferences_chat.getBoolean("isFirst", true)

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
        val params = binding.layoutChatInput.layoutParams as ViewGroup.MarginLayoutParams

        defaultBottomMargin = params.bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val imeHeight = imeInsets.bottom
            val isKeyboardVisible = imeHeight > 0

            binding.layoutChatInput.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = if (isKeyboardVisible) {
                    (imeHeight * 0.8).toInt() // 키보드가 올라왔을 때 입력창과의 간격 조정
                }
                else {
                    defaultBottomMargin
                }
            }

            // 입력창이 가려지지 않도록 RecyclerView 자동 스크롤
            if (isKeyboardVisible) {
                binding.recyclerViewChat.post {
                    binding.recyclerViewChat.scrollToPosition(chatItems.size - 1)
                }
            }

            WindowInsetsCompat.CONSUMED
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
        }
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


            if (!sharedPreferences_chat.getBoolean("isFirst", true)) {
                Handler(Looper.getMainLooper()).post {
                    sendMessage(input_user, true, time_user)
                }
            }

            val jsonInput = JSONObject().apply {
                put("id_user", sharedPreferences_setting.getString("EMAIL", ""))
                put("select_user", sharedPreferences_chat.getString("select_user", ""))
                put("input_user", input_user)
                put("time_user", SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(time_user)))
                put("start_user", sharedPreferences_chat.getString("start_user", ""))
                put("shown_user", "true")
            }

            connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


            val responseJson = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })

            val output_ai = responseJson.getString("output_ai")

            val time_ai = LocalDateTime.parse(responseJson.getString("time_ai"), DateTimeFormatter.ofPattern("yyyy. MM. dd. HH-mm-ss")).atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli()

            Handler(Looper.getMainLooper()).post {
                if (sharedPreferences_chat.getBoolean("isFirst", true)) sharedPreferences_chat.edit().putBoolean("isFirst", false).apply()

                if (!isAdded || _binding == null) return@post

                if (!binding.editTextMessage.isEnabled && !binding.buttonSend.isEnabled) {
                    binding.editTextMessage.isEnabled = true
                    binding.buttonSend.isEnabled = true
                    binding.btnCloseChat.isEnabled = true
                }

                sendMessage(output_ai, false, time_ai)
            }
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

        if (lastDate == null || lastDate != currentDate) {
            chatItems.add(ChatItem.DateDividerItem(currentDate))
        }

        chatItems.add(ChatItem.MessageItem(newMessage))
        chatAdapter.submitList(chatItems.toList()) {
            val position = chatItems.size - 1
            if (position >= 0) {
                binding.recyclerViewChat.scrollToPosition(position)
            }
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
                put("shown_user", "true")
            }

            connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


            val responseJson = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })

            val Messages = responseJson.getJSONArray("chat").let { array ->
                List(array.length()) { i ->
                    val obj = array.getJSONObject(i)
                    ChatMessage(obj.getString("text"), obj.getString("role") == "user", LocalDateTime.parse(obj.getString("time"), DateTimeFormatter.ofPattern("yyyy. MM. dd. HH-mm-ss")).atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli())
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

            Handler(Looper.getMainLooper()).postDelayed({
                chatAdapter.submitList(chatItems.toList()) { binding.recyclerViewChat.scrollToPosition(chatItems.size - 1) }
            }, 450) // 450ms 지연
        }.start()
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

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
        chatAdapter = ChatAdapter(requireContext())
        binding.recyclerViewChat.adapter = chatAdapter
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
