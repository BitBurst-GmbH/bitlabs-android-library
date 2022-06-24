package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.data.model.Survey
import ai.bitlabs.sdk.util.TAG
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter

class SurveysAdapter(private val context: Context, private val surveys: List<Survey>) :
    Adapter<SurveysAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(SurveyView(context))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.surveyView) {
        surveys[position].let { survey ->
            Log.d(TAG, survey.toString())
            rating = survey.rating
            reward = survey.value
            loi = "${survey.loi} minutes"
        }
    }

    override fun getItemCount(): Int = surveys.size

    class ViewHolder(val surveyView: SurveyView) : RecyclerView.ViewHolder(surveyView) {

    }
}