package kr.ac.tukorea.honsulchingu.ui.history

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.FragmentHistoryBinding
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import org.json.JSONObject
import java.net.HttpURLConnection
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: HistoryAdapter

    private var chatList: MutableList<ChatRecord> = mutableListOf()
    private val characterViewModel: CharacterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        load_last()

        historyAdapter = HistoryAdapter(chatList) { chatRecord ->
            val sharedPreferences_chat = requireContext().getSharedPreferences("prefs_chat", MODE_PRIVATE)

            sharedPreferences_chat.edit().apply {
                putString("select_user", chatRecord.name)
                putString("start_user", chatRecord.start_time)
                putBoolean("isFirst", false)
                apply()
            }

            findNavController().navigate(R.id.chatFragment)
        }

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun load_last() {
        Thread {
            val sharedPreferences_history = requireContext().getSharedPreferences("prefs_history", MODE_PRIVATE)

            val sharedPreferences_setting = requireContext().getSharedPreferences("prefs_setting", MODE_PRIVATE)

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
                    put("shown_user", "true")
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
                    val profileImageRes = requireContext().resources.getIdentifier(item.getString("image"), "drawable", requireContext().packageName)
                    ChatRecord(name, last_chat, last_time, start_time, listOf(), profileImageRes)
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


                val jsonInput = JSONObject()

                jsonInput.put("id_user", sharedPreferences_setting.getString("EMAIL", ""))
                jsonInput.put("select_user", chat.name)
                jsonInput.put("input_user", sharedPreferences_setting.getString("TAG", ""))
                jsonInput.put("time_user", "")
                jsonInput.put("start_user", chat.start_time)
                jsonInput.put("shown_user", "true")

                connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                val responseString = connection.inputStream.bufferedReader().use { it.readText() }

                val responseJson = JSONObject(responseString)

                val tagArray = responseJson.getJSONArray("tag")

                val tagList = List(tagArray.length()) { i -> tagArray.getString(i) }

                chat.copy(tag = tagList)
            }


            val updatedChatList = savedChatList.filter { saved -> needTagUpdateChats.none { it.start_time == saved.start_time } } + tagUpdatedChats

            val chatList_temp = updatedChatList.sortedByDescending { it.last_time }.toMutableList()

            Handler(Looper.getMainLooper()).post {
                chatList.clear()
                chatList.addAll(chatList_temp)
                historyAdapter.notifyDataSetChanged()
            }


            val editor = sharedPreferences_history.edit()

            val jsonArray = org.json.JSONArray()

            for (chat in chatList_temp) jsonArray.put(chat.toJson())

            editor.putString("savedChatList", jsonArray.toString())

            editor.apply()
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
