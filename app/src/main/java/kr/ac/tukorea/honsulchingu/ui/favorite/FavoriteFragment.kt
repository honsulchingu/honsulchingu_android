package kr.ac.tukorea.honsulchingu.ui.favorite

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
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Date
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import kr.ac.tukorea.honsulchingu.navigation.NavAnimationUtil
import kr.ac.tukorea.honsulchingu.databinding.FragmentFavoriteBinding
import org.json.JSONObject

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoriteAdapter: FavoriteAdapter

    private val characterViewModel: CharacterViewModel by activityViewModels()

    private val favoriteList = mutableListOf<FavoriteChat>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Thread {
            val sharedPreferences_setting = requireContext().getSharedPreferences("prefs_setting", MODE_PRIVATE)

            val sharedPreferences_chat = requireContext().getSharedPreferences("prefs_chat", MODE_PRIVATE)

            val loadedFavoriteList = run {
                val url = characterViewModel.updateURL("/load_favorite")

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

                val responseJsonArray = responseJsonObject.getJSONArray("favorite")

                MutableList(responseJsonArray.length()) { i ->
                    val item = responseJsonArray.getJSONObject(i)
                    val name = item.getString("select_user")
                    val message = item.getString("text")
                    val time = LocalDateTime.parse(item.getString("time"), DateTimeFormatter.ofPattern("yyyy. MM. dd. HH-mm-ss")).atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli()
                    val start = item.getString("start")
                    val favorite = item.getString("favorite")
                    val image = requireContext().resources.getIdentifier(item.getString("image"), "drawable", requireContext().packageName)
                    FavoriteChat(name, message, time, start, favorite, image)
                }
            }


            val favoriteList_temp = loadedFavoriteList.sortedByDescending { it.favorite }.toMutableList()

            Handler(Looper.getMainLooper()).post {
                if (!isAdded || _binding == null) return@post

                favoriteList.clear()
                favoriteList.addAll(favoriteList_temp)

                if (favoriteList.isEmpty()) binding.loadingInitText.visibility = View.VISIBLE
                else binding.loadingInitText.visibility = View.INVISIBLE

                favoriteAdapter = FavoriteAdapter(
                    favoriteList,
                    onMoveClick = { favoriteChat ->
                        sharedPreferences_chat.edit().apply {
                            putString("select_user", favoriteChat.name)
                            putString("start_user", favoriteChat.start)
                            putBoolean("isFirst", false)
                            putInt("image", favoriteChat.image)
                            apply()
                        }

                        sharedPreferences_setting.edit().apply {
                            putInt("FAVORITECOUNT", favoriteList.size)
                            apply()
                        }

                        characterViewModel.greet_live.value = favoriteChat.message

                        findNavController().navigate(R.id.nav_voiceChat, null, NavAnimationUtil.getSlideFromLeftOptions())
                    },
                    onUnClick = { favoriteChat ->
                        Thread {
                            val url = characterViewModel.updateURL("/delete_favorite")

                            val connection = (url.openConnection() as HttpURLConnection).apply {
                                requestMethod = "POST"
                                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                                doOutput = true
                            }


                            val jsonInput = JSONObject().apply {
                                put("id_user", sharedPreferences_setting.getString("EMAIL", ""))
                                put("select_user", "")
                                put("input_user", "")
                                put("time_user", SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(favoriteChat.time)))
                                put("start_user", "")
                            }

                            connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                            connection.inputStream.bufferedReader().use { it.readText() }


                            Handler(Looper.getMainLooper()).post {
                                sharedPreferences_setting.edit().putInt("FAVORITECOUNT", sharedPreferences_setting.getInt("FAVORITECOUNT", 0) - 1).apply()
                                characterViewModel.favoritecount_live.value = sharedPreferences_setting.getInt("FAVORITECOUNT", 0)
                                findNavController().navigate(R.id.nav_favorite)
                            }
                        }.start()
                    }
                )

                binding.favoriteRecyclerView.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = favoriteAdapter
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
