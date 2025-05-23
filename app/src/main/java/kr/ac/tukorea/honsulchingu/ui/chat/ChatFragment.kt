package kr.ac.tukorea.honsulchingu.ui.chat

import ChatAdapter
import android.animation.ObjectAnimator
import android.content.res.Resources
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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.FragmentChatBinding

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random


class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var defaultBottomMargin: Int = 0

    private lateinit var chatAdapter: ChatAdapter
    private val chatItems = mutableListOf<ChatItem>()

    private val handler = Handler(Looper.getMainLooper())

    // 로딩 메시지 목록
    private val loadingMessages = listOf(
        "대화를 꺼내는 중이에요…",
        "생각의 스위치를 켜는 중이에요.",
        "기억과 술 사이를 잇는 중이에요.",
        "조용히 대화를 깨우는 중이에요.",
    )

    // 1초마다 로딩 메시지 변경하는 Runnable
    private val loadingTextRunnable = object : Runnable {
        override fun run() {
            // 메시지 랜덤 또는 순차 선택 (여기선 랜덤)
            val randomIndex = Random.nextInt(loadingMessages.size)
            binding.loadingText.text = loadingMessages[randomIndex]

            // 3.5초 후 다시 실행
            handler.postDelayed(this, 3500)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 로딩 애니메이션 보이기
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.loadingText.visibility = View.VISIBLE
        binding.recyclerViewChat.visibility = View.GONE

        // 2. 로딩 텍스트 주기적 변경 시작
        handler.post(loadingTextRunnable)

        // 3. 리사이클러뷰 세팅 및 더미 데이터 로드
        setupRecyclerView()
        loadTestData()

        // 4. 일정 시간 후 로딩 UI 제거
        handler.postDelayed({
            binding.loadingAnimation.visibility = View.GONE
            binding.loadingText.visibility = View.GONE
            binding.recyclerViewChat.visibility = View.VISIBLE
            handler.removeCallbacks(loadingTextRunnable)
        }, 3000) // 3초 후 제거. 필요 시 데이터 로딩 완료 기준으로 조정 가능


        setupRecyclerView()
        loadTestData()

        val params = binding.layoutChatInput.layoutParams as ViewGroup.MarginLayoutParams
        defaultBottomMargin = params.bottomMargin


        // btnCloseChat 버튼 클릭 시 VoiceChatFragment로 돌아가기
        val btnCloseChat: ImageButton = binding.root.findViewById(R.id.btnCloseChat)

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val imeHeight = imeInsets.bottom
            val isKeyboardVisible = imeHeight > 0

            binding.layoutChatInput.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = if (isKeyboardVisible) {
                    (imeHeight * 0.05 ).toInt()  // 키보드가 올라왔을 때 입력창과의 간격 조정
                } else {
                    defaultBottomMargin
                }
            }

            // 입력창이 가려지지 않도록 `RecyclerView` 자동 스크롤
            if (isKeyboardVisible) {
                binding.recyclerViewChat.post {
                    binding.recyclerViewChat.scrollToPosition(chatItems.size - 1)
                }
            }

            WindowInsetsCompat.CONSUMED
        }

// 키보드 올라갈 때 스크롤 자동화 및 플릭커 방지
        binding.recyclerViewChat.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {  // 화면이 위로 밀렸을 때 (키보드가 올라갔을 때)
                binding.recyclerViewChat.post {
                    val position = chatItems.size - 1
                    binding.recyclerViewChat.smoothScrollToPosition(position)  // 부드럽게 맨 아래로 스크롤
                }
            }
        }

        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.editTextMessage.text.clear()
            }
        }



    }

    // 메시지 전송 후 항상 부드럽게 맨 아래로 스크롤
    fun scrollToBottom() {
        binding.recyclerViewChat.post {
            val position = chatItems.size - 1
            val layoutManager = binding.recyclerViewChat.layoutManager as LinearLayoutManager

            // 맨 아래로 스크롤하기 전에 리스트가 갱신되었는지 확인
            if (position > layoutManager.findLastVisibleItemPosition()) {
                layoutManager.scrollToPositionWithOffset(chatItems.size - 1, 0)
            }
        }
    }




    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.recyclerViewChat.adapter = chatAdapter
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun sendMessage(text: String) {
        val timestamp = System.currentTimeMillis()

        // 마지막 MessageItem 찾기 (날짜 구분선 제외)
        val lastMessageItem = chatItems.lastOrNull { it is ChatItem.MessageItem } as? ChatItem.MessageItem
        val previousMessage = lastMessageItem?.chatMessage

        val newMessage = ChatMessage(
            message = text,
            isUser = true,
            timestamp = timestamp,
            showTime = true
        )

        // 시간 비교
        if (previousMessage != null && isSameMinute(previousMessage.timestamp, newMessage.timestamp)) {
            val index = chatItems.indexOfLast {
                it is ChatItem.MessageItem && it.chatMessage.timestamp == previousMessage.timestamp
            }
            if (index != -1) {
                val updated = (chatItems[index] as ChatItem.MessageItem).chatMessage.copy(showTime = false)
                chatItems[index] = ChatItem.MessageItem(updated)
            }
        }

        // 날짜 구분선 추가
        val lastDate = previousMessage?.let { formatDate(it.timestamp) }
        val currentDate = formatDate(newMessage.timestamp)

        if (lastDate == null || lastDate != currentDate) {
            chatItems.add(ChatItem.DateDividerItem(currentDate))
        }

        chatItems.add(ChatItem.MessageItem(newMessage))
        chatAdapter.submitList(chatItems.toList()) // 이때마다 리스트 전체를 갱신하면 스크롤 문제 발생할 수 있음

        // 새 메시지가 추가된 후에만 스크롤
        binding.recyclerViewChat.post {
            scrollToBottom()
        }

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

    private fun loadTestData() {
        val now = System.currentTimeMillis()
        val minute = 60 * 1000L

        val dummyMessages = listOf(
            ChatMessage("안녕하세요!", isUser = false, timestamp = now - 10 * minute),
            ChatMessage("네, 반갑습니다!", isUser = true, timestamp = now - 10 * minute),
            ChatMessage("오늘 날씨 좋네요", isUser = false, timestamp = now - 9 * minute),
            ChatMessage("진짜요?", isUser = true, timestamp = now - 8 * minute),
            ChatMessage("네! 맑아요", isUser = false, timestamp = now - 8 * minute),
            ChatMessage("좋네요ㅎㅎ", isUser = true, timestamp = now - 7 * minute),
            ChatMessage("오이아아이아이오오이ㅚㅏ와ㅣ외ㅏ외외ㅏ오아ㅣㅘ아ㅣ산책 추천드립니다...뀨?", isUser = false, timestamp = now - 6 * minute)
        )

        var lastDate: String? = null

        for (message in dummyMessages) {
            val currentDate = formatDate(message.timestamp)

            if (lastDate != currentDate) {
                chatItems.add(ChatItem.DateDividerItem(currentDate))
                lastDate = currentDate
            }

            chatItems.add(ChatItem.MessageItem(message))
        }

        chatAdapter.submitList(chatItems.toList())
        binding.recyclerViewChat.scrollToPosition(chatItems.size - 1)
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

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }


    private fun isSameMinute(time1: Long, time2: Long): Boolean {
        val sdf = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
        return sdf.format(Date(time1)) == sdf.format(Date(time2))
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(loadingTextRunnable)
    }

    companion object {
        private const val ARG_CHARACTER_ID = "character_id"


    }
}


