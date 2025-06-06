package ai.bitlabs.sdk.offerwall.components

import ai.bitlabs.sdk.R
import ai.bitlabs.sdk.offerwall.theme.BLColors
import ai.bitlabs.sdk.offerwall.theme.BLStyle
import ai.bitlabs.sdk.offerwall.shared.BLText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun BLLeaveSurveyDialog(onDismiss: () -> Unit, leaveSurvey: (String) -> Unit) {
    val options = arrayOf("SENSITIVE", "UNINTERESTING", "TECHNICAL", "TOO_LONG", "OTHER")
    val optionsDisplay = arrayOf(
        stringResource(R.string.leave_reason_sensitive),
        stringResource(R.string.leave_reason_uninteresting),
        stringResource(R.string.leave_reason_technical),
        stringResource(R.string.leave_reason_too_long),
        stringResource(R.string.leave_reason_other)
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            BLText(
                text = stringResource(R.string.leave_dialog_title),
                style = BLStyle.h6
            )
            Spacer(modifier = Modifier.height(8.dp))
            optionsDisplay.forEachIndexed { index, option ->
                BLText(
                    text = option,
                    style = BLStyle.normal.copy(fontSize = 16.sp),
                    height = 48.dp,
                    onClick = { leaveSurvey(options[index]) }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            BLText(
                text = stringResource(R.string.leave_dialog_continue).uppercase(),
                height = 48.dp,
                style = BLStyle.medium.copy(color = BLColors.Accent),
                onClick = { onDismiss() },
                contentAlignment = Alignment.CenterEnd,
            )
        }
    }
}

@Preview
@Composable
fun BLLeaveSurveyDialogPreview() {
    BLLeaveSurveyDialog(
        onDismiss = {},
        leaveSurvey = {}
    )
}