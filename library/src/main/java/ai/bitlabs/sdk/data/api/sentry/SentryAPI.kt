package ai.bitlabs.sdk.data.api.sentry

import ai.bitlabs.sdk.data.model.sentry.SentryManager
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface SentryAPI {
    @POST("/api/{projectId}/envelope/")
    fun sendEnvelope(
        @Path("projectId") projectId: String = SentryManager.projectId, @Body envelope: Any
    ): Call<Unit>
}