package ai.bitlabs.sdk

import ai.bitlabs.sdk.BitLabs.token
import ai.bitlabs.sdk.BitLabs.uid
import ai.bitlabs.sdk.data.api.BitLabsAPI
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.data.repositories.BitLabsRepository
import ai.bitlabs.sdk.offerwall.BitLabsOfferwallActivity
import ai.bitlabs.sdk.offerwall.util.WebActivityParams
import ai.bitlabs.sdk.util.BASE_URL
import ai.bitlabs.sdk.util.BUNDLE_KEY_URL
import ai.bitlabs.sdk.util.OnSurveyRewardListener
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.convertKeysToCamelCase
import ai.bitlabs.sdk.util.deviceType
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.gson.GsonBuilder
import com.unity3d.player.UnityPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    internal var fileProviderAuthority = ""

    /** These will be added as query parameters to the OfferWall Link */
    var tags = mutableMapOf<String, Any>()

    internal var bitLabsRepo: BitLabsRepository? = null
    internal var onRewardListener: OnSurveyRewardListener? = null

    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO) }

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

        SentryManager.init(token, uid)

        val userAgent =
            "BitLabs/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.SDK_INT}; ${Build.MODEL}; ${deviceType()})"

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", userAgent)
                    .addHeader("X-User-Id", uid)
                    .build()

                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        bitLabsRepo = BitLabsRepository(retrofit.create(BitLabsAPI::class.java))

        determineAdvertisingInfo(UnityPlayer.currentActivity)

        fileProviderAuthority = "${UnityPlayer.currentActivity.packageName}.provider.bitlabs"


        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            if (throwable.stackTrace.any { it.className.startsWith("ai.bitlabs.sdk") }) {
                SentryManager.captureException(throwable, defaultHandler)
            } else {
                defaultHandler?.uncaughtException(Thread.currentThread(), throwable)
            }
        }
    }

    /** Determines whether the user can perform an action in the OfferWall
     * (either opening a survey or answering qualifications) and then executes your implementation
     * of the checkSurveysCallback().
     * ######
     * If you want to perform background checks if surveys are available, this is the best option.
     */
    fun checkSurveys(gameObject: String) = ifInitialised {
        coroutineScope.launch {
            try {
                val surveys = bitLabsRepo?.getSurveys("UNITY") ?: emptyList()
                UnityPlayer.UnitySendMessage(
                    gameObject,
                    "CheckSurveysCallback",
                    surveys.isNotEmpty().toString()
                )
            } catch (e: Exception) {
                SentryManager.captureException(e)
                UnityPlayer.UnitySendMessage(
                    gameObject,
                    "CheckSurveysException",
                    e.message.toString()
                )
            }
        }
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
        coroutineScope.launch {
            try {
                val surveys = bitLabsRepo?.getSurveys("UNITY") ?: emptyList()
                UnityPlayer.UnitySendMessage(
                    gameObject,
                    "GetSurveysCallback",
                    GsonBuilder().create().toJson(surveys).convertKeysToCamelCase()
                )
            } catch (e: Exception) {
                SentryManager.captureException(e)
                UnityPlayer.UnitySendMessage(
                    gameObject,
                    "GetSurveysException",
                    e.message.toString()
                )
            }
        }
    }

    /** Registers an [OnRewardListener] callback to be invoked when the OfferWall is exited by the user. */
    fun setOnRewardListener(gameObject: String) {
        onRewardListener = OnSurveyRewardListener { payout ->
            UnityPlayer.UnitySendMessage(gameObject, "RewardCallback", payout.toString())
        }
    }

    /** Adds a new tag([key]:[value] pair) to [BitLabs.tags] */
    fun addTag(key: String, value: Any) {
        tags[key] = value
    }

    /**
     * Launches the OfferWall from the currentActivity.
     */
    fun launchOfferWall() = ifInitialised {
        val context = UnityPlayer.currentActivity

        with(Intent(context, BitLabsOfferwallActivity::class.java)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(
                BUNDLE_KEY_URL,
                WebActivityParams(token, uid, "UNITY", adId, tags).url
            )
            context.startActivity(this)
        }
    }

    /** This overload is used internally to tackle the difference between the Core and Unity variants.
     * In Unity, the context is not needed as an argument, but internally it is.
     */
    internal fun launchOfferWall(context: Context) = launchOfferWall()

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
     * Checks whether [token] and [uid] have been set and aren't blank/empty
     * and executes the [block] accordingly.
     */
    private inline fun ifInitialised(block: () -> Unit) {
        val isInitialised = token.isNotBlank().and(uid.isNotBlank()).and(bitLabsRepo != null)

        if (isInitialised) block()
        else Log.e(TAG, "You should initialise BitLabs first! Call BitLabs::init()")
    }
}