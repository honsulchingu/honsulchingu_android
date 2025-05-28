package kr.ac.tukorea.honsulchingu.ui.character

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.FragmentCharacterListBinding
import kr.ac.tukorea.honsulchingu.navigation.NavAnimationUtil
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CharacterListFragment : Fragment() {

    private var _binding: FragmentCharacterListBinding? = null
    private val binding get() = _binding!!
    private lateinit var characterListAdapter: CharacterListAdapter

    private val characterViewModel: CharacterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        characterListAdapter = CharacterListAdapter()

        characterListAdapter.onChatButtonClick = { character ->
            val sharedPreferences_chat = requireContext().getSharedPreferences("prefs_chat", MODE_PRIVATE)

            sharedPreferences_chat.edit().apply {
                putString("select_user", character.type + '_' + character.name)
                putString("start_user", SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(System.currentTimeMillis())))
                putBoolean("isFirst", true)
                putBoolean("isSelected", true)
                putInt("image", character.image)
                putString("greet", character.greet)
                apply()
            }

            findNavController().navigate(R.id.nav_voiceChat, null, NavAnimationUtil.getSlideFromRightOptions())
        }

        binding.characterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.characterRecyclerView.adapter = characterListAdapter

        // ViewModel의 캐릭터 목록 관찰해서 어댑터에 전달
        characterViewModel.filteredCharacters.observe(viewLifecycleOwner) { characters -> characterListAdapter.submitList(characters) }

        // 타입 정보 받아서 캐릭터 로드
        val type = arguments?.getString("TYPE")
        if (type != null) { characterViewModel.updateCharacters(type) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(type: String): CharacterListFragment {
            val fragment = CharacterListFragment()
            val args = Bundle()
            args.putString("TYPE", type) // 타입 정보를 전달
            fragment.arguments = args
            return fragment
        }
    }
}
