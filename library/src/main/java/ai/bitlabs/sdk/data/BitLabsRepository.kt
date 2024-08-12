package ai.bitlabs.sdk.data

import ai.bitlabs.sdk.BitLabs
import ai.bitlabs.sdk.data.model.BitLabsResponse
import ai.bitlabs.sdk.data.model.GetAppSettingsResponse
import ai.bitlabs.sdk.data.model.GetLeaderboardResponse
import ai.bitlabs.sdk.data.model.GetSurveysResponse
import ai.bitlabs.sdk.data.model.LeaveReason
import ai.bitlabs.sdk.data.model.RestrictionReason
import ai.bitlabs.sdk.data.model.Survey
import ai.bitlabs.sdk.data.model.UpdateClickBody
import ai.bitlabs.sdk.data.network.BitLabsAPI
import ai.bitlabs.sdk.util.OnExceptionListener
import ai.bitlabs.sdk.util.OnResponseListener
import ai.bitlabs.sdk.util.TAG
import ai.bitlabs.sdk.util.body
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.caverock.androidsvg.SVG
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** This class is the point of communication between the data and [BitLabs] */
internal class BitLabsRepository(private val bitLabsAPI: BitLabsAPI) {
    internal fun leaveSurvey(clickId: String, reason: String) =
        bitLabsAPI.updateClick(clickId, UpdateClickBody(LeaveReason(reason)))
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
            val surveys = response.body()?.data?.surveys ?: emptyList()

            if (surveys.isNotEmpty()) {
                onResponseListener.onResponse(surveys)
                return
            }

            val restrictionReason = response.body()?.data?.restrictionReason
            if (restrictionReason != null) {
                onExceptionListener.onException(Exception("Restriction Reason: $restrictionReason"))
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