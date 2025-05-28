package kr.ac.tukorea.honsulchingu.ui.chat

import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import kr.ac.tukorea.honsulchingu.R
import kr.ac.tukorea.honsulchingu.viewmodel.CharacterViewModel
import org.json.JSONObject
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private val context: Context,
    private val characterViewModel: CharacterViewModel
) : ListAdapter<ChatItem, RecyclerView.ViewHolder>(DiffCallback) {

    // DiffCallback의 areItemsTheSame와 areContentsTheSame에서 실제 데이터 비교
    object DiffCallback : DiffUtil.ItemCallback<ChatItem>() {
        override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is ChatItem.MessageItem -> if (item.chatMessage.isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
            is ChatItem.DateDividerItem -> VIEW_TYPE_DATE_DIVIDER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_user, parent, false)
                UserViewHolder(view)
            }
            VIEW_TYPE_AI -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_ai, parent, false)
                AIViewHolder(view)
            }
            VIEW_TYPE_DATE_DIVIDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_date_divider, parent, false)
                DateDividerViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ChatItem.MessageItem -> {
                val message = item.chatMessage

                if (holder is UserViewHolder) {
                    holder.bind(message)
                }
                else if (holder is AIViewHolder) {
                    holder.bind(message)
                }
            }
            is ChatItem.DateDividerItem -> {
                (holder as DateDividerViewHolder).bind(item.dateText)
            }
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(message: ChatMessage) {
            messageText.text = message.message
            timeText.text = formatTime(message.timestamp)

            // 페이드 인 애니메이션
            ObjectAnimator.ofFloat(messageText, "alpha", 0f, 1f).apply {
                duration = 300
                start()
            }
        }
    }

    inner class AIViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val imageText: ShapeableImageView = itemView.findViewById(R.id.imageText)
        private val heartText: ImageView = itemView.findViewById(R.id.heartText)
        private val gestureDetector = GestureDetector(itemView.context, GestureListener())

        private val sharedPreferences_chat = context.getSharedPreferences("prefs_chat", MODE_PRIVATE)

        fun bind(message: ChatMessage) {
            messageText.text = message.message
            timeText.text = formatTime(message.timestamp)
            imageText.setImageResource(sharedPreferences_chat.getInt("image", R.drawable.ic_profile_placeholder))
            heartText.visibility = if (message.isFavorite) View.VISIBLE else View.INVISIBLE

            ObjectAnimator.ofFloat(messageText, "alpha", 0f, 1f).apply {
                duration = 300
                start()
            }

            messageText.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }
        }

        inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val heartText: ImageView = itemView.findViewById(R.id.heartText)

                // 하트 버튼이 보이면 즐겨찾기에서 제거하고 버튼 숨기기
                if (heartText.visibility == View.VISIBLE) {
                    // 즐겨찾기에서 삭제하는 로직 (예시)
                    removeFromFavorites(adapterPosition)

                    // 하트 버튼 숨기기
                    heartText.visibility = View.INVISIBLE
                }
                else {
                    // 하트 버튼을 보이게 하고 즐겨찾기에 추가하는 로직
                    addToFavorites(adapterPosition)

                    // 하트 버튼 보이게 하기
                    heartText.visibility = View.VISIBLE

                    // 진동 추가
                    vibratePhone()
                }

                return true
            }

            override fun onLongPress(e: MotionEvent) {
                copyTextToClipboard(messageText.text.toString())
            }

            private fun vibratePhone() {
                val vibrator = itemView.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                else {
                    vibrator.vibrate(100)
                }
            }
        }

        private fun copyTextToClipboard(text: String) {
            val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Message", text)

            clipboard.setPrimaryClip(clip)
        }
    }

    inner class DateDividerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewDateDivider)

        fun bind(dateText: String) {
            dateTextView.text = dateText
        }
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("a h:mm", Locale.getDefault())

        return sdf.format(Date(timestamp))
    }

    private fun addToFavorites(position: Int) {
        val item = getItem(position)

        if (item is ChatItem.MessageItem) {
            Thread {
                val sharedPreferences_setting = context.getSharedPreferences("prefs_setting", MODE_PRIVATE)

                val url = characterViewModel.updateURL("/add_favorite")

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    doOutput = true
                }


                val jsonInput = JSONObject().apply {
                    put("id_user", sharedPreferences_setting.getString("EMAIL", ""))
                    put("select_user", "")
                    put("input_user", "")
                    put("time_user", SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(item.chatMessage.timestamp)))
                    put("start_user", "")
                    put("shown_user", "")
                }

                connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                connection.inputStream.bufferedReader().use { it.readText() }


                Handler(Looper.getMainLooper()).post {
                    sharedPreferences_setting.edit().putInt("FAVORITECOUNT", sharedPreferences_setting.getInt("FAVORITECOUNT", 0) + 1).apply()
                    characterViewModel.favoritecount_live.value = sharedPreferences_setting.getInt("FAVORITECOUNT", 0)
                }
            }.start()
        }
    }

    private fun removeFromFavorites(position: Int) {
        val item = getItem(position)

        if (item is ChatItem.MessageItem) {
            Thread {
                val sharedPreferences_setting = context.getSharedPreferences("prefs_setting", MODE_PRIVATE)

                val url = characterViewModel.updateURL("/delete_favorite")

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    doOutput = true
                }


                val jsonInput = JSONObject().apply {
                    put("id_user", sharedPreferences_setting.getString("EMAIL", ""))
                    put("select_user", "")
                    put("input_user", "")
                    put("time_user", SimpleDateFormat("yyyy. MM. dd. HH-mm-ss", Locale.KOREA).format(Date(item.chatMessage.timestamp)))
                    put("start_user", "")
                    put("shown_user", "")
                }

                connection.outputStream.use { it.write(jsonInput.toString().toByteArray(Charsets.UTF_8)) }


                connection.inputStream.bufferedReader().use { it.readText() }


                Handler(Looper.getMainLooper()).post {
                    sharedPreferences_setting.edit().putInt("FAVORITECOUNT", sharedPreferences_setting.getInt("FAVORITECOUNT", 0) - 1).apply()
                    characterViewModel.favoritecount_live.value = sharedPreferences_setting.getInt("FAVORITECOUNT", 0)
                }
            }.start()
        }
    }

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_AI = 1
        private const val VIEW_TYPE_DATE_DIVIDER = 2
    }
}
