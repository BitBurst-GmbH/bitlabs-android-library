package ai.bitlabs.sdk.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GlobalKtTest {

    @Test
    fun rounded_TwoOrMoreDecimalPoints_TwoDecimalPoints() {
        val numStr = "1.23456789"
        val expected = "1.23"

        val rounded = numStr.rounded()
        assertThat(rounded).isEqualTo(expected)
    }

    @Test
    fun rounded_OneDecimalPoints_OneDecimalPoints() {
        val numStr = "1.2"
        val expected = "1.2"

        val rounded = numStr.rounded()
        assertThat(rounded).isEqualTo(expected)
    }

    @Test
    fun rounded_NoDecimalPoints_NoDecimalPoints() {
        val numStr = "1"
        val expected = "1"

        val rounded = numStr.rounded()
        assertThat(rounded).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_SnakeCase_CamelCase() {
        val snakeCase = "snake_case"
        val expected = "snakeCase"

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_CamelCase_CamelCase() {
        val snakeCase = "camelCase"
        val expected = "camelCase"

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_UnderscoreAtFirst_CamelCase() {
        val snakeCase = "_snake_case"
        val expected = "snakeCase"

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_UnderscoreAtLast_CamelCase() {
        val snakeCase = "snake_case_"
        val expected = "snakeCase"

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }
}