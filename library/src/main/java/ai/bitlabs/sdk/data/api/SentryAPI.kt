package ai.bitlabs.sdk.data.api

import ai.bitlabs.sdk.data.model.sentry.SendEnvelopeResponse
import ai.bitlabs.sdk.data.model.sentry.SentryManager
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

internal interface SentryAPI {
    @POST("/api/{projectId}/envelope/")
    @Headers("Content-Type: application/x-sentry-envelope")
    fun sendEnvelope(
        @Path("projectId") projectId: String = SentryManager.projectId, @Body envelope: RequestBody
    ): Call<SendEnvelopeResponse>
}