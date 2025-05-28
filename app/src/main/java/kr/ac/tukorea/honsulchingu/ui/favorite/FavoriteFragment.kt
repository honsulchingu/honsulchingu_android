package kr.ac.tukorea.honsulchingu.ui.favorite

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.navigation.NavAnimationUtil
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel

class FavoriteFragment : Fragment() {

    private lateinit var favoriteRecyclerView: RecyclerView
    private lateinit var favoriteAdapter: FavoriteAdapter

    private val favoriteList = mutableListOf<FavoriteChat>()
    private val characterViewModel: CharacterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteRecyclerView = view.findViewById(R.id.favoriteRecyclerView)

        favoriteList.addAll(
            listOf(
                FavoriteChat("민혁", "무서울꺼야. 하지만 계속 시도한다면 성장할거야", 1709160000000, R.drawable.friend_choiminhyeok),
                FavoriteChat("민혁", "넌 네가 바뀌어야 한다고 생각 안 해...", 1709160000000, R.drawable.friend_choiminhyeok),
                FavoriteChat("민혁", "감정적으로 살다보면 다치기 쉬워.", 1709160000000, R.drawable.friend_choiminhyeok)
            )
        )

        favoriteAdapter = FavoriteAdapter(
            items = favoriteList,
            onMoveClick = { chat ->
                val sharedPreferences_chat = requireContext().getSharedPreferences("prefs_chat", MODE_PRIVATE)
                val sharedPreferences_setting = requireContext().getSharedPreferences("prefs_setting", MODE_PRIVATE)

                sharedPreferences_chat.edit().apply {
                    putString("select_user", chat.name)
                    // putString("start_user", chat.start_time)
                    putBoolean("isFirst", false)
                    putInt("image", chat.image)
                    putString("greet", chat.message)
                    characterViewModel.greet_live.value = chat.message
                    apply()
                }

                sharedPreferences_setting.edit().apply {
                    putInt("FAVORITECOUNT", favoriteList.size)
                    apply()
                }

                findNavController().navigate(R.id.chatFragment, null, NavAnimationUtil.getSlideFromLeftOptions())
            },
            onUnfavoriteClick = { chat ->
                // 즐겨찾기 해제 처리
            }
        )

        favoriteRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoriteAdapter
        }
    }
}
