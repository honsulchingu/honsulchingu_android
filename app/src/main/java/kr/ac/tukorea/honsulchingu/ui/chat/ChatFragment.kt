package kr.ac.tukorea.honsulchingu.ui.chat

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
import kr.ac.tukorea.honsulchingu.viewmodel.ChatViewModel
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
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
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        loadChat()

        // btnCloseChat 버튼 클릭 시 VoiceChatFragment로 돌아가기
        val btnCloseChat: ImageButton = binding.root.findViewById(R.id.btnCloseChat)

        val params = binding.layoutChatInput.layoutParams as ViewGroup.MarginLayoutParams

        defaultBottomMargin = params.bottomMargin

        btnCloseChat.setOnClickListener {
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val imeHeight = imeInsets.bottom
            val isKeyboardVisible = imeHeight > 0

            binding.layoutChatInput.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = if (isKeyboardVisible) {
                    (imeHeight * 0.8).toInt() // 키보드가 올라왔을 때 입력창과의 간격 조정
                } else {
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

    private fun createURL(endPoint: String): URL {
        val IPv4 = "13.208.186.203"
        return URL("http://$IPv4:8000$endPoint")
    }

    private fun sendToServer(input_user: String, time_user: Long) {
        Thread {
            val url = createURL("/conversation_model")

            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.doOutput = true


            // arguments?.getString("id_user")?.let {
            //     chatViewModel.id_user = it
            // }

            arguments?.getString("select_user")?.let {
                chatViewModel.select_user = it
            }

            arguments?.getString("start_user")?.let {
                chatViewModel.start_user = it
            }

            val jsonInput = JSONObject()

            sendMessage(input_user, true, time_user)
            // jsonInput.put("id_user", chatViewModel.id_user)
            jsonInput.put("id_user", "alps1248@gmail.com")
            jsonInput.put("select_user", chatViewModel.select_user)
            jsonInput.put("input_user", input_user)
            jsonInput.put("time_user", SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(time_user)))
            jsonInput.put("start_user", chatViewModel.start_user)
            jsonInput.put("shown_user", "true")


            val outputStream: OutputStream = connection.outputStream

            outputStream.write(jsonInput.toString().toByteArray(Charsets.UTF_8))
            outputStream.flush()
            outputStream.close()


            val reader = BufferedReader(InputStreamReader(connection.inputStream))

            val responseBuilder = StringBuilder()

            var line: String?

            while (reader.readLine().also { line = it } != null) responseBuilder.append(line)

            reader.close()


            val responseJson = JSONObject(responseBuilder.toString())

            val output_ai = responseJson.getString("output_ai")

            val time_ai = LocalDateTime.parse(responseJson.getString("time_ai"), DateTimeFormatter.ofPattern("yyyy. MM. dd. HH-mm-ss")).atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli()

            sendMessage(output_ai, false, time_ai)

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
        chatAdapter.submitList(chatItems.toList())
        Handler(Looper.getMainLooper()).postDelayed({
            val position = chatItems.size - 1
            val layoutManager = binding.recyclerViewChat.layoutManager as LinearLayoutManager

            // 맨 아래로 스크롤하기 전에 리스트가 갱신되었는지 확인
            if (position > layoutManager.findLastVisibleItemPosition()) {
                layoutManager.scrollToPositionWithOffset(position, 0)
            }
        }, 5) // 5ms 지연

        // 애니메이션 효과 추가
        binding.recyclerViewChat.post {
            val lastPosition = chatItems.size - 1
            val viewHolder =
                binding.recyclerViewChat.findViewHolderForAdapterPosition(lastPosition) as? ChatAdapter.UserViewHolder
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
            val url = createURL("/load_chat")

            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.doOutput = true


            // arguments?.getString("id_user")?.let {
            //     chatViewModel.id_user = it
            // }

            arguments?.getString("select_user")?.let {
                chatViewModel.select_user = it
            }

            arguments?.getString("start_user")?.let {
                chatViewModel.start_user = it
            }

            val jsonInput = JSONObject()

            // jsonInput.put("id_user", chatViewModel.id_user)
            jsonInput.put("id_user", "alps1248@gmail.com")
            jsonInput.put("select_user", chatViewModel.select_user)
            jsonInput.put("input_user", "")
            jsonInput.put("time_user", "")
            jsonInput.put("start_user", chatViewModel.start_user)
            jsonInput.put("shown_user", "true")


            val outputStream: OutputStream = connection.outputStream

            outputStream.write(jsonInput.toString().toByteArray(Charsets.UTF_8))
            outputStream.flush()
            outputStream.close()


            val reader = BufferedReader(InputStreamReader(connection.inputStream))

            val responseBuilder = StringBuilder()

            var line: String?

            while (reader.readLine().also { line = it } != null) responseBuilder.append(line)

            reader.close()


            val responseJsonObject = JSONObject(responseBuilder.toString())

            val responseJsonArray = responseJsonObject.getJSONArray("chat")

            val Messages = mutableListOf<ChatMessage>()

            Messages.add(ChatMessage("인물 별 한 마디", false, System.currentTimeMillis()))

            for (i in 0 until responseJsonArray.length()) {
                val responseJsonItem = responseJsonArray.getJSONObject(i)

                val message = responseJsonItem.getString("text")

                val isUser = responseJsonItem.getString("role") == "user"

                val timestamp = LocalDateTime.parse(responseJsonItem.getString("time"), DateTimeFormatter.ofPattern("yyyy. MM. dd. HH-mm-ss")).atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli()

                Messages.add(ChatMessage(message, isUser, timestamp))
            }


            var lastDate: String? = null

            for (message in Messages) {
                val currentDate = formatDate(message.timestamp)

                if (lastDate != currentDate) {
                    chatItems.add(ChatItem.DateDividerItem(currentDate))
                    lastDate = currentDate
                }

                chatItems.add(ChatItem.MessageItem(message))
            }

            Handler(Looper.getMainLooper()).postDelayed({
                chatAdapter.submitList(chatItems.toList())
                binding.recyclerViewChat.scrollToPosition(chatItems.size - 1)
            }, 450) // 450ms 지연
        }.start()
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
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
        chatAdapter = ChatAdapter()
        binding.recyclerViewChat.adapter = chatAdapter
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
