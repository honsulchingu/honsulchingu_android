package kr.ac.tukorea.honsulchingu.ui.favorite

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
import kr.ac.tukorea.honsulchingu.ui.history.toSmartDateString
import java.util.Date

class FavoriteAdapter(
    private val items: List<FavoriteChat>,
    private val onMoveClick: (FavoriteChat) -> Unit,
    private val onUnfavoriteClick: (FavoriteChat) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProfile: ImageView = itemView.findViewById(R.id.imageProfile)
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val unfavButton: ImageButton = itemView.findViewById(R.id.unfavoriteButton)
        private val moveButton: Button = itemView.findViewById(R.id.moveButton)

        fun bind(item: FavoriteChat) {
            imageProfile.setImageResource(item.image)
            nameText.text = item.name.substringAfter('_')
            messageText.text = item.message
            dateText.text = Date(item.time).toSmartDateString()

            moveButton.setOnClickListener {
                onMoveClick(item)
            }

            expandTouchArea(unfavButton, 20)

            unfavButton.setOnClickListener {
                DialogUtil.showHonsulDialog(
                    context = itemView.context,
                    title = "즐겨찾기를 해제할까요?",
                    message = "이 대화를 즐겨찾기에서 삭제해도\n언제든 다시 추가할 수 있어요.",
                    iconRes = R.drawable.ic_delete,
                    positiveText = "해제하기",
                    negativeText = "취소",
                    onPositiveClick = {
                        onUnfavoriteClick(item)
                    }
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_chat, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
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
