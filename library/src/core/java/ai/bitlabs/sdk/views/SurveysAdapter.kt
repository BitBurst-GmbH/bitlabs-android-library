package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.data.model.Survey
import ai.bitlabs.sdk.data.model.WidgetType
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView

class SurveysAdapter(
    private val context: android.content.Context,
    private val surveys: List<Survey>,
    private val type: WidgetType,
    private val currencyIcon: Drawable?,
    private val widgetColors: IntArray
) : RecyclerView.Adapter<SurveysAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int) =
        ViewHolder(SurveyView(context, type))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.surveyView) {
        surveys[position].let { survey ->
            colors = widgetColors
            reward = survey.value
            rating = survey.rating
            loi = survey.loi.toInt()
            currency = currencyIcon
        }

        setOnClickListener { view ->
            view.alpha = 0.75F
            view.animate()
                .setDuration(500)
                .alpha(1F)
                .start()
            BitLabs.launchOfferWall(context)
        }
    }

    override fun getItemCount(): Int = surveys.size

    class ViewHolder(val surveyView: SurveyView) :
        RecyclerView.ViewHolder(surveyView)
}