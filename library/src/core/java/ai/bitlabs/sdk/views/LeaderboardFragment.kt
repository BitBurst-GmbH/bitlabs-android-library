package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.OwnUser
import androidx.fragment.app.Fragment

class LeaderboardFragment(
    private val topUsers: List<ai.bitlabs.sdk.data.model.TopUser>,
    private val ownUser: OwnUser?,
    private val currencyIconUrl: String
) : Fragment(R.layout.fragment_leaderboard) {
    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val leaderboard =
            view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.bl_rv_leaderboard)
        leaderboard.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            context,
            androidx.recyclerview.widget.LinearLayoutManager.VERTICAL,
            false
        )

        BitLabs.getCurrencyIcon(currencyIconUrl, resources) { drawable ->
            leaderboard.adapter = LeaderboardAdapter(requireContext(), topUsers, ownUser, drawable)
        }

    }
}