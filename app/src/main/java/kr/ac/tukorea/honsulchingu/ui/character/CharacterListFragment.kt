package kr.ac.tukorea.honsulchingu.ui.character

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
import kr.ac.tukorea.honsulchingu.model.ChatCharacter
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CharacterListFragment : Fragment() {

    private lateinit var characterListAdapter: CharacterListAdapter
    private val characterViewModel: CharacterViewModel by activityViewModels()

    private var selectedCharacter: ChatCharacter? = null

    companion object {
        fun newInstance(type: String): CharacterListFragment {
            val fragment = CharacterListFragment()
            val args = Bundle()
            args.putString("TYPE", type) // 타입 정보를 전달
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCharacterListBinding.inflate(inflater, container, false)

        // RecyclerView 설정
        characterListAdapter = CharacterListAdapter(
            onClick = { character ->
                selectedCharacter = character
                val action = CharacterListFragmentDirections.actionCharacterListToChatFragment(character.id)
                findNavController().navigate(action)
            },
            onStartChatClick = { character ->
                // bundle에 select_user, start_user 담기
                val bundle = Bundle().apply {
                    putString("select_user", character.name)
                    putString("start_user", SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(System.currentTimeMillis())))
                }

                // ChatFragment에 보내기
                findNavController().navigate(R.id.chatFragment, bundle)
            }
        )

        binding.characterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.characterRecyclerView.adapter = characterListAdapter

        // 타입에 맞는 캐릭터 목록 로드
        val type = arguments?.getString("TYPE")
        loadCharactersByType(type)

        // ViewModel 관찰
        characterViewModel.filteredCharacters.observe(viewLifecycleOwner) { characters ->
            characterListAdapter.submitList(characters)
        }

        return binding.root
    }

    private fun loadCharactersByType(type: String?) {
        if (type != null) {
            characterViewModel.updateCharacters(type)
        }
    }
}
