package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.*
import androidx.core.content.res.ResourcesCompat
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
    var color = ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, null)
        set(value) {
            field = value
            (findViewById<FrameLayout>(R.id.fl_widget_container)
                .background
                .mutate() as GradientDrawable)
                .setColor(color)

            earnTV.setTextColor(color)
            rewardTV.setTextColor(color)
        }

    private var loiTV: TextView
    private var earnTV: TextView
    private var ratingTV: TextView
    private var rewardTV: TextView
    private var ratingBar: RatingBar

    init {
        LayoutInflater.from(context).inflate(R.layout.view_survey, this, true)

        loiTV = findViewById(R.id.tv_loi)
        earnTV = findViewById(R.id.tv_earn)
        ratingTV = findViewById(R.id.tv_rating)
        rewardTV = findViewById(R.id.tv_reward)
        ratingBar = findViewById(R.id.rating_bar)
    }

    constructor(context: Context) : super(context) {
        bindUI()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        context.withStyledAttributes(attrs, R.styleable.SurveyView) {
            loi = getString(R.styleable.SurveyView_loi) ?: loi
            rating = getInt(R.styleable.SurveyView_rating, rating)
            color = getColor(R.styleable.SurveyView_color, color)
            reward = getString(R.styleable.SurveyView_reward) ?: reward
        }

        bindUI()
    }

    private fun bindUI() {
        loiTV.text = loi
        rewardTV.text = reward
        ratingTV.text = rating.toString()

        ratingBar.rating = rating.toFloat()
    }
}