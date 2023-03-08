package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.Reward
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(
    private val context: Context,
    private val rewards: List<Reward>,
    private val currencyIcon: Drawable?
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            android.view.LayoutInflater.from(context)
                .inflate(R.layout.item_leaderboard, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reward = rewards[position]
        holder.rank?.text = reward.rank.toString()
        holder.name?.text = "anonymous"
        holder.reward?.text = reward.rewardRaw.toString()
        currencyIcon?.apply {
            setBounds(0, 0, intrinsicWidth * 2, intrinsicHeight * 2)
            holder.reward?.setCompoundDrawables(null, null, this, null)
        }
    }

    override fun getItemCount(): Int = rewards.size

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val rank: TextView? = view.findViewById(R.id.bl_tv_leaderboard_rank)
        val name: TextView? = view.findViewById(R.id.bl_tv_leaderboard_name)
        val reward: TextView? = view.findViewById(R.id.bl_tv_leaderboard_points)
        val avatar: ImageView? = view.findViewById(R.id.bl_iv_leaderboard_top_avatar)
    }
}