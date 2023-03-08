package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.OwnUser
import ai.bitlabs.sdk.data.model.TopUser
import ai.bitlabs.sdk.util.TAG
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

class LeaderboardAdapter(
    private val context: Context,
    private val topUsers: List<TopUser>,
    private val ownUser: OwnUser?,
    private val currencyIcon: Drawable?
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            android.view.LayoutInflater.from(context)
                .inflate(R.layout.item_leaderboard, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = topUsers[position]

        holder.name?.text = user.name
        holder.rank?.text = user.rank.toString()
        holder.reward?.text = user.earningsRaw.toString()
        holder.you?.text = if (user.rank == ownUser?.rank) " (You)" else ""

        currencyIcon?.apply {
            setBounds(0, 0, intrinsicWidth * 2, intrinsicHeight * 2)
            holder.reward?.setCompoundDrawables(null, null, this, null)
        }
    }

    override fun getItemCount(): Int = topUsers.size

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val you: TextView? = view.findViewById(R.id.bl_tv_leaderboard_you)
        val rank: TextView? = view.findViewById(R.id.bl_tv_leaderboard_rank)
        val name: TextView? = view.findViewById(R.id.bl_tv_leaderboard_name)
        val reward: TextView? = view.findViewById(R.id.bl_tv_leaderboard_points)
        val avatar: ImageView? = view.findViewById(R.id.bl_iv_leaderboard_top_avatar)
    }
}