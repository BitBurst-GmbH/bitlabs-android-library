package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.data.util.body
import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonSyntaxException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertThrows
import org.junit.Test

class ResponseBodyExtensionKtTest {

    @Test
    fun errorBody_NonBitLabsResponseBody_ReturnsNull() {
        val json = "Response body that isn't a BitLabsResponse body"

        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        assertThrows(JsonSyntaxException::class.java) {
            responseBody.body<Unit>()
        }
    }

    @Test
    fun errorBody_BitLabsResponseBody_ReturnsParsedObject() {
        val json = """{ error:{details:{http:400,msg:"Any Request"}}, status:""}""".trimIndent()

        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val bitLabsResponse = responseBody.body<Unit>()

        assertThat(bitLabsResponse).isNotNull()
        assertThat(bitLabsResponse?.error).isNotNull()
        assertThat(bitLabsResponse?.error?.details).isNotNull()
        assertThat(bitLabsResponse?.error?.details?.http).isEqualTo("400")
        assertThat(bitLabsResponse?.error?.details?.msg).isEqualTo("Any Request")
    }

    @Test
    fun errorBody_EmptyBody_ReturnsNull() {
        val json = ""

        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val bitLabsResponse = responseBody.body<Unit>()

        assertThat(bitLabsResponse).isNull()
    }

    data class Data(val id: Int, val name: String)

    @Test
    fun body_ValidBitLabsResponseBodyWithData_ReturnsParsedObject() {
        val json = """{"error": null, "status": "success", "data": {"id": 1, "name": "Sample"}}"""
        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val bitLabsResponse = responseBody.body<Data>()
        assertThat(bitLabsResponse).isNotNull()
        assertThat(bitLabsResponse?.status).isEqualTo("success")
        assertThat(bitLabsResponse?.data).isNotNull()
        assertThat(bitLabsResponse?.data?.id).isEqualTo(1)
        assertThat(bitLabsResponse?.data?.name).isEqualTo("Sample")
    }

    @Test
    fun body_BitLabsResponseBodyWithPartialData_ReturnsParsedObjectWithNullField() {
        val json =
            """{"error": {"details": {"msg": "Any Request"}}, "status": ""}""" // missing "http"
        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())
        val bitLabsResponse = responseBody.body<Unit>()
        assertThat(bitLabsResponse).isNotNull()
        assertThat(bitLabsResponse?.error).isNotNull()
        assertThat(bitLabsResponse?.error?.details).isNotNull()
        assertThat(bitLabsResponse?.error?.details?.http).isNull() // missing field should be null
        assertThat(bitLabsResponse?.error?.details?.msg).isEqualTo("Any Request")
    }
}