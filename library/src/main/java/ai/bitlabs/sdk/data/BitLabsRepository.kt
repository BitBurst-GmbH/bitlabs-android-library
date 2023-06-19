package ai.bitlabs.sdk.data

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.data.model.*
import ai.bitlabs.sdk.data.network.BitLabsAPI
import ai.bitlabs.sdk.util.*
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.caverock.androidsvg.SVG
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** This class is the point of communication between the data and [BitLabs] */
internal class BitLabsRepository(token: String, uid: String) {
    private val bitLabsAPI = Retrofit.Builder()
        .baseUrl("https://api.bitlabs.ai/")
        .client(OkHttpClient.Builder().addInterceptor { chain ->
            chain.run {
                proceed(
                    request().newBuilder()
                        .addHeader("X-Api-Token", token)
                        .addHeader("X-User-Id", uid)
                        .build()
                )
            }
        }.build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BitLabsAPI::class.java)

    internal fun leaveSurvey(networkId: String, surveyId: String, reason: String) =
        bitLabsAPI.leaveSurvey(networkId, surveyId, LeaveReason(reason))
            .enqueue(object : Callback<BitLabsResponse<Unit>> {
                override fun onResponse(
                    call: Call<BitLabsResponse<Unit>>,
                    response: Response<BitLabsResponse<Unit>>
                ) {
                    if (response.isSuccessful)
                        Log.i(TAG, "LeaveSurvey - Success")
                    else response.errorBody()?.body<Unit>()?.error?.details?.run {
                        Log.e(TAG, "LeaveSurvey $http - $msg")
                    }
                }

                override fun onFailure(call: Call<BitLabsResponse<Unit>>, t: Throwable) {
                    Log.e(TAG, "LeaveSurvey Failure - ${t.message ?: "Unknown Error"}")
                }
            })

    internal fun getSurveys(
        sdk: String,
        onResponseListener: OnResponseListener<List<Survey>>,
        onExceptionListener: OnExceptionListener
    ) = bitLabsAPI.getSurveys(sdk).enqueue(object : Callback<BitLabsResponse<GetSurveysResponse>> {
        override fun onResponse(
            call: Call<BitLabsResponse<GetSurveysResponse>>,
            response: Response<BitLabsResponse<GetSurveysResponse>>
        ) {
            if (response.isSuccessful) {
                response.body()?.data?.surveys?.run {
                    val surveys = this.ifEmpty { (1..3).map { randomSurvey(it) } }
                    onResponseListener.onResponse(surveys)
                }
                return
            }

            response.errorBody()?.body<GetSurveysResponse>()?.error?.details?.run {
                onExceptionListener.onException(Exception("$http - $msg"))
            }
        }

        override fun onFailure(call: Call<BitLabsResponse<GetSurveysResponse>>, t: Throwable) {
            onExceptionListener.onException(Exception(t))
        }
    })

    internal fun getAppSettings(
        onResponseListener: OnResponseListener<GetAppSettingsResponse>,
        onExceptionListener: OnExceptionListener
    ) = bitLabsAPI.getAppSettings()
        .enqueue(object : Callback<BitLabsResponse<GetAppSettingsResponse>> {
            override fun onResponse(
                call: Call<BitLabsResponse<GetAppSettingsResponse>>,
                response: Response<BitLabsResponse<GetAppSettingsResponse>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { onResponseListener.onResponse(it) }
                    return
                }

                response.errorBody()?.body<GetSurveysResponse>()?.error?.details?.run {
                    onExceptionListener.onException(Exception("$http - $msg"))
                }
            }

            override fun onFailure(
                call: Call<BitLabsResponse<GetAppSettingsResponse>>,
                t: Throwable
            ) {
                onExceptionListener.onException(Exception(t))
            }
        })

    internal fun getLeaderboard(
        onResponseListener: OnResponseListener<GetLeaderboardResponse>,
        onExceptionListener: OnExceptionListener
    ) = bitLabsAPI.getLeaderboard()
        .enqueue(object : Callback<BitLabsResponse<GetLeaderboardResponse>> {
            override fun onResponse(
                call: Call<BitLabsResponse<GetLeaderboardResponse>>,
                response: Response<BitLabsResponse<GetLeaderboardResponse>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { onResponseListener.onResponse(it) }
                    return
                }

                response.errorBody()?.body<GetLeaderboardResponse>()?.error?.details?.run {
                    onExceptionListener.onException(Exception("$http - $msg"))
                }
            }

            override fun onFailure(
                call: Call<BitLabsResponse<GetLeaderboardResponse>>,
                t: Throwable
            ) {
                onExceptionListener.onException(Exception(t))
            }
        })

    internal fun getCurrencyIcon(
        url: String,
        resources: Resources,
        onResponseListener: OnResponseListener<Drawable?>
    ) = bitLabsAPI.getCurrencyIcon(url)
        .enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val drawable = if (it.contentType()?.subtype() == "svg+xml")
                            with(SVG.getFromString(it.string())) {
                                val bitmap = Bitmap.createBitmap(
                                    documentWidth.toInt(),
                                    documentHeight.toInt(),
                                    Bitmap.Config.ARGB_8888
                                )

                                val canvas = Canvas(bitmap)
                                canvas.drawRGB(255, 255, 255)

                                renderToCanvas(canvas)

                                BitmapDrawable(resources, bitmap)
                            }
                        else
                            BitmapDrawable(resources, it.byteStream())

                        onResponseListener.onResponse(drawable)
                    }
                    return
                }

                Log.e(
                    TAG, "getCurrencyIcon Failure - " +
                            (response.errorBody()?.string() ?: "Unknown Error")
                )
                onResponseListener.onResponse(null)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "getCurrencyIcon Failure - ${t.message ?: "Unknown Error"}")
            }
        })
}