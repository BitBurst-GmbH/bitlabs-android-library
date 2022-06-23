package ai.bitlabs.sdk.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.*

class SurveyView : LinearLayout {

    lateinit var durationIV: ImageView
    lateinit var durationTV: TextView
    lateinit var ratingRB: RatingBar
    lateinit var ratingTV: TextView
    lateinit var startButton: ImageButton

    var rating = 1
    var duration = ""

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

}