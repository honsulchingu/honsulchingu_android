package kr.ac.tukorea.honsulchingu.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R

class GuideAdapter(private val guideList: List<String>) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {

    private val expandedState = mutableListOf<Boolean>().apply {
        repeat(guideList.size) { add(false) }  // 각 항목의 초기 상태는 접혀있음
    }

    inner class GuideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val guideText: TextView = view.findViewById(R.id.guide_text)
        val guideDescription: TextView = view.findViewById(R.id.guide_answer)
        val guideArrow: ImageView = view.findViewById(R.id.guide_arrow)
        val container: ConstraintLayout = view.findViewById(R.id.container)  // container (최상위 레이아웃)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_help_guide, parent, false)
        return GuideViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val guide = guideList[position]

        // 텍스트 설정
        holder.guideText.text = guide

        // 기본적으로 답변은 숨겨짐
        holder.guideDescription.visibility = if (expandedState[position]) View.VISIBLE else View.GONE
        holder.guideDescription.alpha = if (expandedState[position]) 1f else 0f
        holder.guideArrow.setImageResource(
            if (expandedState[position]) R.drawable.ic_arrow_right else R.drawable.ic_arrow_down
        )

        // 클릭 리스너: 접히거나 펼쳐짐
        holder.container.setOnClickListener {
            // 상태 토글
            expandedState[position] = !expandedState[position]

            // 애니메이션 추가: alpha 값을 변경하여 부드럽게 펼치기/접기
            val targetVisibility = if (expandedState[position]) View.VISIBLE else View.GONE
            val targetAlpha = if (expandedState[position]) 1f else 0f

            // alpha 애니메이션을 이용하여 부드럽게 펼치기/접기
            holder.guideDescription.animate()
                .alpha(targetAlpha)
                .setDuration(300) // 애니메이션 시간 설정
                .withEndAction {
                    // 애니메이션 종료 후 visibility 변경
                    holder.guideDescription.visibility = targetVisibility
                }

            // 화살표 아이콘 방향 변경
            holder.guideArrow.setImageResource(
                if (expandedState[position]) R.drawable.ic_arrow_right else R.drawable.ic_arrow_down
            )

            // RecyclerView 업데이트
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = guideList.size
}
