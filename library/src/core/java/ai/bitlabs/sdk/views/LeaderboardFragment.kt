package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.OwnUser
import ai.bitlabs.sdk.data.model.Reward
import ai.bitlabs.sdk.data.model.TopUser
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class LeaderboardFragment(
    private val topUsers: List<TopUser>,
    private val rewards: List<Reward>,
    private val ownUser: OwnUser?,
    private val currencyIconUrl: String,
    private val color: IntArray,
) : Fragment(R.layout.fragment_leaderboard) {
    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val leaderboard =
            view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.bl_rv_leaderboard)
        leaderboard.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            context,
            androidx.recyclerview.widget.LinearLayoutManager.VERTICAL,
            false
        )

        view.findViewById<View>(R.id.bl_ll_top_reward_1)
            ?.apply { bind(this, color, "1st Place", rewards[0].rewardRaw) }

        view.findViewById<View>(R.id.bl_ll_top_reward_2)
            ?.apply { bind(this, color, "2nd Place", rewards[1].rewardRaw) }

        view.findViewById<View>(R.id.bl_ll_top_reward_3)
            ?.apply { bind(this, color, "3rd Place", rewards[2].rewardRaw) }

        BitLabs.getCurrencyIcon(currencyIconUrl, resources) { drawable ->
            leaderboard.adapter =
                LeaderboardAdapter(requireContext(), topUsers, ownUser, drawable, color.first())
        }
    }

    private fun bind(view: View, color: IntArray, place: String, reward: Double) {
        view.findViewById<TextView>(R.id.bl_tv_top_rank)?.text = place
        view.findViewById<TextView>(R.id.bl_tv_top_reward)?.apply {
            text = reward.toString()
            (background.mutate() as GradientDrawable).colors = color
        }
    }

}