package ai.bitlabs.sdk.data.repositories

import ai.bitlabs.sdk.data.api.BitLabsAPI
import ai.bitlabs.sdk.data.model.bitlabs.GetSurveysResponse
import ai.bitlabs.sdk.data.model.bitlabs.LeaveReason
import ai.bitlabs.sdk.data.model.bitlabs.UpdateClickBody
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.data.util.body
import ai.bitlabs.sdk.util.TAG
import android.util.Log

/** This class is the point of communication between the data and [BitLabs] */
internal class BitLabsRepository(private val api: BitLabsAPI) {

    suspend fun leaveSurvey(clickId: String, reason: String) = try {
        val response = api.updateClick(clickId, UpdateClickBody(LeaveReason(reason)))
        response.errorBody()?.body<Unit>()?.error?.details?.run {
            throw Exception("LeaveSurvey Error: $http - $msg")
        }

        Log.i(TAG, "LeaveSurvey - Success")
        Unit
    } catch (e: Exception) {
        SentryManager.captureException(e)
        throw e
//        Log.e(TAG, "LeaveSurvey Failure - ${e.message ?: "Unknown Error"}")
    }

    suspend fun getSurveys(sdk: String) = try {
        val response = api.getSurveys(sdk)
        val restrictionReason = response.body()?.data?.restrictionReason
        if (restrictionReason != null) {
            throw Exception("GetSurveys Error: ${restrictionReason.prettyPrint()}")
        }

        response.errorBody()?.body<GetSurveysResponse>()?.error?.details?.run {
            throw Exception("GetSurveys Error: $http - $msg")
        }

        response.body()?.data?.surveys ?: emptyList()
    } catch (e: Exception) {
        SentryManager.captureException(e)
        throw e
    }

    suspend fun getAppSettings(token: String) = try {
        api.getAppSettings(url = "https://dashboard.bitlabs.ai/api/public/v1/apps/$token")
    } catch (e: Exception) {
        SentryManager.captureException(e)
        throw e
    }
}
