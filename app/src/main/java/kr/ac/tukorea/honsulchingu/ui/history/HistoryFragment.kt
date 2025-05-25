package kr.ac.tukorea.honsulchingu.ui.history

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.FragmentHistoryBinding
import kr.ac.tukorea.honsulchingu.navigation.NavAnimationUtil
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: HistoryAdapter

    private val handler = Handler(Looper.getMainLooper())

    // 로딩 메시지 목록
    private val loadingMessages = listOf(
        "대화 기록 불러오는 중이에요…",
        "추억 한 장씩 넘기는 중이에요.",
        "기억을 한 모금씩 따르고 있어요.",
        "조용히 대화를 깨우는 중이에요.",
        "기린은 태어나자마자 2m에서 떨어진대요.",
        "수달은 서로 손잡고 잔다고 해요.",
        "사람은 평균 8초 안에 집중을 잃는대요.",
        "고양이는 술 냄새를 싫어한대요.",
        "맥주는 탄산 때문에 흡수가 더 빨라져요.",
        "올빼미는 얼굴 움직임 없이 소리 방향을 구별해요.",
        "초콜릿이랑 술은 같이 먹으면 흡수가 빨라져요.",
        "차가운 잔이 술맛을 20% 더 좋게 느끼게 한대요."
    )

    // 1초마다 로딩 메시지 변경하는 Runnable
    private val loadingTextRunnable = object : Runnable {
        override fun run() {
            // 메시지 랜덤 또는 순차 선택 (여기선 랜덤)
            val randomIndex = Random.nextInt(loadingMessages.size)
            binding.loadingText.text = loadingMessages[randomIndex]

            // 1초 후 다시 실행
            handler.postDelayed(this, 3500)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 UI 설정
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.loadingText.visibility = View.VISIBLE
        binding.chatRecyclerView.visibility = View.GONE

        // 로딩 메시지 애니메이션 시작
        handler.post(loadingTextRunnable)

        // 예시: 5초 후 데이터 로딩 (실제 앱에서는 ViewModel과 LiveData 활용 권장)
        view.postDelayed({
            historyAdapter = HistoryAdapter(
                chatList,
                onMoveClick = { chatRecord ->
                    findNavController().navigate(
                        R.id.nav_voiceChat,
                        null,
                        NavAnimationUtil.getSlideFromRightOptions()
                    )
                },
                onDeleteClick = { chatRecord ->
                    val position = chatList.indexOf(chatRecord)
                    if (position != -1) {
                        // 대화기록 삭제 처리
                    }
                }
            )

            binding.chatRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = historyAdapter
            }

            // 로딩 애니메이션 및 텍스트 숨기기
            binding.loadingAnimation.visibility = View.GONE
            binding.loadingText.visibility = View.GONE
            binding.chatRecyclerView.visibility = View.VISIBLE

            // 메시지 변경 중단
            handler.removeCallbacks(loadingTextRunnable)

        }, 2000)
    }

    private fun parseDateToMillis(dateString: String): Long {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.parse(dateString)?.time ?: System.currentTimeMillis()
    }

    private val chatList = listOf(
        ChatRecord(
            name = "민혁",
            time = parseDateToMillis("2024-04-01 22:30"),
            last_message = "오늘 고생했어~",
            tags = listOf("진지한", "위로"),
            profileImageRes = R.drawable.friend1
        ),
        ChatRecord(
            name = "지수",
            time = parseDateToMillis("2024-04-02 20:15"),
            last_message = "한잔 어때?",
            tags = listOf("가벼운", "친근한"),
            profileImageRes = R.drawable.friend2
        )
    )

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(loadingTextRunnable)
        _binding = null
    }
}
