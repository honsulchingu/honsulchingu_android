package kr.ac.tukorea.honsulchingu.ui.character

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.databinding.ItemCharacterBinding
import kr.ac.tukorea.honsulchingu.model.ChatCharacter


class CharacterListAdapter(
    private val onClick: (ChatCharacter) -> Unit
) : ListAdapter<ChatCharacter, CharacterListAdapter.CharacterViewHolder>(CharacterDiffCallback()) {


    var onChatButtonClick: ((ChatCharacter) -> Unit)? = null

    inner class CharacterViewHolder(private val binding: ItemCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(character: ChatCharacter) {
            binding.characterName.text = character.name
            binding.characterMessage.text = character.message
            binding.characterDescription.text = character.description

            // 프로필 이미지 둥글게, 배경 둥근 drawable로 설정
            binding.characterImage.apply {
                setImageResource(character.profileImage)
                // 둥근 배경 drawable 적용 (이미 있던 profile_circle_bg)
                background = ContextCompat.getDrawable(context, R.drawable.profile_circle_bg)
                clipToOutline = true  // 둥근 배경 따라 이미지 자르기
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            }

            // 태그 동적 추가
            binding.tagLayout.removeAllViews()
            val inflater = LayoutInflater.from(binding.root.context)
            character.tags.forEach { tag ->
                val tagView = TextView(binding.root.context).apply {
                    text = "#$tag"
                    setTextColor(ContextCompat.getColor(context, R.color.purple))
                    setBackgroundResource(R.drawable.tag_background)

                    val paddingHorizontal = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
                    val paddingVertical = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
                    setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)

                    typeface = ResourcesCompat.getFont(context, R.font.pretendard_medium)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
                // 마진 적용
                val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                val marginEnd = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8f, binding.root.resources.displayMetrics).toInt()
                layoutParams.marginEnd = marginEnd
                tagView.layoutParams = layoutParams

                binding.tagLayout.addView(tagView)
            }

            // 클릭 이벤트
            binding.root.setOnClickListener {
                onClick(character)
            }

            // 대화 시작 버튼 클릭 토스트
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
        holder.bind(getItem(position))
    }

    class CharacterDiffCallback : DiffUtil.ItemCallback<ChatCharacter>() {
        override fun areItemsTheSame(oldItem: ChatCharacter, newItem: ChatCharacter): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatCharacter, newItem: ChatCharacter): Boolean {
            return oldItem == newItem
        }
    }
}
