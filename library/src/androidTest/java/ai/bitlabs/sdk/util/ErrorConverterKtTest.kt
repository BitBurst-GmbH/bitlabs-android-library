package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.data.model.BitLabsResponse
import com.google.common.truth.Truth.assertThat
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test

class ErrorConverterKtTest {

    @Test
    fun errorBody_NonBitLabsResponseBody_Null() {
        val json = "Response body that isn't a BitLabsResponse body"

        val responseBody = ResponseBody.create(MediaType.parse("application/json"), json)

        val bitLabsResponse = responseBody.body<BitLabsResponse<Unit>>()

        assertThat(bitLabsResponse).isNull()
    }

    @Test
    fun errorBody_BitLabsResponseBody_BitLabsResponse() {
        val json = """{ error:{details:{http:400,msg:"Any Request"}}, status:""}""".trimIndent()

        val responseBody = ResponseBody.create(MediaType.parse("application/json"), json)

        val bitLabsResponse = responseBody.body<BitLabsResponse<Unit>>()

        assertThat(bitLabsResponse).isNotNull()
        assertThat(bitLabsResponse?.error).isNotNull()
        assertThat(bitLabsResponse?.error?.details).isNotNull()
        assertThat(bitLabsResponse?.error?.details?.http).isEqualTo("400")
        assertThat(bitLabsResponse?.error?.details?.msg).isEqualTo("Any Request")
    }

    @Test
    fun errorBody_EmptyBody_Null() {
        val json = ""

        val responseBody = ResponseBody.create(MediaType.parse("application/json"), json)

        val bitLabsResponse = responseBody.body<BitLabsResponse<Unit>>()

        assertThat(bitLabsResponse).isNull()
    }
}