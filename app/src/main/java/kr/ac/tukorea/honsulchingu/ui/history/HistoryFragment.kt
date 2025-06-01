package kr.ac.tukorea.honsulchingu.ui.history

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import java.net.HttpURLConnection
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import kr.ac.tukorea.honsulchingu.navigation.NavAnimationUtil
import kr.ac.tukorea.honsulchingu.databinding.FragmentHistoryBinding
import org.json.JSONObject

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var historyAdapter: HistoryAdapter

    private val characterViewModel: CharacterViewModel by activityViewModels()

    private var chatList: MutableList<ChatRecord> = mutableListOf()

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadLast()

        // 초기 UI 설정
        val sharedPreferences_history = requireContext().getSharedPreferences("prefs_history", MODE_PRIVATE)

        if (sharedPreferences_history.getBoolean("isDeleted", false)) {
            binding.loadingAnimation.visibility = View.GONE
            binding.loadingText.visibility = View.GONE
            binding.chatRecyclerView.visibility = View.VISIBLE
        }
        else {
            binding.loadingAnimation.visibility = View.VISIBLE
            binding.loadingText.visibility = View.VISIBLE
            binding.chatRecyclerView.visibility = View.GONE

            // 로딩 메시지 애니메이션 시작
            handler.post(loadingTextRunnable)

            // 무한대 로딩 애니메이션
            view.postDelayed({ if (!isAdded || _binding == null) return@postDelayed }, Integer.MAX_VALUE.toLong()) // 무한대 대기
        }
    }

    private fun loadLast() {
        Thread {
            val startTime = System.currentTimeMillis()


            val context = context ?: return@Thread

            val sharedPreferences_setting = context.getSharedPreferences("prefs_setting", MODE_PRIVATE)

            val sharedPreferences_history = context.getSharedPreferences("prefs_history", MODE_PRIVATE)

            val sharedPreferences_chat = context.getSharedPreferences("prefs_chat", MODE_PRIVATE)

            val jsonString = sharedPreferences_history.getString("savedChatList", "[]")

            val savedChatList = try { org.json.JSONArray(jsonString).let { jsonArray -> MutableList(jsonArray.length()) { i -> ChatRecord.fromJson(jsonArray.getJSONObject(i)) } } }
            catch (e: Exception) { mutableListOf() }


            val loadedChatList = run {
                val url = characterViewModel.updateURL("/load_last")

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    doOutput = true
                }


                val jsonInput = JSONObject().apply {
                    put("id_user", sharedPreferences_setting.getString("EMAIL", ""))
                    put("select_user", "")
                    put("input_user", "")
                    put("time_user", "")
                    put("start_user", "")
                }

                connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                val responseString = connection.inputStream.bufferedReader().use { it.readText() }

                val responseJsonObject = JSONObject(responseString)

                val responseJsonArray = responseJsonObject.getJSONArray("last")

                MutableList(responseJsonArray.length()) { i ->
                    val item = responseJsonArray.getJSONObject(i)
                    val name = item.getString("select_user")
                    val last_chat = item.getString("text")
                    val last_time = LocalDateTime.parse(item.getString("time"), DateTimeFormatter.ofPattern("yyyy. MM. dd. HH-mm-ss")).atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli()
                    val start_time = item.getString("start")
                    val image = context.resources.getIdentifier(item.getString("image"), "drawable", context.packageName)
                    val isFavorite = item.getString("favorite") != ""
                    ChatRecord(name, last_chat, last_time, start_time, listOf(), image, isFavorite)
                }
            }


            val needTagUpdateChats = loadedChatList.filter { loaded ->
                val match = savedChatList.find { it.start_time == loaded.start_time }
                match == null || match.last_time != loaded.last_time
            }


            val tagUpdatedChats = needTagUpdateChats.map { chat ->
                val url = characterViewModel.updateURL("/create_tag")

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    doOutput = true
                }


                val jsonInput = JSONObject().apply {
                    put("id_user", sharedPreferences_setting.getString("EMAIL", ""))
                    put("select_user", chat.name)
                    put("input_user", sharedPreferences_setting.getString("TAG", ""))
                    put("time_user", "")
                    put("start_user", chat.start_time)
                }

                connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                val responseString = connection.inputStream.bufferedReader().use { it.readText() }

                val responseJson = JSONObject(responseString)

                val tagArray = responseJson.getJSONArray("tag")

                val tagList = List(tagArray.length()) { i -> tagArray.getString(i) }

                chat.copy(tag = tagList)
            }


            val updatedChatList = savedChatList.filter { saved -> needTagUpdateChats.none { it.start_time == saved.start_time } } + tagUpdatedChats


            val chatList_temp = updatedChatList.sortedByDescending { it.last_time }.toMutableList()

            val elapsedTime = System.currentTimeMillis() - startTime

            var delay = maxOf(0L, 3000L - elapsedTime)

            if (sharedPreferences_history.getBoolean("isDeleted", false)) {
                delay = 0L
                sharedPreferences_history.edit().putBoolean("isDeleted", false).apply()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (!isAdded || _binding == null) return@postDelayed

                chatList.clear()
                chatList.addAll(chatList_temp)

                sharedPreferences_setting.edit().putInt("CHATCOUNT", chatList.size).apply()
                characterViewModel.chatcount_live.value = chatList.size

                if (chatList.isEmpty()) binding.loadingInitText.visibility = View.VISIBLE
                else binding.loadingInitText.visibility = View.INVISIBLE

                historyAdapter = HistoryAdapter(
                    chatList,
                    onMoveClick = { chatRecord ->
                        sharedPreferences_chat.edit().apply {
                            putString("select_user", chatRecord.name)
                            putString("start_user", chatRecord.start_time)
                            putString("greet", chatRecord.last_chat)
                            putInt("image", chatRecord.image)
                            putBoolean("isFirst", false)
                            putBoolean("isSelected", true)
                            apply()
                        }

                        characterViewModel.greet_live.value = chatRecord.last_chat
                        characterViewModel.isSelected_live.value = true

                        findNavController().navigate(R.id.nav_voiceChat, null, NavAnimationUtil.getSlideFromRightOptions())
                    },
                    onDeleteClick = { chatRecord ->
                        Thread {
                            val url = characterViewModel.updateURL("/delete_chat")

                            val connection = (url.openConnection() as HttpURLConnection).apply {
                                requestMethod = "POST"
                                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                                doOutput = true
                            }


                            val jsonInput = JSONObject().apply {
                                put("id_user", sharedPreferences_setting.getString("EMAIL", ""))
                                put("select_user", chatRecord.name)
                                put("input_user", "")
                                put("time_user", "")
                                put("start_user", chatRecord.start_time)
                            }

                            connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                            val responseString = connection.inputStream.bufferedReader().use { it.readText() }

                            val responseJson = JSONObject(responseString)

                            val favoriteCount = responseJson.getInt("favorite_count")

                            val savedList = org.json.JSONArray(sharedPreferences_history.getString("savedChatList", "[]"))

                            val updatedList = org.json.JSONArray()

                            for (i in 0 until savedList.length()) {
                                val obj = savedList.getJSONObject(i)
                                if (obj.getString("start_time") != chatRecord.start_time) updatedList.put(obj)
                            }

                            sharedPreferences_history.edit().putString("savedChatList", updatedList.toString()).apply()

                            sharedPreferences_history.edit().putBoolean("isDeleted", true).apply()

                            Handler(Looper.getMainLooper()).post {
                                sharedPreferences_chat.edit().putString("greet", "혼술친구를 먼저 정해주세요").apply()
                                characterViewModel.greet_live.value = "혼술친구를 먼저 정해주세요"

                                sharedPreferences_setting.edit().putInt("CHATCOUNT", sharedPreferences_setting.getInt("CHATCOUNT", 0) - 1).apply()
                                characterViewModel.chatcount_live.value = sharedPreferences_setting.getInt("CHATCOUNT", 0)

                                sharedPreferences_setting.edit().putInt("FAVORITECOUNT", sharedPreferences_setting.getInt("FAVORITECOUNT", 0) - favoriteCount).apply()
                                characterViewModel.favoritecount_live.value = sharedPreferences_setting.getInt("FAVORITECOUNT", 0)

                                sharedPreferences_chat.edit().putBoolean("isSelected", false).apply()
                                characterViewModel.isSelected_live.value = false

                                findNavController().navigate(R.id.nav_history)
                            }
                        }.start()
                    }
                )

                binding.chatRecyclerView.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = historyAdapter
                }

                binding.loadingAnimation.visibility = View.GONE
                binding.loadingText.visibility = View.GONE
                binding.chatRecyclerView.visibility = View.VISIBLE
                handler.removeCallbacks(loadingTextRunnable)
            }, delay) // 최소 3초 로딩 애니메이션 보장


            val editor = sharedPreferences_history.edit()

            val jsonArray = org.json.JSONArray()

            for (chat in chatList_temp) jsonArray.put(chat.toJson())

            editor.putString("savedChatList", jsonArray.toString())

            editor.apply()
        }.start()
    }

    private val loadingTextRunnable = object : Runnable {
        override fun run() {
            binding.loadingText.text = loadingMessages[Random.nextInt(loadingMessages.size)]
            handler.postDelayed(this, 3500) // 3.5초 후 메시지 전환
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(loadingTextRunnable)
    }
}
