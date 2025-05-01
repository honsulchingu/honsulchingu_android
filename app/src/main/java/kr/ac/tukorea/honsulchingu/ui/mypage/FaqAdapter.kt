package kr.ac.tukorea.honsulchingu.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R

import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

class FaqAdapter(private val faqList: List<FaqItem>) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    inner class FaqViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionText: TextView = view.findViewById(R.id.faq_question)
        val answerText: TextView = view.findViewById(R.id.faq_answer)
        val arrowIcon: ImageView = view.findViewById(R.id.faq_arrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_help_guide, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faqItem = faqList[position]
        holder.questionText.text = faqItem.question
        holder.answerText.text = faqItem.answer

        // 초기 상태에서는 답변은 보이지 않도록 설정
        holder.answerText.visibility = View.GONE

        // 클릭 이벤트 처리
        var isExpanded = false
        holder.itemView.setOnClickListener {
            isExpanded = !isExpanded

            // 애니메이션으로 부드럽게 펼치기
            TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup, AutoTransition())
            holder.answerText.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // 화살표 회전
            holder.arrowIcon.animate().rotation(if (isExpanded) 180f else 0f).setDuration(200).start()
        }
    }

    override fun getItemCount(): Int = faqList.size
}

