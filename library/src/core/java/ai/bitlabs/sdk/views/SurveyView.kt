package ai.bitlabs.sdk.views

import androidx.core.content.withStyledAttributes

class SurveyView : android.widget.LinearLayout {

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
    var color = androidx.core.content.res.ResourcesCompat.getColor(
        resources,
        ai.bitlabs.sdk.R.color.colorPrimaryDark,
        null
    )
        set(value) {
            field = value
            (findViewById<android.widget.FrameLayout>(ai.bitlabs.sdk.R.id.fl_widget_container)
                .background
                .mutate() as android.graphics.drawable.GradientDrawable)
                .setColor(color)

            earnTV.setTextColor(color)
            rewardTV.setTextColor(color)
        }

    private var loiTV: android.widget.TextView
    private var earnTV: android.widget.TextView
    private var ratingTV: android.widget.TextView
    private var rewardTV: android.widget.TextView
    private var ratingBar: android.widget.RatingBar

    init {
        android.view.LayoutInflater.from(context)
            .inflate(ai.bitlabs.sdk.R.layout.view_survey, this, true)

        loiTV = findViewById(ai.bitlabs.sdk.R.id.tv_loi)
        earnTV = findViewById(ai.bitlabs.sdk.R.id.tv_earn)
        ratingTV = findViewById(ai.bitlabs.sdk.R.id.tv_rating)
        rewardTV = findViewById(ai.bitlabs.sdk.R.id.tv_reward)
        ratingBar = findViewById(ai.bitlabs.sdk.R.id.rating_bar)
    }

    constructor(context: android.content.Context) : super(context) {
        bindUI()
    }

    constructor(context: android.content.Context, attrs: android.util.AttributeSet) : super(context, attrs) {
        context.withStyledAttributes(attrs, ai.bitlabs.sdk.R.styleable.SurveyView) {
            loi = getString(ai.bitlabs.sdk.R.styleable.SurveyView_loi) ?: loi
            rating = getInt(ai.bitlabs.sdk.R.styleable.SurveyView_rating, rating)
            color = getColor(ai.bitlabs.sdk.R.styleable.SurveyView_color, color)
            reward = getString(ai.bitlabs.sdk.R.styleable.SurveyView_reward) ?: reward
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