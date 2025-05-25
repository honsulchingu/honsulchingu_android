package kr.ac.tukorea.honsulchingu

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.NavOptions

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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

        // BottomNavigationView 아이템 선택 시 애니메이션과 함께 네비게이션 처리
        bottomNav.setOnItemSelectedListener { item ->
            val navOptions = NavOptions.Builder()
                .setEnterAnim(getEnterAnimForDestination(item.itemId))
                .setExitAnim(getExitAnimForDestination(item.itemId))
                .setPopEnterAnim(getPopEnterAnimForDestination(item.itemId))
                .setPopExitAnim(getPopExitAnimForDestination(item.itemId))
                .build()

            when (item.itemId) {
                R.id.nav_character -> {
                    navController.navigate(R.id.nav_character, null, navOptions)
                    true
                }
                R.id.nav_history -> {
                    navController.navigate(R.id.nav_history, null, navOptions)
                    true
                }
                R.id.nav_voiceChat -> {
                    navController.navigate(R.id.nav_voiceChat, null, navOptions)
                    true
                }
                R.id.nav_favorite -> {
                    navController.navigate(R.id.nav_favorite, null, navOptions)
                    true
                }
                R.id.nav_mypage -> {
                    navController.navigate(R.id.nav_mypage, null, navOptions)
                    true
                }
                else -> false
            }
        }
    }

    // 애니메이션 설정 함수
    private fun getEnterAnimForDestination(destinationId: Int): Int {
        return when (destinationId) {
            R.id.nav_character, R.id.nav_history, R.id.nav_favorite, R.id.nav_mypage -> R.anim.fade_in // 부드러운 전환 (밝기 변화)
            else -> R.anim.fade_in // 기본 부드러운 전환
        }
    }

    private fun getExitAnimForDestination(destinationId: Int): Int {
        return when (destinationId) {
            R.id.nav_character, R.id.nav_history, R.id.nav_favorite, R.id.nav_mypage -> R.anim.fade_out // 부드러운 전환 (밝기 변화)
            else -> R.anim.fade_out // 기본 부드러운 전환
        }
    }

    private fun getPopEnterAnimForDestination(destinationId: Int): Int {
        return when (destinationId) {
            R.id.nav_character, R.id.nav_history, R.id.nav_favorite, R.id.nav_mypage -> R.anim.fade_in // 부드러운 전환 (밝기 변화)
            else -> R.anim.fade_in // 기본 부드러운 전환
        }
    }

    private fun getPopExitAnimForDestination(destinationId: Int): Int {
        return when (destinationId) {
            R.id.nav_character, R.id.nav_history, R.id.nav_favorite, R.id.nav_mypage -> R.anim.fade_out // 부드러운 전환 (밝기 변화)
            else -> R.anim.fade_out // 기본 부드러운 전환
        }
    }
}
