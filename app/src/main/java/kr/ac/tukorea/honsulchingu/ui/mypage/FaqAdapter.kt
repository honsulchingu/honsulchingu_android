package kr.ac.tukorea.honsulchingu.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R

class FaqAdapter(private val faqList: List<FaqItem>) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    private val expandedState = MutableList(faqList.size) { false }

    inner class FaqViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionText: TextView = view.findViewById(R.id.guide_text)
        val answerText: TextView = view.findViewById(R.id.guide_answer)
        val arrowIcon: ImageView = view.findViewById(R.id.guide_arrow)
        val container: ConstraintLayout = view.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_help_guide, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faq = faqList[position]
        val isExpanded = expandedState[position]

        holder.questionText.text = faq.question
        holder.answerText.text = faq.answer

        // 초기 상태 반영
        holder.answerText.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.answerText.alpha = if (isExpanded) 1f else 0f
        holder.arrowIcon.rotation = if (isExpanded) 180f else 0f

        // 클릭 시 확장/축소 처리
        holder.container.setOnClickListener {
            val newState = !expandedState[position]
            expandedState[position] = newState

            // 화살표 회전
            holder.arrowIcon.animate().rotation(if (newState) 180f else 0f).start()

            if (newState) {
                holder.answerText.visibility = View.VISIBLE
                holder.answerText.alpha = 0f
                holder.answerText.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            } else {
                holder.answerText.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        holder.answerText.visibility = View.GONE
                    }
                    .start()
            }
        }
    }

    override fun getItemCount(): Int = faqList.size
}
