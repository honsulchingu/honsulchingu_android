package kr.ac.tukorea.honsulchingu.ui.character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.FragmentCharacterListBinding
import kr.ac.tukorea.honsulchingu.navigation.NavAnimationUtil
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel

class CharacterListFragment : Fragment() {

    private var _binding: FragmentCharacterListBinding? = null
    private val binding get() = _binding!!

    private lateinit var characterListAdapter: CharacterListAdapter
    private val characterViewModel: CharacterViewModel by activityViewModels()

    companion object {
        fun newInstance(type: String): CharacterListFragment {
            val fragment = CharacterListFragment()
            val args = Bundle()
            args.putString("TYPE", type)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        characterListAdapter = CharacterListAdapter { character ->
        }
        characterListAdapter.onChatButtonClick = { character ->
            Toast.makeText(requireContext(), "${character.name}님과의 대화를 시작합니다.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(
                R.id.nav_voiceChat,
                null,
                NavAnimationUtil.getSlideFromRightOptions()
            )
        }


        binding.characterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.characterRecyclerView.adapter = characterListAdapter

        // ViewModel의 캐릭터 목록 관찰해서 어댑터에 전달
        characterViewModel.filteredCharacters.observe(viewLifecycleOwner) { characters ->
            characterListAdapter.submitList(characters)
        }

        // 타입 정보 받아서 캐릭터 로드
        val type = arguments?.getString("TYPE")
        if (type != null) {
            characterViewModel.updateCharacters(type)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

