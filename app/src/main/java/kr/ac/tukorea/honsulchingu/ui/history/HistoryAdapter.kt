package kr.ac.tukorea.honsulchingu.ui.history

import android.app.AlertDialog
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R
import java.util.Date

class HistoryAdapter(
    private val chatList: List<ChatRecord>,
    private val onMoveClick: (ChatRecord) -> Unit,
    private val onDeleteClick: (ChatRecord) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProfile: ImageView = itemView.findViewById(R.id.imageProfile)
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        private val textMessage: TextView = itemView.findViewById(R.id.textMessage)
        private val tagContainer: LinearLayout = itemView.findViewById(R.id.tagContainer)
        private val moveButton: Button = itemView.findViewById(R.id.moveButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delButton)

        fun bind(item: ChatRecord) {
            imageProfile.setImageResource(item.profileImageRes)
            textName.text = item.name
            textTime.text = Date(item.time).toSmartDateString()
            textMessage.text = item.last_message

            moveButton.setOnClickListener {
                onMoveClick(item)
            }

            deleteButton.setOnClickListener {
                val context = itemView.context
                val dialog = AlertDialog.Builder(context, R.style.HonsulAlertDialog)
                    .setTitle("기록 삭제")
                    .setMessage("이 대화 기록을 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ ->
                        onDeleteClick(item)
                    }
                    .setNegativeButton("취소", null)
                    .create()

                dialog.show()

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                    ContextCompat.getColor(context, R.color.purple)
                )
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                    ContextCompat.getColor(context, R.color.gray)
                )
            }

            tagContainer.removeAllViews()
            item.tags.forEach { tag ->
                val tagView = TextView(itemView.context).apply {
                    text = tag
                    setTextColor(itemView.context.getColor(R.color.purple))
                    setBackgroundResource(R.drawable.tag_background)
                    val paddingHorizontal = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
                    val paddingVertical = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
                    setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
                    textSize = 14f
                    typeface = resources.getFont(R.font.pretendard_medium)
                }
                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 16, 0)
                }
                tagContainer.addView(tagView, params)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_record, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position])
        val isLast = position == chatList.lastIndex
        holder.itemView.findViewById<View>(R.id.divider).visibility =
            if (isLast) View.GONE else View.VISIBLE
    }

    override fun getItemCount(): Int = chatList.size
}
