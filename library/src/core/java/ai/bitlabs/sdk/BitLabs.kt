package ai.bitlabs.sdk

import ai.bitlabs.sdk.data.BitLabsRepository
import ai.bitlabs.sdk.data.model.Survey
import ai.bitlabs.sdk.data.model.WebActivityParams
import ai.bitlabs.sdk.data.model.WidgetType
import ai.bitlabs.sdk.data.network.BitLabsAPI
import ai.bitlabs.sdk.util.BASE_URL
import ai.bitlabs.sdk.util.BUNDLE_KEY_COLOR
import ai.bitlabs.sdk.util.BUNDLE_KEY_PARAMS
import ai.bitlabs.sdk.util.OnExceptionListener
import ai.bitlabs.sdk.util.OnResponseListener
import ai.bitlabs.sdk.util.OnRewardListener
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.extractColors
import ai.bitlabs.sdk.util.randomSurvey
import ai.bitlabs.sdk.views.LeaderboardFragment
import ai.bitlabs.sdk.views.SurveysAdapter
import ai.bitlabs.sdk.views.WebActivity
import ai.bitlabs.sdk.views.WidgetFragment
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * The main class including all the library functions to use in your code.
 * ######
 * This is a singleton object, so you'll have one instance throughout the whole
 * main process(app lifecycle).
 */
object BitLabs {
    var debugMode = false;

    private var uid = ""
    private var adId = ""
    private var token = ""
    private var currencyIconUrl = ""
    private var bonusPercentage = 0.0
    private var headerColor = intArrayOf(0, 0)
    private var widgetColors = intArrayOf(0, 0)

    /** These will be added as query parameters to the OfferWall Link */
    var tags = mutableMapOf<String, Any>()

    private var bitLabsRepo: BitLabsRepository? = null
    internal var onRewardListener: OnRewardListener? = null

    /**
     * Initialises the connection with BitLabs API using your app [token] and [uid]
     * and gets the user [Advertising Id][AdvertisingIdClient.Info] using the activity [context].
     * ######
     * **IMPORTANT:** This is the essential function. Without it, the library will not function
     * properly. So make sure you call it before using the library's functions.
     * @param[token] Found on your [BitLabs Dashboard](https://dashboard.bitlabs.ai/),
     * @param[uid] Unique for every user to initialise the connection with the BitLabs API.
     */
    fun init(context: Context, token: String, uid: String) {
        this.token = token
        this.uid = uid
        bitLabsRepo = BitLabsRepository(
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OkHttpClient.Builder().addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .addHeader("X-Api-Token", token)
                            .addHeader("X-User-Id", uid)
                            .build()
                    )
                }.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BitLabsAPI::class.java)
        )
        determineAdvertisingInfo(context)

        getAppSettings()
    }

    /**
     * Determines whether the user can perform an action in the OfferWall
     * (either opening a survey or answering qualifications) and then executes your implementation
     * of the [OnResponseListener.onResponse].
     * ######
     * If you want to perform background checks if surveys are available, this is the best option.
     *
     * When a response is received, [onResponseListener] is called. Its boolean
     * parameter is `true` if an action can be performed and `false` otherwise. If it's `null`,
     * then there has been an internal error which is most probably logged with 'BitLabs' as a tag.
     */
    fun checkSurveys(
        onResponseListener: OnResponseListener<Boolean>, onExceptionListener: OnExceptionListener
    ) = ifInitialised {
        bitLabsRepo?.getSurveys("NATIVE", { surveys ->
            onResponseListener.onResponse(surveys.isNotEmpty())
        }, onExceptionListener)
    }

    /**
     * Fetches a list of surveys the user can open.
     * ######
     * When a response is received, [onResponseListener] is called.
     * Its parameter is the list of surveys. If it's `null`, then there has been an internal error
     * which is most probably logged with 'BitLabs' as a tag.
     */
    fun getSurveys(
        onResponseListener: OnResponseListener<List<Survey>>,
        onExceptionListener: OnExceptionListener
    ) = ifInitialised {
        bitLabsRepo?.getSurveys(
            "NATIVE",
            { onResponseListener.onResponse(it.ifEmpty { (1..3).map { i -> randomSurvey(i) } }) },
            onExceptionListener
        )
    }

    /** Registers an [OnRewardListener] callback to be invoked when the OfferWall is exited by the user. */
    fun setOnRewardListener(onRewardListener: OnRewardListener) {
        this.onRewardListener = onRewardListener
    }

    /** Adds a new ([key]:[value]) pair to [BitLabs.tags] */
    fun addTag(key: String, value: Any) {
        tags[key] = value
    }

    /**
     * Launches the OfferWall from the [context] of the Activity you pass.
     * ######
     * It's recommended that that you use a context you know the lifecycle of
     * in order to avoid memory leaks and other issues associated with Activities.
     */
    fun launchOfferWall(context: Context) = ifInitialised {
        with(Intent(context, WebActivity::class.java)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(
                BUNDLE_KEY_PARAMS,
                WebActivityParams(token, uid, "NATIVE", adId, tags).asBundle()
            )
            putExtra(BUNDLE_KEY_COLOR, headerColor)
            context.startActivity(this)
        }
    }

    fun showSurvey(
        activity: FragmentActivity,
        containerId: Int,
        type: WidgetType = WidgetType.SIMPLE
    ) = activity
        .supportFragmentManager
        .beginTransaction()
        .replace(containerId, WidgetFragment(uid, token, type))
        .commit()


    /**
     * Returns a RecyclerView populated with the [surveys].
     */
    @JvmOverloads
    @Deprecated("Use showSurvey instead")
    fun getSurveyWidgets(
        context: Context, surveys: List<Survey>, type: WidgetType = WidgetType.COMPACT
    ) = RecyclerView(context).apply {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        getCurrencyIcon(currencyIconUrl, context.resources) {
            adapter = SurveysAdapter(context, surveys, type, it, widgetColors, bonusPercentage)
        }
    }

    fun showLeaderboard(activity: FragmentActivity, containerId: Int) = activity
        .supportFragmentManager
        .beginTransaction()
        .replace(containerId, WidgetFragment(uid, token, WidgetType.LEADERBOARD))
        .commit()

    @Deprecated("Use showLeaderboard instead")
    fun getLeaderboard(onResponseListener: OnResponseListener<LeaderboardFragment?>) =
        bitLabsRepo?.getLeaderboard({ leaderboard ->
            onResponseListener.onResponse(leaderboard.topUsers?.takeUnless { it.isEmpty() }?.run {
                LeaderboardFragment(this, leaderboard.ownUser, currencyIconUrl, widgetColors)
            })
        }, { Log.e(TAG, "$it") })

    internal fun leaveSurvey(clickId: String, reason: String) =
        bitLabsRepo?.leaveSurvey(clickId, reason)

    internal fun getCurrencyIcon(
        url: String, resources: Resources, onResponseListener: OnResponseListener<Drawable?>
    ) = bitLabsRepo?.getCurrencyIcon(url, resources, onResponseListener)

    /**
     * Gets the required settings from the BitLabs API.
     */
    private fun getAppSettings() = bitLabsRepo?.getAppSettings({ app ->
        app.visual.run {
            widgetColors = extractColors(surveyIconColor).takeIf { it.isNotEmpty() } ?: widgetColors
            headerColor = extractColors(navigationColor).takeIf { it.isNotEmpty() } ?: headerColor
        }

        app.currency.symbol.run { currencyIconUrl = content.takeIf { isImage } ?: "" }
        bonusPercentage = app.currency.bonusPercentage / 100.0


        app.promotion?.bonusPercentage?.run {
            bonusPercentage += this / 100.0 + this * bonusPercentage / 100.0
        }
    }, { Log.e(TAG, "$it") })

    private fun determineAdvertisingInfo(context: Context) = Thread {
        try {
            adId = AdvertisingIdClient.getAdvertisingIdInfo(context).id ?: ""
            Log.d(TAG, "Advertising Id: $adId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to determine Advertising Id", e)
        }
    }.start()

    /**
     * Checks whether [token] and [uid] have been set and aren't blank/empty and
     * [bitLabsRepo] is initialised and executes the [block] accordingly.
     */
    private inline fun ifInitialised(block: () -> Unit) {
        val isInitialised = token.isNotBlank().and(uid.isNotBlank()).and(bitLabsRepo != null)

        if (isInitialised) block()
        else Log.e(TAG, "You should initialise BitLabs first! Call BitLabs::init()")
    }
}