package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.OwnUser
import ai.bitlabs.sdk.data.model.TopUser
import ai.bitlabs.sdk.util.getLuminance
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(
    private val context: Context,
    private val topUsers: List<TopUser>,
    private val ownUser: OwnUser?,
    private val currencyIcon: Drawable?,
    private val color: Int,
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

        when (user.rank) {
            1 -> setupTrophy(holder.trophy, "1", color)
            2 -> setupTrophy(holder.trophy, "2", color)
            3 -> setupTrophy(holder.trophy, "3", color)
            else -> holder.trophy?.visibility = android.view.View.GONE
        }
    }

    override fun getItemCount(): Int = topUsers.size

    private fun setupTrophy(trophy: TextView?, text: String, color: Int) = trophy?.run {
        setText(text)
        with(background.mutate()) { DrawableCompat.setTint(this, color) }
        setTextColor(if (getLuminance(color) > 0.729 * 255) Color.BLACK else Color.WHITE)
        visibility = android.view.View.VISIBLE
    }

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val you: TextView? = view.findViewById(R.id.bl_tv_leaderboard_you)
        val rank: TextView? = view.findViewById(R.id.bl_tv_leaderboard_rank)
        val name: TextView? = view.findViewById(R.id.bl_tv_leaderboard_name)
        val reward: TextView? = view.findViewById(R.id.bl_tv_leaderboard_points)
        val trophy: TextView? = view.findViewById(R.id.bl_iv_leaderboard_top_trophy)
    }
}