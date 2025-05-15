package kr.ac.tukorea.honsulchingu.ui.character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.FragmentCharacterListBinding
import kr.ac.tukorea.honsulchingu.model.ChatCharacter
import kr.ac.tukorea.honsulchingu.ui.character.CharacterListAdapter
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel

class CharacterListFragment : Fragment() {

    private lateinit var characterListAdapter: CharacterListAdapter
    private val characterViewModel: CharacterViewModel by activityViewModels()

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
        characterListAdapter = CharacterListAdapter { character ->
            // 캐릭터 클릭 시 ChatFragment로 이동
            val action = CharacterListFragmentDirections.actionCharacterListToChatFragment(character.id)
            findNavController().navigate(action)
        }

        binding.characterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.characterRecyclerView.adapter = characterListAdapter

        // 타입에 맞는 캐릭터 목록 로드
        val type = arguments?.getString("TYPE")
        loadCharactersByType(type)

        // ViewModel 관찰
        characterViewModel.filteredCharacters.observe(viewLifecycleOwner, Observer<List<ChatCharacter>> { characters ->
            characterListAdapter.submitList(characters)
        })

        // 버튼에 리스너 추가
        val startChatButton = binding.root.findViewById<AppCompatButton>(R.id.startChatButton)


        return binding.root
    }

    private fun loadCharactersByType(type: String?) {
        if (type != null) {
            characterViewModel.updateCharacters(type)
        }
    }
}
