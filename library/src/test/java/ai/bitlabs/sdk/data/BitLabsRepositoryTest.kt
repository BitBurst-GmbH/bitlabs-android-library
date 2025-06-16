package ai.bitlabs.sdk.data

import ai.bitlabs.sdk.data.api.BitLabsAPI
import ai.bitlabs.sdk.data.model.bitlabs.BitLabsResponse
import ai.bitlabs.sdk.data.model.bitlabs.Category
import ai.bitlabs.sdk.data.model.bitlabs.Configuration
import ai.bitlabs.sdk.data.model.bitlabs.GetAppSettingsResponse
import ai.bitlabs.sdk.data.model.bitlabs.GetSurveysResponse
import ai.bitlabs.sdk.data.model.bitlabs.RestrictionReason
import ai.bitlabs.sdk.data.model.bitlabs.Survey
import ai.bitlabs.sdk.data.repositories.BitLabsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class BitLabsRepositoryTest {

    @MockK
    private lateinit var bitLabsAPI: BitLabsAPI

    @InjectMockKs
    private lateinit var bitLabsRepository: BitLabsRepository

    private inline fun <reified T : Any> createBitLabsResponse(data: T) =
        BitLabsResponse(data, null, "", "")

    private inline fun <reified T : Any> createBitLabsErrorResponse(): Response<BitLabsResponse<T>> {
        val errorJson = """
        {
            "error": {
                "details": {
                    "http": 400,
                    "msg": "Mock Request Error"
                },
                "status": ""
            }
        }
        """.trimIndent()

        val errorResponseBody = ResponseBody.create(
            MediaType.parse("application/json"),
            errorJson
        )

        return mockk(relaxed = true) {
            every { isSuccessful } returns false
            every { body() } returns null
            every { errorBody() } returns errorResponseBody
        }
    }


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun leaveSurvey_Failure() = runTest {
        coEvery { bitLabsAPI.updateClick(any(), any()) } throws
                Exception("Unexpected Error")

        try {
            bitLabsRepository.leaveSurvey("clickId", "mockReason")
            fail("Expected an exception to be thrown")
        } catch (e: Exception) {
            assertThat(e.message).contains("Unexpected Error")
        }
    }

    @Test
    fun leaveSurvey_Response_Error() = runTest {
        val mockResponse = createBitLabsErrorResponse<Unit>()

        coEvery { bitLabsAPI.updateClick(any(), any()) } returns mockResponse

        try {
            bitLabsRepository.leaveSurvey("clickId", "mockReason")
            fail("Expected an exception to be thrown")
        } catch (e: Exception) {
            assertThat(e.message).contains("400")
            assertThat(e.message).contains("Mock Request Error")
        }
    }

    @Test
    fun leaveSurvey_Response_Success() = runTest {
        val mockBitLabsResponse = createBitLabsResponse(mockk<Unit>())

        val mockResponse = Response.success(mockBitLabsResponse)

        coEvery { bitLabsAPI.updateClick(any(), any()) } returns mockResponse

        try {
            bitLabsRepository.leaveSurvey("", "")
        } catch (e: Exception) {
            fail("Expected no exception, but got: ${e.message}")
        }
    }

    @Test
    fun getSurveys_Failure() = runTest {
        coEvery { bitLabsAPI.getSurveys(any()) } throws
                Exception("Unexpected Error")

        try {
            bitLabsRepository.getSurveys("")
            fail("Expected an exception to be thrown")
        } catch (e: Exception) {
            assertThat(e.message).contains("Unexpected Error")
        }
    }

    @Test
    fun getSurveys_Response_Error() = runTest {
        val mockResponse = createBitLabsErrorResponse<GetSurveysResponse>()

        coEvery { bitLabsAPI.getSurveys(any()) } returns mockResponse

        try {
            bitLabsRepository.getSurveys("")
            fail("Expected an exception to be thrown")
        } catch (e: Exception) {
            assertThat(e.message).contains("400")
            assertThat(e.message).contains("Mock Request Error")
        }
    }

    @Test
    fun getSurveys_Response_Success() = runTest {
        val mockSurveysResponse = GetSurveysResponse(
            surveys = listOf(
                Survey(
                    id = "mockId",
                    type = "mockType",
                    clickUrl = "https://example.com/survey1",
                    cpi = "mockCPI",
                    value = "mockValue",
                    loi = 1.0,
                    country = "mockCountry",
                    language = "mockLanguage",
                    rating = 3,
                    category = mockk<Category>(),
                    tags = listOf("mockTag"),
                )
            ),
            restrictionReason = null
        )

        val mockBitLabsResponse = createBitLabsResponse(mockSurveysResponse)

        val mockResponse = Response.success(mockBitLabsResponse)

        coEvery { bitLabsAPI.getSurveys(any()) } returns mockResponse

        try {
            val surveys = bitLabsRepository.getSurveys("")
            assertThat(surveys).isNotEmpty()
            assertThat(surveys[0].id).isEqualTo("mockId")
            assertThat(surveys[0].type).isEqualTo("mockType")
            assertThat(surveys[0].clickUrl).isEqualTo("https://example.com/survey1")
            assertThat(surveys[0].cpi).isEqualTo("mockCPI")
            assertThat(surveys[0].value).isEqualTo("mockValue")
            assertThat(surveys[0].loi).isEqualTo(1.0)
            assertThat(surveys[0].country).isEqualTo("mockCountry")
            assertThat(surveys[0].language).isEqualTo("mockLanguage")
            assertThat(surveys[0].rating).isEqualTo(3)
            assertThat(surveys[0].tags).containsExactly("mockTag")
            assertThat(surveys[0].category).isNotNull()
        } catch (e: Exception) {
            fail("Expected no exception, but got: ${e.message}")
        }
    }

    @Test
    fun getSurveys_Response_Restriction() = runTest {
        var expectedRestrictionReason = RestrictionReason(
            reason = "Mock Restriction Reason",
            notVerified = true,
            usingVpn = null,
            bannedUntil = null,
            unsupportedCountry = null,
        )

        val mockSurveysResponse = GetSurveysResponse(
            surveys = emptyList(),
            restrictionReason = expectedRestrictionReason
        )

        val mockBitLabsResponse = createBitLabsResponse(mockSurveysResponse)

        val mockResponse = Response.success(mockBitLabsResponse)

        coEvery { bitLabsAPI.getSurveys(any()) } returns mockResponse

        try {
            bitLabsRepository.getSurveys("")
            fail("Expected an exception to be thrown")
        } catch (e: Exception) {
            assertThat(e.message).contains(expectedRestrictionReason.prettyPrint())
        }
    }


    @Test
    fun getAppSettings_Failure() = runTest {
        coEvery { bitLabsAPI.getAppSettings(any()) } throws
                Exception("Unexpected Error")

        try {
            bitLabsRepository.getAppSettings("")
            fail("Expected an exception to be thrown")
        } catch (e: Exception) {
            assertThat(e.message).contains("Unexpected Error")
        }
    }


    @Test
    fun getAppSettings_Response_Success() = runTest {
        val expectedConfiguration = listOf(
            Configuration(
                internalIdentifier = "app.visual.light.navigation_color",
                value = "#FFFFFF"
            ),
            Configuration(
                internalIdentifier = "app.visual.light.background_color",
                value = "#000000"
            )
        )

        val mockResponse = GetAppSettingsResponse(configuration = expectedConfiguration)

        coEvery { bitLabsAPI.getAppSettings(any()) } returns mockResponse

        try {
            val config = bitLabsRepository.getAppSettings("").configuration
            assertThat(config).isNotEmpty()
            assertThat(config).containsExactlyElementsIn(expectedConfiguration)

            assertThat(config.find { it.internalIdentifier == "app.visual.light.navigation_color" }?.value)
                .isEqualTo("#FFFFFF")

            assertThat(config.find { it.internalIdentifier == "app.visual.light.background_color" }?.value)
                .isEqualTo("#000000")
        } catch (e: Exception) {
            fail("Expected no exception, but got: ${e.message}")
        }
    }
}