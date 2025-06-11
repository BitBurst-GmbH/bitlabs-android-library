package ai.bitlabs.sdk.data.repositories

import ai.bitlabs.sdk.data.api.BitLabsAPI
import ai.bitlabs.sdk.data.model.bitlabs.GetSurveysResponse
import ai.bitlabs.sdk.data.model.bitlabs.LeaveReason
import ai.bitlabs.sdk.data.model.bitlabs.UpdateClickBody
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import ai.bitlabs.sdk.data.util.body

internal class BitLabsRepository(private val api: BitLabsAPI) {
    suspend fun leaveSurvey(clickId: String, reason: String) = try {
        val response = api.updateClick(clickId, UpdateClickBody(LeaveReason(reason)))
        response.errorBody()?.body<Unit>()?.error?.details?.run {
            throw Exception("LeaveSurvey Error: $http - $msg")
        }

        Unit
    } catch (e: Exception) {
        SentryManager.captureException(e)
        throw e
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
