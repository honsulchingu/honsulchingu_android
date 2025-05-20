package kr.ac.tukorea.honsulchingu.ui.favorite

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R
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
        private val unfavButton: ImageButton = itemView.findViewById(R.id.unfavButton)
        private val moveButton: Button = itemView.findViewById(R.id.moveButton)

        fun bind(item: FavoriteChat) {
            imageProfile.setImageResource(item.profileImageRes)
            nameText.text = item.name
            messageText.text = item.message
            dateText.text = Date(item.time).toSmartDateString()

            moveButton.setOnClickListener {
                onMoveClick(item)
            }

            expandTouchArea(unfavButton, 20)

            unfavButton.setOnClickListener {
                val context = itemView.context
                val dialog = AlertDialog.Builder(context, R.style.HonsulAlertDialog)
                    .setTitle("즐겨찾기 해제")
                    .setMessage("이 대화를 즐겨찾기에서 해제하시겠습니까?")
                    .setPositiveButton("확인") { _, _ ->
                        onUnfavoriteClick(item)
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
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_chat, parent, false)
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