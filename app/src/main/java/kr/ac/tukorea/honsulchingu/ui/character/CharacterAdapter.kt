package kr.ac.tukorea.honsulchingu.ui.character

import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.ItemCharacterBinding
import kr.ac.tukorea.honsulchingu.model.ChatCharacter

class CharacterAdapter(
    private var characterList: List<ChatCharacter>,
    private val onClick: (ChatCharacter) -> Unit
) : RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

    inner class CharacterViewHolder(private val binding: ItemCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(character: ChatCharacter) {
            binding.characterName.text = character.name
            binding.characterDescription.text = character.description
            binding.characterMessage.text = character.message
            binding.characterImage.setImageResource(character.profileImage)

            // 태그 동적 추가
            binding.tagLayout.removeAllViews()
            val inflater = LayoutInflater.from(binding.root.context)
            character.tags.forEach { tag ->
                val tagView = TextView(binding.root.context).apply {
                    text = "#$tag"
                    setTextColor(ContextCompat.getColor(context, R.color.purple))
                    setBackgroundResource(R.drawable.tag_background)
                    setPadding(16, 8, 16, 8)
                    typeface = ResourcesCompat.getFont(context, R.font.pretendard_medium)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
                binding.tagLayout.addView(tagView)
            }

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
        val character = characterList[position]
        Log.d("CharacterAdapter", "Binding item at position: $position, name: ${character.name}")
        holder.bind(character)
    }


    override fun getItemCount(): Int {
        Log.d("CharacterAdapter", "getItemCount 호출됨: ${characterList.size}")
        return characterList.size
    }


    fun updateList(newList: List<ChatCharacter>) {
        Log.d("CharacterAdapter", "Updating list with size: ${newList.size}")
        characterList = newList
        newList.forEach {
            Log.d("CharacterAdapter", "캐릭터 이름: ${it.name}, 타입: ${it.type}")
        }
        notifyDataSetChanged()
    }



}
