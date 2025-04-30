package kr.ac.tukorea.honsulchingu


import CharacterSelectFragment
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.history.HistoryFragment

import kr.ac.tukorea.honsulchingu.ui.voice.VoiceChatFragment


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val navController = findNavController(R.id.fragment_container)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavRoot)
        bottomNav.setupWithNavController(navController)
        // 캐릭터 선택 프래그먼트를 처음에 띄우기
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HistoryFragment())
                .commit()
        }
    }
}
