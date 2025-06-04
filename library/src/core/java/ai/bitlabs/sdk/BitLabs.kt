package ai.bitlabs.sdk

import ai.bitlabs.sdk.BitLabs.bitLabsRepo
import ai.bitlabs.sdk.BitLabs.token
import ai.bitlabs.sdk.BitLabs.uid
import ai.bitlabs.sdk.data.api.BitLabsAPI
import ai.bitlabs.sdk.data.model.bitlabs.Survey
import ai.bitlabs.sdk.data.model.bitlabs.WidgetType
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.data.repositories.BitLabsRepository
import ai.bitlabs.sdk.offerwall.BitLabsOfferwallActivity
import ai.bitlabs.sdk.offerwall.Offerwall
import ai.bitlabs.sdk.offerwall.util.WebActivityParams
import ai.bitlabs.sdk.util.BASE_URL
import ai.bitlabs.sdk.util.BUNDLE_KEY_TOKEN
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import ai.bitlabs.sdk.util.OnExceptionListener
import ai.bitlabs.sdk.util.OnResponseListener
import ai.bitlabs.sdk.util.OnSurveyRewardListener
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.buildHttpClientWithHeaders
import ai.bitlabs.sdk.util.buildRetrofit
import ai.bitlabs.sdk.util.deviceType
import ai.bitlabs.sdk.views.BitLabsWidgetFragment
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.identifier.AdvertisingIdClient

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
    internal var fileProviderAuthority = ""

    /** These will be added as query parameters to the OfferWall Link */
    @Deprecated("Use OFFERWALL MODULE instead")
    var tags = mutableMapOf<String, Any>()

    internal var bitLabsRepo: BitLabsRepository? = null
    internal var onRewardListener: OnSurveyRewardListener? = null

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

        SentryManager.init(token, uid)

        bitlabsRepoInit()

        determineAdvertisingInfo(context)

        fileProviderAuthority = "${context.packageName}.provider.bitlabs"

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            if (throwable.stackTrace.any { it.className.startsWith("ai.bitlabs.sdk") }) {
                SentryManager.captureException(throwable, defaultHandler)
            } else {
                defaultHandler?.uncaughtException(Thread.currentThread(), throwable)
            }
        }
    }

    private fun bitlabsRepoInit() {
        val userAgent =
            "BitLabs/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.SDK_INT}; ${Build.MODEL}; ${deviceType()})"

        val okHttpClient = buildHttpClientWithHeaders(
            "User-Agent" to userAgent,
            "X-User-Id" to uid
        )

        val retrofit = buildRetrofit(BASE_URL, okHttpClient)

        bitLabsRepo = BitLabsRepository(retrofit.create(BitLabsAPI::class.java))
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
        onResponseListener: OnResponseListener<Boolean>, onExceptionListener: OnExceptionListener,
    ) = ifInitialised {
        bitLabsRepo?.getSurveys(token, "NATIVE", { surveys ->
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
        onExceptionListener: OnExceptionListener,
    ) = ifInitialised {
        bitLabsRepo?.getSurveys(token, "NATIVE", onResponseListener, onExceptionListener)
    }

    /** Registers an [OnSurveyRewardListener] callback to be invoked when the OfferWall is exited by the user. */
    @Deprecated("Use OFFERWALL MODULE instead")
    fun setOnRewardListener(onSurveyRewardListener: OnSurveyRewardListener) {
        this.onRewardListener = onSurveyRewardListener
    }

    /** Adds a new ([key]:[value]) pair to [BitLabs.tags] */
    @Deprecated("Use OFFERWALL MODULE instead")
    fun addTag(key: String, value: Any) {
        tags[key] = value
    }

    /**
     * Launches the OfferWall from the [context] of the Activity you pass.
     * ######
     * It's recommended that that you use a context you know the lifecycle of
     * in order to avoid memory leaks and other issues associated with Activities.
     */
    @Deprecated("Use OFFERWALL MODULE instead")
    fun launchOfferWall(context: Context) = ifInitialised {
        with(Intent(context, BitLabsOfferwallActivity::class.java)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(
                BUNDLE_KEY_URL,
                WebActivityParams(token, uid, "NATIVE", adId, tags).url
            )
            putExtra(BUNDLE_KEY_TOKEN, token)

            context.startActivity(this)
        }
    }

    /**
     * Shows a Survey Fragment in the [activity] with the [containerId] as its container.
     */
    fun showSurvey(
        activity: FragmentActivity, containerId: Int, type: WidgetType = WidgetType.SIMPLE,
    ) = ifInitialised {
        activity.supportFragmentManager.beginTransaction()
            .replace(containerId, BitLabsWidgetFragment(uid, token, type)).commit()
    }

    /**
     * Shows a Leaderboard Fragment in the [activity] with the [containerId] as its container.
     */
    fun showLeaderboard(activity: FragmentActivity, containerId: Int) = ifInitialised {
        activity.supportFragmentManager.beginTransaction()
            .replace(containerId, BitLabsWidgetFragment(uid, token, WidgetType.LEADERBOARD))
            .commit()
    }

    internal fun leaveSurvey(clickId: String, reason: String) =
        bitLabsRepo?.leaveSurvey(token, clickId, reason)

    private fun determineAdvertisingInfo(context: Context) = Thread {
        try {
            adId = AdvertisingIdClient.getAdvertisingIdInfo(context).id ?: ""
            Log.d(TAG, "Advertising Id: $adId")
        } catch (e: Exception) {
            SentryManager.captureException(e)
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

    object OFFERWALL {
        @JvmStatic
        fun create(token: String, uid: String) = Offerwall(token, uid)
    }
}