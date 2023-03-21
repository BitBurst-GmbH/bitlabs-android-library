package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.WidgetType
import android.content.Context
import android.graphics.Color
import androidx.core.content.res.*
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.DrawableCompat

class SurveyView(context: Context, private val type: WidgetType = WidgetType.SIMPLE) :
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
    var loi = 1
        set(value) {
            field = value
            loiTV?.text = getLoiString(value)
        }
    var colors = intArrayOf(
        ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, null),
        ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, null)
    )
        set(value) {
            field = value
            (findViewById<android.view.ViewGroup>(R.id.bl_widget_container)
                .background
                .mutate() as android.graphics.drawable.GradientDrawable)
                .colors = value


            val usedColor = when (type) {
                WidgetType.COMPACT -> value.first()
                WidgetType.SIMPLE -> Color.WHITE
                WidgetType.FULLWIDTH -> Color.WHITE
            }

            (findViewById<android.widget.ImageView>(R.id.iv_play)
                ?.drawable
                ?.mutate())
                ?.apply { DrawableCompat.setTint(this, usedColor) }

            findViewById<android.widget.TextView>(R.id.tv_earn_now)?.setTextColor(value.first())

            rewardTV?.setTextColor(usedColor)
        }

    private var loiTV: android.widget.TextView? = null
    private var ratingTV: android.widget.TextView? = null
    private var rewardTV: android.widget.TextView? = null
    private var ratingBar: android.widget.RatingBar? = null

    init {
        val layout = when (type) {
            WidgetType.COMPACT -> R.layout.view_compact_survey
            WidgetType.SIMPLE -> R.layout.view_simple_survey
            WidgetType.FULLWIDTH -> R.layout.view_fullwidth_survey
        }

        android.view.LayoutInflater.from(context).inflate(layout, this, true)

        loiTV = findViewById(R.id.tv_loi)
        ratingTV = findViewById(R.id.tv_rating)
        rewardTV = findViewById(R.id.tv_reward)
        ratingBar = findViewById(R.id.rating_bar)

        bindUI()
    }

    constructor(context: Context, attrs: android.util.AttributeSet) : this(context) {
        context.withStyledAttributes(attrs, R.styleable.SurveyView) {
            loi = getInt(R.styleable.SurveyView_loi, loi)
            rating = getInt(R.styleable.SurveyView_rating, rating)
            reward = getString(R.styleable.SurveyView_reward) ?: reward
            colors = intArrayOf(
                getColor(R.styleable.SurveyView_startColor, colors.first()),
                getColor(R.styleable.SurveyView_endColor, colors.last())
            )
        }
    }

    private fun bindUI() {
        loiTV?.text = getLoiString(loi)
        rewardTV?.text = getEarnRewardString(reward)
        ratingTV?.text = rating.toString()

        ratingBar?.rating = rating.toFloat()
    }

    private fun getLoiString(value: Int) = when (type) {
        WidgetType.SIMPLE -> resources.getString(R.string.simple_loi, value)
        WidgetType.COMPACT -> resources.getString(R.string.compact_loi, value)
        WidgetType.FULLWIDTH -> resources.getString(R.string.compact_loi, value)
    }

    private fun getEarnRewardString(value: String) = when (type) {
        WidgetType.COMPACT -> resources.getString(R.string.compact_earn_reward, value)
        WidgetType.SIMPLE -> resources.getString(R.string.simple_earn_reward, value)
        WidgetType.FULLWIDTH -> value
    }
}