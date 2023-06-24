package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.User
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LeaderboardFragment(
    private val topUsers: List<User>,
    private val ownUser: User?,
    private val currencyIconUrl: String,
    private val color: IntArray,
) : Fragment(R.layout.fragment_leaderboard) {
    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val leaderboard =
            view.findViewById<RecyclerView>(R.id.bl_rv_leaderboard)
        leaderboard.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        view.setOnClickListener { BitLabs.launchOfferWall(requireContext()) }

        view.findViewById<TextView>(R.id.bl_tv_own_user_rank).text =
            if (ownUser == null) "Participate in a survey to join the leaderboard."
            else getString(R.string.leaderboard_own_user_rank, ownUser.rank)

        BitLabs.getCurrencyIcon(currencyIconUrl, resources) { drawable ->
            leaderboard.adapter =
                LeaderboardAdapter(requireContext(), topUsers, ownUser, drawable, color.first())
        }
    }
}