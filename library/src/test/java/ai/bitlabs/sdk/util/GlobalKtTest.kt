package ai.bitlabs.sdk.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GlobalKtTest {

    @Test
    fun snakeToCamelCase_SnakeCase_CamelCase() {
        val snakeCase = "snake_case"
        val expected = "snakeCase"

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

    @Test
    fun snakeToCamelCase_EmptyString_EmptyString() {
        val snakeCase = ""
        val expected = ""

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_OnlyUnderscores_EmptyString() {
        val snakeCase = "____"
        val expected = ""

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_MultipleConsecutiveUnderscores_CamelCase() {
        val snakeCase = "snake___case"
        val expected = "snakeCase"

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_MultipleUnderscoreAtStart_CamelCase() {
        val snakeCase = "___snake_case"
        val expected = "snakeCase"

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_MultipleUnderscoreAtEnd_CamelCase() {
        val snakeCase = "snake_case___"
        val expected = "snakeCase"

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun snakeToCamelCase_Uppercase_StringToCamelCase() {
        val snakeCase = "SNAKE_CASE"
        val expected = "snakeCase"

        val camelCase = snakeCase.snakeToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun convertKeysToCamelCase_SingleSnakeCaseKey_ConvertsToCamelCase() {
        val snakeCase = "{\"snake_case\": 1}"
        val expected = "{\"snakeCase\": 1}"

        val camelCase = snakeCase.convertKeysToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun convertKeysToCamelCase_MultipleSnakeCaseKeys_ConvertsAllToCamelCase() {
        val snakeCase = "{\"snake_case\": 1, \"another_snake_case\": 2}"
        val expected = "{\"snakeCase\": 1, \"anotherSnakeCase\": 2}"

        val camelCase = snakeCase.convertKeysToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun convertKeysToCamelCase_MixedCaseKeys_UnchangedForNonSnakeCase() {
        val snakeCase = "{\"snake_case\": 1, \"anotherCamelCase\": 2}"
        val expected = "{\"snakeCase\": 1, \"anotherCamelCase\": 2}"

        val camelCase = snakeCase.convertKeysToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun convertKeysToCamelCase_NoSnakeCaseKeys_Unchanged() {
        val snakeCase = "{\"snakeCase\": 1, \"anotherCamelCase\": 2}"
        val expected = "{\"snakeCase\": 1, \"anotherCamelCase\": 2}"

        val camelCase = snakeCase.convertKeysToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun convertKeysToCamelCase_EmptyString_Unchanged() {
        val snakeCase = ""
        val expected = ""

        val camelCase = snakeCase.convertKeysToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun convertKeysToCamelCase_NestedObjects_ConvertsAllNestedKeys() {
        val snakeCase = "{\"snake_case\": {\"another_snake_case\": 2}}"
        val expected = "{\"snakeCase\": {\"anotherSnakeCase\": 2}}"

        val camelCase = snakeCase.convertKeysToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }

    @Test
    fun convertKeysToCamelCase_ArrayOfObjects_ConvertsAllKeysInArray() {
        val snakeCase = "[{\"snake_case\": 1, \"another_snake_case\": 2}]"
        val expected = "[{\"snakeCase\": 1, \"anotherSnakeCase\": 2}]"

        val camelCase = snakeCase.convertKeysToCamelCase()
        assertThat(camelCase).isEqualTo(expected)
    }
}