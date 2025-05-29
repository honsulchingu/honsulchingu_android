package kr.ac.tukorea.honsulchingu

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kr.ac.tukorea.honsulchingu.navigation.NavAnimationUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // 네비게이션 컨트롤러 얻기
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        // BottomNavigationView 설정
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavRoot)
        bottomNav.setupWithNavController(navController)

        // 탭 상태 반영 - ChatFragment도 VoiceChat으로 포함되도록 설정
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_character -> bottomNav.menu.findItem(R.id.nav_character).isChecked = true
                R.id.nav_history -> bottomNav.menu.findItem(R.id.nav_history).isChecked = true
                R.id.nav_voiceChat, R.id.chatFragment -> bottomNav.menu.findItem(R.id.nav_voiceChat).isChecked = true
                R.id.nav_favorite -> bottomNav.menu.findItem(R.id.nav_favorite).isChecked = true
                R.id.nav_mypage -> bottomNav.menu.findItem(R.id.nav_mypage).isChecked = true
            }
        }

        // 구분선 View 생성
        val divider = View(this).apply {
            id = View.generateViewId()
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 1).apply {
                bottomToTop = bottomNav.id
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
            setBackgroundColor(ContextCompat.getColor(context, R.color.divider_color))
        }

        // 구분선 추가
        val rootLayout = findViewById<ConstraintLayout>(R.id.main)
        rootLayout.addView(divider)

        // BottomNavigationView 아이템 선택 시 애니메이션과 함께 네비게이션 처리
        bottomNav.setOnItemSelectedListener { item ->
            val destinationId = when (item.itemId) {
                R.id.nav_character -> R.id.nav_character
                R.id.nav_history -> R.id.nav_history
                R.id.nav_voiceChat -> R.id.nav_voiceChat
                R.id.nav_favorite -> R.id.nav_favorite
                R.id.nav_mypage -> R.id.nav_mypage
                else -> null
            }
            if (destinationId != null) {
                // 현재 네비게이션 그래프의 루트 (예: R.id.nav_graph_root) 혹은 전체 스택을 비우고 이동할 화면 ID를 popUpTo로 지정
                val navOptions = NavAnimationUtil.getFadeOptions(popUpToId = navController.graph.startDestinationId, inclusive = false)
                navController.navigate(destinationId, null, navOptions)
                true
            }
            else {
                false
            }
        }
        bottomNav.itemBackground = ContextCompat.getDrawable(this, R.drawable.transparent_ripple)
    }
}
