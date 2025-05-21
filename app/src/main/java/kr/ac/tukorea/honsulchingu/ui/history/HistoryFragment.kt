package kr.ac.tukorea.honsulchingu.ui.history

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.FragmentHistoryBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

        load_last()

        historyAdapter = HistoryAdapter(chatList) { chatRecord ->
            // bundle에 select_user, start_user 담기
            val bundle = Bundle().apply {
                putString("select_user", chatRecord.name)
                putString("start_user", chatRecord.start_time)
            }

            // ChatFragment에 보내기
            findNavController().navigate(R.id.chatFragment, bundle)
        }

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private var chatList: MutableList<ChatRecord> = mutableListOf()

    private fun createURL(endPoint: String): URL {
        val IPv4 = "13.208.186.203"
        return URL("http://$IPv4:8000$endPoint")
    }

    private fun load_last() {
        Thread {
            val url = createURL("/load_last")

            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.doOutput = true


            // arguments?.getString("id_user")?.let {
            //     chatViewModel.id_user = it
            // }

            val jsonInput = JSONObject()

            // jsonInput.put("id_user", chatViewModel.id_user)
            jsonInput.put("id_user", "alps1248@gmail.com")
            jsonInput.put("select_user", "")
            jsonInput.put("input_user", "")
            jsonInput.put("time_user", "")
            jsonInput.put("start_user", "")
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

            val responseJsonArray = responseJsonObject.getJSONArray("last")

            val chatList_temp = mutableListOf<ChatRecord>()

            for (i in 0 until responseJsonArray.length()) {
                val responseJsonItem = responseJsonArray.getJSONObject(i)

                val name = responseJsonItem.getString("select_user")

                val last_chat = responseJsonItem.getString("text")

                val last_time = LocalDateTime.parse(responseJsonItem.getString("time"), DateTimeFormatter.ofPattern("yyyy. MM. dd. HH-mm-ss")).atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli()

                val start_time = responseJsonItem.getString("start")

                val tagJsonArray = responseJsonItem.getJSONArray("tag")

                val tag = (0 until tagJsonArray.length()).map { i -> tagJsonArray.getString(i) }

                val profileImageRes = requireContext().resources.getIdentifier(responseJsonItem.getString("image"), "drawable", requireContext().packageName)

                chatList_temp.add(ChatRecord(name, last_chat, last_time, start_time, tag, profileImageRes))
            }

            Handler(Looper.getMainLooper()).post {
                chatList.clear()
                chatList.addAll(chatList_temp)
                historyAdapter.notifyDataSetChanged()
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
