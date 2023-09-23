package ai.bitlabs.sdk.data

import ai.bitlabs.sdk.data.model.BitLabsResponse
import ai.bitlabs.sdk.data.model.GetAppSettingsResponse
import ai.bitlabs.sdk.data.model.GetLeaderboardResponse
import ai.bitlabs.sdk.data.model.GetSurveysResponse
import ai.bitlabs.sdk.data.model.Survey
import ai.bitlabs.sdk.data.network.BitLabsAPI
import ai.bitlabs.sdk.util.OnExceptionListener
import ai.bitlabs.sdk.util.OnResponseListener
import android.util.Log
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BitLabsRepositoryTest {

    @MockK
    private lateinit var bitLabsAPI: BitLabsAPI

    @MockK(relaxed = true)
    private lateinit var onExceptionListener: OnExceptionListener

    @InjectMockKs
    private lateinit var bitLabsRepository: BitLabsRepository

    private inline fun <reified T : Any> getWorkingResponseBody() =
        BitLabsResponse(mockk<T>(relaxed = true), null, "", "")

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun leaveSurvey_Failure() = mockkStatic(Log::class) {
        every { Log.e(any(), any()) } returns 0

        every { bitLabsAPI.updateClick(any(), any()) } returns object :
            BitLabsCall<BitLabsResponse<Unit>>() {
            override fun enqueue(callback: Callback<BitLabsResponse<Unit>>) {
                callback.onFailure(this, Throwable())
            }
        }

        bitLabsRepository.leaveSurvey("", "")

        verify { Log.e(any(), any()) }
    }

    @Test
    fun leaveSurvey_Response_Error() = mockkStatic(Log::class) {
        every { Log.e(any(), any()) } returns 0

        val errorResponseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            "{error:{details:{http:400,msg:\"Any Request\"}}, status:\"\"}"
        )

        every { bitLabsAPI.updateClick(any(), any()) } returns object :
            BitLabsCall<BitLabsResponse<Unit>>() {
            override fun enqueue(callback: Callback<BitLabsResponse<Unit>>) {
                callback.onResponse(this, Response.error(400, errorResponseBody))
            }
        }

        bitLabsRepository.leaveSurvey("", "")

        verify { Log.e(any(), any()) }
    }

    @Test
    fun leaveSurvey_Response_Success() = mockkStatic(Log::class) {
        every { Log.i(any(), any()) } returns 0

        every { bitLabsAPI.updateClick(any(), any()) } returns object :
            BitLabsCall<BitLabsResponse<Unit>>() {
            override fun enqueue(callback: Callback<BitLabsResponse<Unit>>) {
                callback.onResponse(this, Response.success(getWorkingResponseBody()))
            }
        }

        bitLabsRepository.leaveSurvey("", "")

        verify { Log.i(any(), any()) }
    }

    @Test
    fun getSurveys_Failure() {
        every { bitLabsAPI.getSurveys(any()) } returns object :
            BitLabsCall<BitLabsResponse<GetSurveysResponse>>() {
            override fun enqueue(callback: Callback<BitLabsResponse<GetSurveysResponse>>) {
                callback.onFailure(this, Throwable())
            }

        }

        bitLabsRepository.getSurveys("", {}, onExceptionListener)

        verify { onExceptionListener.onException(any()) }
    }

    @Test
    fun getSurveys_Response_Error() {
        val errorResponseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            "{error:{details:{http:400,msg:\"Any Request\"}}, status:\"\"}"
        )

        every { bitLabsAPI.getSurveys(any()) } returns object :
            BitLabsCall<BitLabsResponse<GetSurveysResponse>>() {
            override fun enqueue(callback: Callback<BitLabsResponse<GetSurveysResponse>>) {
                callback.onResponse(this, Response.error(400, errorResponseBody))
            }
        }

        bitLabsRepository.getSurveys("", {}, onExceptionListener)

        verify { onExceptionListener.onException(any()) }
    }

    @Test
    fun getSurveys_Response_Success() {
        val onResponseListener = mockk<OnResponseListener<List<Survey>>>(relaxed = true)

        every { bitLabsAPI.getSurveys(any()) } returns object :
            BitLabsCall<BitLabsResponse<GetSurveysResponse>>() {
            override fun enqueue(callback: Callback<BitLabsResponse<GetSurveysResponse>>) {
                callback.onResponse(this, Response.success(getWorkingResponseBody()))
            }
        }

        bitLabsRepository.getSurveys("", onResponseListener) {}

        verify { onResponseListener.onResponse(any()) }
    }

    @Test
    fun getAppSettings_Failure() {
        every { bitLabsAPI.getAppSettings() } returns object :
            BitLabsCall<BitLabsResponse<GetAppSettingsResponse>>() {
            override fun enqueue(callback: Callback<BitLabsResponse<GetAppSettingsResponse>>) {
                callback.onFailure(this, Throwable())
            }
        }

        bitLabsRepository.getAppSettings({}, onExceptionListener)

        verify { onExceptionListener.onException(any()) }
    }

    @Test
    fun getAppSettings_Response_Error() {
        val errorResponseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            "{error:{details:{http:400,msg:\"Any Request\"}}, status:\"\"}"
        )

        every { bitLabsAPI.getAppSettings() } returns object :
            BitLabsCall<BitLabsResponse<GetAppSettingsResponse>>() {
            override fun enqueue(callback: Callback<BitLabsResponse<GetAppSettingsResponse>>) {
                callback.onResponse(this, Response.error(400, errorResponseBody))
            }
        }

        bitLabsRepository.getAppSettings({}, onExceptionListener)

        verify { onExceptionListener.onException(any()) }
    }

    @Test
    fun getAppSettings_Response_Success() {
        val onResponseListener = mockk<OnResponseListener<GetAppSettingsResponse>>(relaxed = true)

        every { bitLabsAPI.getAppSettings() } returns object :
            BitLabsCall<BitLabsResponse<GetAppSettingsResponse>>() {
            override fun enqueue(callback: Callback<BitLabsResponse<GetAppSettingsResponse>>) {
                callback.onResponse(this, Response.success(getWorkingResponseBody()))
            }
        }

        bitLabsRepository.getAppSettings(onResponseListener) {}

        verify { onResponseListener.onResponse(any()) }
    }


    @Test
    fun getLeaderboard_Failure() {
        every { bitLabsAPI.getLeaderboard() } returns object :
            BitLabsCall<BitLabsResponse<GetLeaderboardResponse>>() {
            override fun enqueue(callback: Callback<BitLabsResponse<GetLeaderboardResponse>>) {
                callback.onFailure(this, Throwable())
            }
        }

        bitLabsRepository.getLeaderboard({}, onExceptionListener)

        verify { onExceptionListener.onException(any()) }
    }

    @Test
    fun getLeaderboard_Response_Error() {
        val errorResponseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            "{error:{details:{http:400,msg:\"Any Request\"}}, status:\"\"}"
        )

        every { bitLabsAPI.getLeaderboard() } returns object :
            BitLabsCall<BitLabsResponse<GetLeaderboardResponse>>() {
            override fun enqueue(callback: Callback<BitLabsResponse<GetLeaderboardResponse>>) {
                callback.onResponse(this, Response.error(400, errorResponseBody))
            }
        }

        bitLabsRepository.getLeaderboard({}, onExceptionListener)

        verify { onExceptionListener.onException(any()) }
    }
}

abstract class BitLabsCall<T> : Call<T> {
    override fun clone() = this

    override fun execute(): Response<T> {
        TODO("Not yet implemented")
    }

    override fun isExecuted() = false

    override fun cancel() {}

    override fun isCanceled() = false

    override fun request(): Request = Request.Builder().build()

    override fun timeout() = Timeout()
}