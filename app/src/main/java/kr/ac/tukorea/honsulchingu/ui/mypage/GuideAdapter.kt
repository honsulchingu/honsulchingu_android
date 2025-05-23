package kr.ac.tukorea.honsulchingu.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.honsulchingu.R

class GuideAdapter(private val guideList: List<GuideItem>) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {

    private val expandedState = MutableList(guideList.size) { false }

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
        holder.guideText.text = guide.title
        holder.guideDescription.text = guide.description

        val isExpanded = expandedState[position]

        // 초기 상태 세팅
        holder.guideDescription.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.guideDescription.alpha = if (isExpanded) 1f else 0f
        holder.guideArrow.rotation = if (isExpanded) 180f else 0f

        holder.container.setOnClickListener {
            val expanding = !expandedState[position]
            expandedState[position] = expanding

            // 화살표 회전 애니메이션
            holder.guideArrow.animate().rotation(if (expanding) 180f else 0f).start()

            if (expanding) {
                holder.guideDescription.visibility = View.VISIBLE
                holder.guideDescription.alpha = 0f
                holder.guideDescription.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            } else {
                holder.guideDescription.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        holder.guideDescription.visibility = View.GONE
                    }
                    .start()
            }
        }
    }

    override fun getItemCount(): Int = guideList.size
}
