package ai.bitlabs.sdk.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GlobalKtTest {

    @Test
    fun rounded_WhenTwoOrMoreDecimalPoints_ExpectTwoDecimalPoints() {
        val numStr = "1.23456789"
        val expected = "1.23"

        val rounded = numStr.rounded()
        assertThat(rounded).isEqualTo(expected)
    }

    @Test
    fun rounded_WhenOneDecimalPoints_ExpectTwoDecimalPoints() {
        val numStr = "1.2"
        val expected = "1.2"

        val rounded = numStr.rounded()
        assertThat(rounded).isEqualTo(expected)
    }

    @Test
    fun rounded_WhenNoDecimalPoints_ExpectNoDecimalPoints() {
        val numStr = "1"
        val expected = "1"

        val rounded = numStr.rounded()
        assertThat(rounded).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_WhenSnakeCase_ExpectCamelCase() {
        val snakeCase = "snake_case"
        val expected = "snakeCase"

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_WhenCamelCase_ExpectCamelCase() {
        val snakeCase = "camelCase"
        val expected = "camelCase"

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_WhenUnderscoreAtFirst_ExpectCamelCase() {
        val snakeCase = "_snake_case"
        val expected = "snakeCase"

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_WhenUnderscoreAtLast_ExpectCamelCase() {
        val snakeCase = "snake_case_"
        val expected = "snakeCase"

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }
}