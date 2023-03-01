package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.Reward
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(
    private val context: Context,
    private val rewards: List<Reward>,
    private val currencyIcon: String
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            android.view.LayoutInflater.from(context)
                .inflate(R.layout.item_leaderboard, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reward = rewards[position]
        holder.rank.text = reward.rank.toString()
        holder.name.text = "anonymous"
        holder.reward.text = reward.rewardRaw.toString()
    }

    override fun getItemCount(): Int = rewards.size

    class ViewHolder(val view: android.view.View) : RecyclerView.ViewHolder(view) {
        val rank = view.findViewById<android.widget.TextView>(R.id.bl_tv_leaderboard_rank)
        val name = view.findViewById<android.widget.TextView>(R.id.bl_tv_leaderboard_name)
        val reward = view.findViewById<android.widget.TextView>(R.id.bl_tv_leaderboard_points)
    }
}