import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.history.ChatRecord
import kr.ac.tukorea.honsulchingu.ui.history.toSmartDateString
import java.util.Date

class HistoryAdapter(
    private val chatList: List<ChatRecord>,
    private val onMoveClick: (ChatRecord) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProfile: ImageView = itemView.findViewById(R.id.imageProfile)
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        private val textMessage: TextView = itemView.findViewById(R.id.textMessage)
        private val tagContainer: LinearLayout = itemView.findViewById(R.id.tagContainer)
        private val moveButton: Button = itemView.findViewById(R.id.moveButton)

        fun bind(item: ChatRecord) {
            imageProfile.setImageResource(item.profileImageRes)
            textName.text = item.name
            textTime.text = Date(item.time).toSmartDateString()
            textMessage.text = item.last_message

            moveButton.setOnClickListener {
                onMoveClick(item)
            }

            tagContainer.removeAllViews()
            item.tags.forEach { tag ->
                val tagView = TextView(itemView.context).apply {
                    text = tag
                    setTextColor(itemView.context.getColor(R.color.purple))
                    setBackgroundResource(R.drawable.tag_background)
                    setPadding(24, 8, 24, 8)
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
