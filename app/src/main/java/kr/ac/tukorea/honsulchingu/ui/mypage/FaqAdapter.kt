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
        val guideText: TextView = itemView.findViewById(R.id.guide_text)
        val guideAnswer: TextView = itemView.findViewById(R.id.guide_answer)
        val guideArrow: ImageView = itemView.findViewById(R.id.guide_arrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_help_guide, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faqItem = faqList[position]
        holder.guideText.text = faqItem.question
        holder.guideAnswer.text = faqItem.answer

        var isExpanded = false
        // 초기 상태에서는 답변을 숨기기
        holder.guideAnswer.visibility = View.GONE
        holder.guideArrow.rotation = 0f

        // 클릭 시 펼치기/접기 처리
        holder.itemView.setOnClickListener {
            isExpanded = !isExpanded

            // 펼치기/접기 처리
            if (isExpanded) {
                holder.guideAnswer.visibility = View.VISIBLE
            } else {
                holder.guideAnswer.visibility = View.GONE
            }

            // 화살표 회전 애니메이션
            holder.guideArrow.rotation = if (isExpanded) 180f else 0f
        }
    }

    override fun getItemCount(): Int = faqList.size
}
