package ai.bitlabs.sdk

import ai.bitlabs.sdk.data.BitLabsRepository
import ai.bitlabs.sdk.data.model.WebActivityParams
import ai.bitlabs.sdk.data.network.BitLabsAPI
import ai.bitlabs.sdk.util.BASE_URL
import ai.bitlabs.sdk.util.BUNDLE_KEY_COLOR
import ai.bitlabs.sdk.util.BUNDLE_KEY_PARAMS
import ai.bitlabs.sdk.util.OnRewardListener
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.convertKeysToCamelCase
import ai.bitlabs.sdk.util.extractColors
import ai.bitlabs.sdk.util.randomSurvey
import ai.bitlabs.sdk.views.WebActivity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.gson.GsonBuilder
import com.unity3d.player.UnityPlayer
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
    var debugMode = false

    private var uid = ""
    private var adId = ""
    private var token = ""
    private var currencyIconUrl = ""
    private var bonusPercentage = 0.0
    private var widgetColor = intArrayOf(0, 0)
    private var headerColor = intArrayOf(0, 0)

    /** These will be added as query parameters to the OfferWall Link */
    var tags = mutableMapOf<String, Any>()

    private var bitLabsRepo: BitLabsRepository? = null
    internal var onRewardListener: OnRewardListener? = null

    /**
     * Initialises the connection with BitLabs API using your app [token] and [uid]
     * and gets the user [Advertising Id][AdvertisingIdClient.Info] using the currentActivity context.
     * ######
     * **IMPORTANT:** This is the essential function. Without it, the library will not function
     * properly. So make sure you call it before using the library's functions.
     * @param[token] Found on your [BitLabs Dashboard](https://dashboard.bitlabs.ai/),
     * @param[uid] Unique for every user to initialise the connection with the BitLabs API.
     */
    fun init(token: String, uid: String) {
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

        determineAdvertisingInfo(UnityPlayer.currentActivity)

        getAppSettings()
    }

    /**
     * Gets the app settings from the BitLabs API.
     */
    private fun getAppSettings() = bitLabsRepo?.getAppSettings({
        it.visual.run {
            widgetColor = extractColors(surveyIconColor)
            headerColor = extractColors(navigationColor)
        }

        it.currency.symbol.run { currencyIconUrl = content.takeIf { isImage } ?: "" }
        val bonus = it.currency.bonusPercentage / 100.0

        it.promotion?.bonusPercentage?.run {
            bonusPercentage = bonus + this / 100.0 + this * bonus / 100.0
        } ?: run { bonusPercentage = bonus }
    }, { Log.e(TAG, "$it") })

    /** Determines whether the user can perform an action in the OfferWall
     * (either opening a survey or answering qualifications) and then executes your implementation
     * of the checkSurveysCallback().
     * ######
     * If you want to perform background checks if surveys are available, this is the best option.
     */
    fun checkSurveys(gameObject: String) = ifInitialised {
        bitLabsRepo?.getSurveys("UNITY", { surveys ->
            UnityPlayer.UnitySendMessage(
                gameObject,
                "CheckSurveysCallback",
                surveys.isNotEmpty().toString()
            )
        }, { e ->
            UnityPlayer.UnitySendMessage(gameObject, "CheckSurveysException", e.message.toString())
        })
    }

    /**
     * Fetches a list of surveys the user can open.
     * ######
     * If the user still has to answer a qualification before more surveys can be returned,
     * then this will return 3 random Surveys just for display.
     * ######
     * The getSurveysCallback() is executed when a response is received.
     * Its parameter is the String in format of JSON list of surveys in . If it's `null`,
     * then there has been an internal error which is mostly logged with 'BitLabs' as a tag.
     */
    fun getSurveys(gameObject: String) = ifInitialised {
        bitLabsRepo?.getSurveys("UNITY", { surveys ->
            UnityPlayer.UnitySendMessage(
                gameObject,
                "GetSurveysCallback",
                GsonBuilder().create()
                    .toJson(surveys.ifEmpty { { (1..3).map { randomSurvey(it) } } })
                    .convertKeysToCamelCase()
            )
        }, { exception ->
            UnityPlayer.UnitySendMessage(
                gameObject,
                "GetSurveysException",
                exception.message.toString()
            )
        })
    }

    /**
     * Fetches the leaderboard.
     * ######
     * The getLeaderBoardCallback() is executed when a response is received.
     * Its parameter is the String in format of JSON list of surveys in . If it's `null`,
     * then there has been an internal error which is mostly logged with 'BitLabs' as a tag.
     */
    fun getLeaderboard(gameObject: String) = ifInitialised {
        bitLabsRepo?.getLeaderboard({ leaderBoard ->
            UnityPlayer.UnitySendMessage(
                gameObject,
                "GetLeaderboardCallback",
                GsonBuilder().create().toJson(leaderBoard).convertKeysToCamelCase()
            )
        }, { Log.e(TAG, "$it") })
    }

    /** Registers an [OnRewardListener] callback to be invoked when the OfferWall is exited by the user. */
    fun setOnRewardListener(gameObject: String) {
        onRewardListener = OnRewardListener { payout ->
            UnityPlayer.UnitySendMessage(gameObject, "RewardCallback", payout.toString())
        }
    }

    /** Adds a new tag([key]:[value] pair) to [BitLabs.tags] */
    fun addTag(key: String, value: Any) {
        tags[key] = value
    }

    fun getColor() = widgetColor

    fun getCurrencyIconUrl() = currencyIconUrl

    fun getBonusPercentage() = bonusPercentage

    /**
     * Launches the OfferWall from the currentActivity.
     */
    fun launchOfferWall() = ifInitialised {
        val context = UnityPlayer.currentActivity

        with(Intent(context, WebActivity::class.java)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(
                BUNDLE_KEY_PARAMS,
                WebActivityParams(token, uid, "UNITY", adId, tags).asBundle()
            )
            putExtra(BUNDLE_KEY_COLOR, headerColor)
            context.startActivity(this)
        }
    }

    /** This overload is used internally to tackle the difference between the Core and Unity variants.
     * In Unity, the context is not needed as an argument, but internally it is.
     */
    internal fun launchOfferWall(context: Context) = launchOfferWall()

    internal fun leaveSurvey(clickId: String, reason: String) =
        bitLabsRepo?.leaveSurvey(clickId, reason)

    private fun determineAdvertisingInfo(context: Context) = Thread {
        try {
            adId = AdvertisingIdClient.getAdvertisingIdInfo(context).id ?: ""
            Log.d(TAG, "Advertising Id: $adId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to determine Advertising Id", e)
        }
    }.start()

    /**
     * Checks whether [token] and [uid] have been set and aren't blank/empty
     * and executes the [block] accordingly.
     */
    private inline fun ifInitialised(block: () -> Unit) {
        val isInitialised = token.isNotBlank().and(uid.isNotBlank()).and(bitLabsRepo != null)

        if (isInitialised) block()
        else Log.e(TAG, "You should initialise BitLabs first! Call BitLabs::init()")
    }
}