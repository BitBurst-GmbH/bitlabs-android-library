package ai.bitlabs.sdk

import ai.bitlabs.sdk.BitLabs.API.token
import ai.bitlabs.sdk.BitLabs.API.uid
import ai.bitlabs.sdk.BitLabs.token
import ai.bitlabs.sdk.BitLabs.uid
import ai.bitlabs.sdk.data.model.bitlabs.Survey
import ai.bitlabs.sdk.data.model.bitlabs.WidgetType
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.data.repositories.BitLabsRepository
import ai.bitlabs.sdk.offerwall.BitLabsOfferwallActivity
import ai.bitlabs.sdk.offerwall.Offerwall
import ai.bitlabs.sdk.offerwall.util.WebActivityParams
import ai.bitlabs.sdk.util.BUNDLE_KEY_TOKEN
import ai.bitlabs.sdk.util.BUNDLE_KEY_UID
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import ai.bitlabs.sdk.util.OnExceptionListener
import ai.bitlabs.sdk.util.OnResponseListener
import ai.bitlabs.sdk.util.OnSurveyRewardListener
import ai.bitlabs.sdk.util.createBitLabsRepository
import ai.bitlabs.sdk.views.BitLabsWidgetFragment
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "BitLabs"

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

    /** These will be added as query parameters to the OfferWall Link */
    @Deprecated("Use OFFERWALL object instead")
    var tags = mutableMapOf<String, Any>()

    private var repo: BitLabsRepository? = null
    internal var onRewardListener: OnSurveyRewardListener? = null

    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO) }

    /**
     * Initialises the connection with BitLabs API using your app [token] and [uid]
     * and gets the user [Advertising Id][AdvertisingIdClient.Info] using the activity [context].
     * ######
     * **IMPORTANT:** This is the essential function. Without it, the library will not function
     * properly. So make sure you call it before using the library's functions.
     * @param[token] Found on your [BitLabs Dashboard](https://dashboard.bitlabs.ai/),
     * @param[uid] Unique for every user to initialise the connection with the BitLabs API.
     */
    @Deprecated("Use API and OFFERWALL objects instead")
    fun init(context: Context, token: String, uid: String) {
        this.token = token
        this.uid = uid

        determineAdvertisingInfo(context)

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            if (throwable.stackTrace.any { it.className.startsWith("ai.bitlabs.sdk") }) {
                SentryManager.captureException(
                    token, uid,
                    throwable, defaultHandler
                )
            } else {
                defaultHandler?.uncaughtException(Thread.currentThread(), throwable)
            }
        }
    }

    @Deprecated("Use API object instead")
    fun checkSurveys(
        onResponseListener: OnResponseListener<Boolean>, onExceptionListener: OnExceptionListener,
    ) = ifInitialised {
        coroutineScope.launch {
            try {
                val surveys = repo?.getSurveys("NATIVE") ?: emptyList()
                onResponseListener.onResponse(surveys.isNotEmpty())
            } catch (e: Exception) {
                onExceptionListener.onException(e)
            }
        }
    }

    @Deprecated("Use API object instead")
    fun getSurveys(
        onResponseListener: OnResponseListener<List<Survey>>,
        onExceptionListener: OnExceptionListener,
    ) = ifInitialised {
        coroutineScope.launch {
            try {
                repo?.getSurveys("NATIVE")?.let {
                    onResponseListener.onResponse(it)
                }
            } catch (e: Exception) {
                onExceptionListener.onException(e)
            }
        }
    }

    /** Registers an [OnSurveyRewardListener] callback to be invoked when the OfferWall is exited by the user. */
    @Deprecated("Use OFFERWALL object instead")
    fun setOnRewardListener(onSurveyRewardListener: OnSurveyRewardListener) {
        this.onRewardListener = onSurveyRewardListener
    }

    /** Adds a new ([key]:[value]) pair to [BitLabs.tags] */
    @Deprecated("Use OFFERWALL object instead")
    fun addTag(key: String, value: Any) {
        tags[key] = value
    }

    /**
     * Launches the OfferWall from the [context] of the Activity you pass.
     * ######
     * It's recommended that that you use a context you know the lifecycle of
     * in order to avoid memory leaks and other issues associated with Activities.
     */
    @Deprecated("Use OFFERWALL object instead")
    fun launchOfferWall(context: Context) = ifInitialised {
        with(Intent(context, BitLabsOfferwallActivity::class.java)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(
                BUNDLE_KEY_URL,
                WebActivityParams(token, uid, "NATIVE", adId, tags).url
            )
            putExtra(BUNDLE_KEY_UID, uid)
            putExtra(BUNDLE_KEY_TOKEN, token)

            context.startActivity(this)
        }
    }

    /**
     * Shows a Survey Fragment in the [activity] with the [containerId] as its container.
     */
    @Deprecated("Will be removed in future releases.")
    fun showSurvey(
        activity: FragmentActivity, containerId: Int, type: WidgetType = WidgetType.SIMPLE,
    ) = ifInitialised {
        activity.supportFragmentManager.beginTransaction()
            .replace(containerId, BitLabsWidgetFragment(uid, token, type)).commit()
    }

    /**
     * Shows a Leaderboard Fragment in the [activity] with the [containerId] as its container.
     */
    @Deprecated("Will be removed in future releases.")
    fun showLeaderboard(activity: FragmentActivity, containerId: Int) = ifInitialised {
        activity.supportFragmentManager.beginTransaction()
            .replace(containerId, BitLabsWidgetFragment(uid, token, WidgetType.LEADERBOARD))
            .commit()
    }

    private fun determineAdvertisingInfo(context: Context) = Thread {
        try {
            adId = AdvertisingIdClient.getAdvertisingIdInfo(context).id ?: ""
            Log.d(TAG, "Advertising Id: $adId")
        } catch (e: Exception) {
            SentryManager.captureException(token, uid, e)
            Log.e(TAG, "Failed to determine Advertising Id", e)
        }
    }.start()

    /**
     * Checks whether [token] and [uid] have been set and aren't blank/empty
     * and executes the [block] accordingly.
     */
    private inline fun ifInitialised(block: () -> Unit) {
        val isInitialised = token.isNotBlank().and(uid.isNotBlank())

        if (isInitialised) block()
        else Log.e(TAG, "You should initialise BitLabs first! Call BitLabs::init()")
    }

    object API {
        private var token = ""
        private var uid = ""
        private var repo: BitLabsRepository? = null
        private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO) }

        @JvmStatic
        fun init(token: String, uid: String) {
            this.token = token
            this.uid = uid

            repo = createBitLabsRepository(token, uid)
        }

        /**
         * Checks whether [token] and [uid] have been set and aren't blank/empty
         * and executes the [block] accordingly.
         */
        private inline fun ifInitialised(block: () -> Unit) {
            val isInitialised = token.isNotBlank().and(uid.isNotBlank())

            if (isInitialised) block()
            else Log.e(TAG, "You should initialise the API first! Call BitLabs.API::init()")
        }

        @JvmStatic
        fun checkSurveys(
            onResponseListener: OnResponseListener<Boolean>,
            onExceptionListener: OnExceptionListener,
        ) = ifInitialised {
            coroutineScope.launch {
                try {
                    val surveys = repo?.getSurveys("NATIVE") ?: emptyList()
                    onResponseListener.onResponse(surveys.isNotEmpty())
                } catch (e: Exception) {
                    SentryManager.captureException(token, uid, e)
                    onExceptionListener.onException(e)
                }
            }
        }

        @JvmStatic
        fun getSurveys(
            onResponseListener: OnResponseListener<List<Survey>>,
            onExceptionListener: OnExceptionListener,
        ) = ifInitialised {
            coroutineScope.launch {
                try {
                    repo?.getSurveys("NATIVE")?.let {
                        onResponseListener.onResponse(it)
                    }
                } catch (e: Exception) {
                    SentryManager.captureException(token, uid, e)
                    onExceptionListener.onException(e)
                }
            }
        }
    }

    object OFFERWALL {
        @JvmStatic
        fun create(token: String, uid: String) = Offerwall(token, uid)
    }
}