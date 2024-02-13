package ai.bitlabs.sdk.views

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.data.model.WidgetType
import ai.bitlabs.sdk.util.toPx
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
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
    var oldReward = "0.5"
        set(value) {
            field = value
            oldRewardTV?.text = value
        }
    var bonus = 0
        set(value) {
            field = value
            bonusPercentageTV?.text = "+$value%"
            if (value == 0) {
                bonusPercentageTV?.visibility = View.GONE
                oldRewardTV?.visibility = View.GONE
            } else {
                bonusPercentageTV?.visibility = View.VISIBLE
                oldRewardTV?.visibility = View.VISIBLE
            }
        }
    var currency: Drawable? = null
        set(value) {
            field = value
            value?.constantState?.newDrawable()?.mutate()?.apply {
                val size = (if (type == WidgetType.SIMPLE) 16 else 11).toPx().toInt()
                setBounds(0, 0, size, size)
                rewardTV?.setCompoundDrawables(null, null, this, null)
            }

            value?.constantState?.newDrawable()?.mutate()?.apply {
                val size = (if (type == WidgetType.SIMPLE) 12 else 9).toPx().toInt()
                setBounds(0, 0, size, size)
                oldRewardTV?.setCompoundDrawables(null, null, this, null)
            }
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

            (bonusPercentageTV?.background?.mutate() as android.graphics.drawable.GradientDrawable)
                .colors = when (type) {
                WidgetType.COMPACT -> value
                WidgetType.SIMPLE -> intArrayOf(Color.WHITE, Color.WHITE)
                WidgetType.FULL_WIDTH -> intArrayOf(Color.WHITE, Color.WHITE)

                else -> intArrayOf(Color.WHITE, Color.WHITE)
            }

            bonusPercentageTV?.setTextColor(
                when (type) {
                    WidgetType.COMPACT -> Color.WHITE
                    WidgetType.SIMPLE -> value.first()
                    WidgetType.FULL_WIDTH -> value.first()

                    else -> value.first()
                }
            )

            val usedColor = when (type) {
                WidgetType.COMPACT -> value.first()
                WidgetType.SIMPLE -> Color.WHITE
                WidgetType.FULL_WIDTH -> Color.WHITE

                else -> Color.WHITE
            }

            (findViewById<android.widget.ImageView>(R.id.iv_play)
                ?.drawable
                ?.mutate())
                ?.apply { DrawableCompat.setTint(this, usedColor) }

            findViewById<android.widget.TextView>(R.id.tv_earn_now)?.setTextColor(value.first())

            oldRewardTV?.setTextColor(usedColor)

            rewardTV?.setTextColor(usedColor)
        }

    private var loiTV: android.widget.TextView? = null
    private var ratingTV: android.widget.TextView? = null
    private var rewardTV: android.widget.TextView? = null
    private var ratingBar: android.widget.RatingBar? = null
    private var oldRewardTV: android.widget.TextView? = null
    private var bonusPercentageTV: android.widget.TextView? = null

    init {
        val layout = when (type) {
            WidgetType.COMPACT -> R.layout.view_compact_survey
            WidgetType.SIMPLE -> R.layout.view_simple_survey
            WidgetType.FULL_WIDTH -> R.layout.view_fullwidth_survey

            else -> R.layout.view_simple_survey
        }

        android.view.LayoutInflater.from(context).inflate(layout, this, true)

        loiTV = findViewById(R.id.tv_loi)
        ratingTV = findViewById(R.id.tv_rating)
        rewardTV = findViewById(R.id.tv_reward)
        ratingBar = findViewById(R.id.rating_bar)
        oldRewardTV = findViewById(R.id.tv_old_reward)
        bonusPercentageTV = findViewById(R.id.tv_bonus_percentage)

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
        oldRewardTV?.text = reward
        oldRewardTV?.paintFlags = (oldRewardTV?.paintFlags
            ?: android.graphics.Paint.STRIKE_THRU_TEXT_FLAG) or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG

        loiTV?.text = getLoiString(loi)
        ratingTV?.text = rating.toString()
        rewardTV?.text = getEarnRewardString(reward)

        ratingBar?.rating = rating.toFloat()
    }

    private fun getLoiString(value: Int) = when (type) {
        WidgetType.SIMPLE -> resources.getString(R.string.simple_loi, value)
        WidgetType.COMPACT -> resources.getString(R.string.compact_loi, value)
        WidgetType.FULL_WIDTH -> resources.getString(R.string.compact_loi, value)

        else -> resources.getString(R.string.simple_loi, value)
    }

    private fun getEarnRewardString(value: String) = when (type) {
        WidgetType.COMPACT -> value
        WidgetType.SIMPLE -> resources.getString(R.string.simple_earn_reward, value)
        WidgetType.FULL_WIDTH -> value

        else -> value
    }
}