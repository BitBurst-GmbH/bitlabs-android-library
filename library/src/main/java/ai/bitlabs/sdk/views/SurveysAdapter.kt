package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.data.model.Survey
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter

class SurveysAdapter(
    private val context: Context,
    private val surveys: List<Survey>,
    private val widgetColor: Int
) : Adapter<SurveysAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(SurveyView(context))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.surveyView) {
        surveys[position].let { survey ->
            color = widgetColor
            reward = survey.value
            rating = survey.rating
            loi = "${survey.loi.toInt()} minutes"
        }

        setOnClickListener { view ->
            view.alpha = 0.75F;
            view.animate()
                .setDuration(500)
                .alpha(1F)
                .start()
            BitLabs.launchOfferWall(context) }
    }

    override fun getItemCount(): Int = surveys.size

    class ViewHolder(val surveyView: SurveyView) : RecyclerView.ViewHolder(surveyView)
}