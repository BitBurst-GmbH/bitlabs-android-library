package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.WidgetType
import android.content.Context
import androidx.core.content.withStyledAttributes

class SurveyView(context: Context, private val type: WidgetType = WidgetType.compact) :
    android.widget.LinearLayout(context) {

    var rating = 3
        set(value) {
            field = value
            ratingTV?.text = value.toString()
            ratingBar?.rating = value.toFloat()
        }
    var reward = "0.5"
        set(value) {
            field = value
            rewardTV?.text = getEarnRewardString(value)
        }
    var loi = "1 minute"
        set(value) {
            field = value
            loiTV?.text = value
        }
    var color = androidx.core.content.res.ResourcesCompat.getColor(
        resources,
        ai.bitlabs.sdk.R.color.colorPrimaryDark,
        null
    )
        set(value) {
            field = value
            (findViewById<android.view.ViewGroup>(ai.bitlabs.sdk.R.id.bl_widget_container)
                .background
                .mutate() as android.graphics.drawable.GradientDrawable)
                .setColor(color)
        }

    private var loiTV: android.widget.TextView? = null
    private var ratingTV: android.widget.TextView? = null
    private var rewardTV: android.widget.TextView? = null
    private var ratingBar: android.widget.RatingBar? = null

    init {
        val layout = when (type) {
            WidgetType.compact -> ai.bitlabs.sdk.R.layout.view_compact_survey
            WidgetType.simple -> ai.bitlabs.sdk.R.layout.view_simple_survey
            else -> ai.bitlabs.sdk.R.layout.view_compact_survey
        }

        android.view.LayoutInflater.from(context).inflate(layout, this, true)

        loiTV = findViewById(ai.bitlabs.sdk.R.id.tv_loi)
        ratingTV = findViewById(ai.bitlabs.sdk.R.id.tv_rating)
        rewardTV = findViewById(ai.bitlabs.sdk.R.id.tv_reward)
        ratingBar = findViewById(ai.bitlabs.sdk.R.id.rating_bar)

        bindUI()
    }

    constructor(
        context: android.content.Context,
        attrs: android.util.AttributeSet
    ) : this(context) {
        context.withStyledAttributes(attrs, ai.bitlabs.sdk.R.styleable.SurveyView) {
            loi = getString(ai.bitlabs.sdk.R.styleable.SurveyView_loi) ?: loi
            rating = getInt(ai.bitlabs.sdk.R.styleable.SurveyView_rating, rating)
            color = getColor(ai.bitlabs.sdk.R.styleable.SurveyView_color, color)
            reward = getString(ai.bitlabs.sdk.R.styleable.SurveyView_reward) ?: reward
        }
    }

    private fun bindUI() {
        loiTV?.text = loi
        rewardTV?.text = getEarnRewardString(reward);
        ratingTV?.text = rating.toString()

        ratingBar?.rating = rating.toFloat()
    }

    private fun getEarnRewardString(value: String) = when (type) {
        WidgetType.compact -> resources.getString(R.string.compact_earn_reward, value)
        WidgetType.simple -> resources.getString(R.string.simple_earn_reward, value)
        else -> resources.getString(R.string.simple_earn_reward, value)
    }
}