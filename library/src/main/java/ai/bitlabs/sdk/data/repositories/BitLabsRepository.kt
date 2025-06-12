package ai.bitlabs.sdk.data.repositories

import ai.bitlabs.sdk.data.api.BitLabsAPI
import ai.bitlabs.sdk.data.model.bitlabs.GetSurveysResponse
import ai.bitlabs.sdk.data.model.bitlabs.LeaveReason
import ai.bitlabs.sdk.data.model.bitlabs.Survey
import ai.bitlabs.sdk.data.model.bitlabs.UpdateClickBody
import ai.bitlabs.sdk.data.util.body

internal class BitLabsRepository(private val api: BitLabsAPI) {
    suspend fun leaveSurvey(clickId: String, reason: String) {
        val response = api.updateClick(clickId, UpdateClickBody(LeaveReason(reason)))
        response.errorBody()?.body<Unit>()?.error?.details?.run {
            throw Exception("LeaveSurvey Error: $http - $msg")
        }
    }

    suspend fun getSurveys(sdk: String): List<Survey> {
        val response = api.getSurveys(sdk)
        val restrictionReason = response.body()?.data?.restrictionReason
        if (restrictionReason != null) {
            throw Exception("GetSurveys Error: ${restrictionReason.prettyPrint()}")
        }

        response.errorBody()?.body<GetSurveysResponse>()?.error?.details?.run {
            throw Exception("GetSurveys Error: $http - $msg")
        }

        return response.body()?.data?.surveys ?: emptyList()
    }

    suspend fun getAppSettings(token: String) =
        api.getAppSettings(url = "https://dashboard.bitlabs.ai/api/public/v1/apps/$token")
}
