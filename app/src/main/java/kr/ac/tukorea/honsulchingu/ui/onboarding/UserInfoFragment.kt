package kr.ac.tukorea.honsulchingu.ui.onboarding

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.FragmentUserInfoBinding
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import org.json.JSONObject
import java.net.HttpURLConnection

class UserInfoFragment : Fragment() {

    private val characterViewModel: CharacterViewModel by viewModels()

    private var _binding: FragmentUserInfoBinding? = null
    private val binding get() = _binding!!

    private var selectedGender: String? = null
    private var age: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val maleButton = binding.btnMale
        val femaleButton = binding.btnFemale

        // 성별 버튼 클릭
        val genderClickListener = View.OnClickListener { v ->
            when (v.id) {
                R.id.btn_male -> selectGender(maleButton)
                R.id.btn_female -> selectGender(femaleButton)
            }
            checkNextButtonEnable()
        }

        maleButton.setOnClickListener(genderClickListener)
        femaleButton.setOnClickListener(genderClickListener)

        // 나이 입력 체크
        binding.etAge.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                age = s.toString().toIntOrNull()
                checkNextButtonEnable()
            }
        })

        // 루트 레이아웃 터치 시 EditText 비활성화 및 키보드 숨기기
        binding.root.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (binding.etAge.isFocused) {
                    binding.etAge.clearFocus()

                    // 키보드 숨기기
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.etAge.windowToken, 0)
                }

                // 클릭 이벤트 수행 (접근성 경고 해결)
                v.performClick()
            }
            true
        }

        binding.etAge.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                // 포커스 해제
                binding.etAge.clearFocus()

                // 키보드 숨기기
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etAge.windowToken, 0)

                true // 이벤트 소비
            } else {
                false
            }
        }

        // 빈 클릭 리스너 (Lint 경고 제거용)
        binding.root.setOnClickListener { }

        // 다음 버튼 클릭
        binding.btnNext.setOnClickListener {
            // 다음 화면으로 이동 처리
            (activity as? FirstLoginActivity)?.goToTutorial()

            Thread {
                val sharedPreferences_setting = requireContext().getSharedPreferences("prefs_setting", MODE_PRIVATE)

                val url = characterViewModel.updateURL("/add_user")

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    doOutput = true
                }


                val jsonInput = JSONObject().apply {
                    put("email", sharedPreferences_setting.getString("EMAIL", "") ?: "")
                    put("nickname", sharedPreferences_setting.getString("NICKNAME", "") ?: "")
                    put("image", sharedPreferences_setting.getString("IMAGE", "") ?: "")
                    put("age", age.toString())
                    put("gender", selectedGender)
                    put("startday", sharedPreferences_setting.getString("STARTDAY", "") ?: "")
                }

                connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                connection.inputStream.bufferedReader().use { it.readText() }


                sharedPreferences_setting.edit().apply {
                    putString("AGE", age.toString())
                    putString("GENDER", selectedGender)
                    apply()
                }

                Log.d("db", "first")
                Log.d("db", sharedPreferences_setting.getString("AGE", "") ?: "")
                Log.d("db", sharedPreferences_setting.getString("GENDER", "") ?: "")
            }.start()
        }
    }

    private fun selectGender(selectedButton: MaterialButton) {
        val buttons = listOf(binding.btnMale, binding.btnFemale)
        buttons.forEach { button ->
            if (button == selectedButton) {
                button.isSelected = true
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple))
            } else {
                button.isSelected = false
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            }
        }
        selectedGender = selectedButton.text.toString()
    }

    private fun checkNextButtonEnable() {
        val enable = !selectedGender.isNullOrEmpty() && (age ?: 0) >= 18
        binding.btnNext.isEnabled = enable
        binding.btnNext.setTextColor(
            if (enable) ContextCompat.getColor(requireContext(), R.color.bg_color)
            else ContextCompat.getColor(requireContext(), R.color.gray)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
