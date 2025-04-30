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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FavoriteAdapter(
    private val items: List<FavoriteChat>,
    private val onMoveClick: (FavoriteChat) -> Unit,
    private val onUnfavoriteClick: (FavoriteChat) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageProfile: ImageView = itemView.findViewById(R.id.imageProfile)
        val nameText: TextView = itemView.findViewById(R.id.nameText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val unfavButton: ImageButton = itemView.findViewById(R.id.unfavButton)
        val moveButton: Button = itemView.findViewById(R.id.moveButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_chat, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val item = items[position]

        holder.imageProfile.setImageResource(item.profileImageRes)
        holder.nameText.text = item.name
        holder.messageText.text = item.message
        holder.dateText.text = Date(item.time).toSmartDateString()

        holder.unfavButton.setOnClickListener { onUnfavoriteClick(item) }
        holder.moveButton.setOnClickListener { onMoveClick(item) }

        holder.unfavButton.setOnClickListener {
            val context = holder.itemView.context
            val dialog = AlertDialog.Builder(context, R.style.HonsulAlertDialog)
                .setTitle("즐겨찾기 해제")
                .setMessage("이 대화를 즐겨찾기에서 해제하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    onUnfavoriteClick(item)
                }
                .setNegativeButton("취소", null)
                .create()

            dialog.show()

            // 버튼 색상 및 폰트 설정
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(context, R.color.purple)
            )
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(context, R.color.gray)
            )


        }
    }


    override fun getItemCount(): Int = items.size
}
