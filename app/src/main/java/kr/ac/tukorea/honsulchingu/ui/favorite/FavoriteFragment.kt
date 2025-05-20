package kr.ac.tukorea.honsulchingu.ui.favorite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.navigation.NavAnimationUtil


class FavoriteFragment : Fragment() {

    private lateinit var favoriteRecyclerView: RecyclerView
    private lateinit var favoriteAdapter: FavoriteAdapter
    private val favoriteList = mutableListOf<FavoriteChat>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteRecyclerView = view.findViewById(R.id.favoriteRecyclerView)

        // 테스트용 더미 데이터
        favoriteList.addAll(
            listOf(
                FavoriteChat("민혁", "무서울꺼야. 하지만 계속 시도한다면 성장할거야", 1709160000000, R.drawable.friend1),
                FavoriteChat("민혁", "넌 네가 바뀌어야 한다고 생각 안 해...", 1709160000000, R.drawable.friend1),
                FavoriteChat("민혁", "감정적으로 살다보면 다치기 쉬워.", 1709160000000, R.drawable.friend2)
            )
        )



        favoriteAdapter = FavoriteAdapter(
            items = favoriteList,
            onMoveClick = { chat ->
                findNavController().navigate(
                    R.id.nav_voiceChat,
                    null,
                    NavAnimationUtil.getSlideFromLeftOptions()
                )
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
