package kr.ac.tukorea.honsulchingu.ui.character

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.databinding.ItemCharacterBinding
import kr.ac.tukorea.honsulchingu.model.ChatCharacter

// ChatCharacter의 항목들을 표시하는 Adapter
class CharacterListAdapter(
    private val onClick: (ChatCharacter) -> Unit
) : ListAdapter<ChatCharacter, CharacterListAdapter.CharacterViewHolder>(CharacterDiffCallback()) {

    inner class CharacterViewHolder(private val binding: ItemCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(character: ChatCharacter) {
            binding.characterName.text = character.name
            binding.characterDescription.text = character.description
            // 필요 시 이미지 설정 등 추가 가능

            // 클릭 이벤트 처리
            binding.root.setOnClickListener {
                onClick(character)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCharacterBinding.inflate(inflater, parent, false)
        return CharacterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(getItem(position)) // getItem(position)을 사용해서 데이터 바인딩
    }

    // ListAdapter는 DiffUtil을 사용해서 효율적인 변경 처리를 합니다
    class CharacterDiffCallback : DiffUtil.ItemCallback<ChatCharacter>() {
        override fun areItemsTheSame(oldItem: ChatCharacter, newItem: ChatCharacter): Boolean {
            return oldItem.id == newItem.id // id가 같으면 동일한 항목으로 판단
        }

        override fun areContentsTheSame(oldItem: ChatCharacter, newItem: ChatCharacter): Boolean {
            return oldItem == newItem // 내용이 같으면 동일한 항목으로 판단
        }
    }
}

