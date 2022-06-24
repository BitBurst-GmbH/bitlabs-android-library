package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.*
import androidx.core.content.withStyledAttributes

class SurveyView : LinearLayout {

    var rating = 3
        set(value) {
            field = value
            ratingTV.text = value.toString()
            ratingBar.rating = value.toFloat()
        }
    var reward = 0.5
        set(value) {
            field = value
            rewardTV.text = value.toString()
        }
    var duration = "1 minute"
        set(value) {
            field = value
            durationTV.text = value
        }

    private var ratingTV: TextView
    private var rewardTV: TextView
    private var ratingBar: RatingBar
    private var durationTV: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_survey, this, true)

        isClickable = true

        ratingTV = findViewById(R.id.tv_rating)
        rewardTV = findViewById(R.id.tv_reward)
        ratingBar = findViewById(R.id.rating_bar)
        durationTV = findViewById(R.id.tv_duration)
    }

    constructor(context: Context) : super(context) {
        bindUI()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        context.withStyledAttributes(attrs, R.styleable.SurveyView) {
            rating = getInt(R.styleable.SurveyView_rating, 3)
            reward = getFloat(R.styleable.SurveyView_reward, 0.5F).toDouble()
            duration = getString(R.styleable.SurveyView_duration) ?: "1 minute"
        }

        bindUI()
    }

    private fun bindUI() {
        durationTV.text = duration
        ratingTV.text = rating.toString()
        rewardTV.text = reward.toString()

        ratingBar.rating = rating.toFloat()
    }
}