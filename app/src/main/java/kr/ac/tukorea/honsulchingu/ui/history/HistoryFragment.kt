package kr.ac.tukorea.honsulchingu.history

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.FragmentHistoryBinding
import kr.ac.tukorea.honsulchingu.ui.chat.ChatFragment
import kr.ac.tukorea.honsulchingu.ui.voice.VoiceChatFragment
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        historyAdapter = HistoryAdapter(chatList) { chatRecord ->
            findNavController().navigate(R.id.action_historyFragment_to_voiceChatFragment)
        }


        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
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
        ),
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



