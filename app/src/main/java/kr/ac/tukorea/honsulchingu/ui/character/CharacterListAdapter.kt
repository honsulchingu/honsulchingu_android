package kr.ac.tukorea.honsulchingu.ui.character

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.ItemCharacterBinding

class CharacterListAdapter : ListAdapter<ChatCharacter, CharacterListAdapter.CharacterViewHolder>(CharacterDiffCallback()) {

    var onChatButtonClick: ((ChatCharacter) -> Unit)? = null

    inner class CharacterViewHolder(private val binding: ItemCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(character: ChatCharacter) {
            binding.characterName.text = character.name.substringAfter('_')
            binding.characterMessage.text = character.greet
            binding.characterTag1.text = character.tag[0]
            binding.characterTag2.text = character.tag[1]
            binding.characterTag3.text = character.tag[2]
            binding.characterDescription.text = character.description
            binding.characterImage.setImageResource(character.image)

            // 프로필 이미지 둥글게, 배경 둥근 drawable로 설정
            binding.characterImage.apply {
                setImageResource(character.image)
                // 둥근 배경 drawable 적용 (이미 있던 profile_circle_bg)
                background = ContextCompat.getDrawable(context, R.drawable.profile_circle_bg)
                clipToOutline = true  // 둥근 배경 따라 이미지 자르기
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            }

            // 대화 시작 버튼 클릭 이벤트
            binding.startChatButton.setOnClickListener {
                onChatButtonClick?.invoke(character)
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

