package kr.ac.tukorea.honsulchingu.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.ui.DialogUtil
import java.util.Date

class HistoryAdapter(
    private val chatList: List<ChatRecord>,
    private val onMoveClick: (ChatRecord) -> Unit,
    private val onDeleteClick: (ChatRecord) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.textName)
        private val last_chat: TextView = itemView.findViewById(R.id.textMessage)
        private val last_time: TextView = itemView.findViewById(R.id.textTime)
        private val tag1: TextView = itemView.findViewById(R.id.textTag1)
        private val tag2: TextView = itemView.findViewById(R.id.textTag2)
        private val tag3: TextView = itemView.findViewById(R.id.textTag3)
        private val image: ImageView = itemView.findViewById(R.id.textImage)
        private val heart: ImageView = itemView.findViewById(R.id.textHeart)
        private val moveButton: Button = itemView.findViewById(R.id.moveButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(item: ChatRecord) {
            name.text = item.name.substringAfter('_')
            last_chat.text = item.last_chat
            last_time.text = Date(item.last_time).toSmartDateString()
            tag1.text = item.tag[0]
            tag2.text = item.tag[1]
            tag3.text = item.tag[2]
            image.setImageResource(item.image)
            heart.visibility = if (item.isFavorite) View.VISIBLE else View.INVISIBLE

            moveButton.setOnClickListener {
                onMoveClick(item)
            }

            expandTouchArea(deleteButton, 20)

            deleteButton.setOnClickListener {
                DialogUtil.showHonsulDialog(
                    context = itemView.context,
                    title = "대화기록을 삭제할까요?",
                    message = "이 대화는 복구할 수 없어요.\n정말 삭제하시겠어요?",
                    iconRes = R.drawable.ic_delete,
                    positiveText = "삭제하기",
                    negativeText = "취소",
                    onPositiveClick = {
                        onDeleteClick(item)
                    }
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_record, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position])
        val isLast = position == chatList.lastIndex
        holder.itemView.findViewById<View>(R.id.divider).visibility = if (isLast) View.GONE else View.VISIBLE
    }

    override fun getItemCount(): Int = chatList.size
}

// 터치 영역 확장 함수
private fun expandTouchArea(view: View, extraPadding: Int) {
    val parent = view.parent as View
    parent.post {
        val rect = android.graphics.Rect()
        view.getHitRect(rect)
        rect.top -= extraPadding
        rect.bottom += extraPadding
        rect.left -= extraPadding
        rect.right += extraPadding
        val touchDelegate = android.view.TouchDelegate(rect, view)
        parent.touchDelegate = touchDelegate
    }
}
