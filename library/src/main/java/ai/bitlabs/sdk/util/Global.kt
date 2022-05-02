package ai.bitlabs.sdk.util

import ai.bitlabs.sdk.data.model.Category
import ai.bitlabs.sdk.data.model.Details
import ai.bitlabs.sdk.data.model.Survey
import kotlin.random.Random

internal const val TAG = "BitLabs"

internal const val BUNDLE_KEY_PARAMS = "bundle-key-params"

internal fun randomSurvey(i: Int) = with(Random(i)) {
    Survey(
        networkId = nextInt(),
        id = i,
        cpi = "0.5",
        value = "0.5",
        loi = nextDouble(30.0),
        remaining = 3,
        details = Details(Category("Survey-$i", "")),
        rating = nextInt(6),
        link = "",
        missingQuestions = 0
    )
}