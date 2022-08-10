package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.data.model.Survey
import androidx.recyclerview.widget.RecyclerView

class SurveysAdapter(
    private val context: android.content.Context,
    private val surveys: List<Survey>,
    private val widgetColor: Int
) : RecyclerView.Adapter<SurveysAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int) =
        ViewHolder(ai.bitlabs.sdk.views.SurveyView(context))

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
            BitLabs.launchOfferWall(context)
        }
    }

    override fun getItemCount(): Int = surveys.size

    class ViewHolder(val surveyView: ai.bitlabs.sdk.views.SurveyView) :
        RecyclerView.ViewHolder(surveyView)
}