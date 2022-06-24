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
    var reward = "0.5"
        set(value) {
            field = value
            rewardTV.text = value
        }
    var loi = "1 minute"
        set(value) {
            field = value
            loiTV.text = value
        }

    private var ratingTV: TextView
    private var rewardTV: TextView
    private var ratingBar: RatingBar
    private var loiTV: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_survey, this, true)

        ratingTV = findViewById(R.id.tv_rating)
        rewardTV = findViewById(R.id.tv_reward)
        ratingBar = findViewById(R.id.rating_bar)
        loiTV = findViewById(R.id.tv_loi)
    }

    constructor(context: Context) : super(context) {
        bindUI()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        context.withStyledAttributes(attrs, R.styleable.SurveyView) {
            rating = getInt(R.styleable.SurveyView_rating, 3)
            reward = getString(R.styleable.SurveyView_reward) ?: reward
            loi = getString(R.styleable.SurveyView_loi) ?: loi
        }

        bindUI()
    }

    private fun bindUI() {
        loiTV.text = loi
        ratingTV.text = rating.toString()
        rewardTV.text = reward.toString()

        ratingBar.rating = rating.toFloat()
    }
}