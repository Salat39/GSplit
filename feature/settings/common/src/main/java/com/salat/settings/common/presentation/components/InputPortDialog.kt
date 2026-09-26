package com.salat.settings.common.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.uikit.component.BaseDialog
import com.salat.uikit.component.DialogButton
import com.salat.uikit.component.DialogButtonKind
import com.salat.uikit.component.DialogButtons
import com.salat.uikit.component.DialogContainerPadding
import com.salat.uikit.component.DialogInsetShape
import com.salat.uikit.component.DialogTextPadding
import com.salat.uikit.component.DialogTitle
import com.salat.uikit.component.DialogTopPadding
import com.salat.uikit.component.SettingsDefaults
import com.salat.uikit.theme.AppTheme
import presentation.capitalizeFirstLetter

@Suppress("SameParameterValue")
@Composable
fun InputPortDialog(
    title: String,
    uiScaleState: Float? = null,
    onFinishInput: (Int) -> Unit,
    onDismiss: () -> Unit = {}
) = BaseDialog(uiScaleState = uiScaleState, onDismiss = onDismiss) {
    Column(modifier = Modifier.padding(top = DialogTopPadding)) {
        DialogTitle(stringResource(R.string.connection_port))

        Spacer(Modifier.height(5.dp))

        Text(
            text = stringResource(R.string.enter_your_port),
            modifier = Modifier.padding(horizontal = DialogTextPadding),
            color = AppTheme.colors.contentPrimary.copy(SettingsDefaults.SUBTITLE_ALPHA),
            style = AppTheme.typography.dialogSubtitle
        )

        val fieldTypography = AppTheme.typography.stubTitle
        val keyboardController = LocalSoftwareKeyboardController.current

        var inputValue: TextFieldValue by remember {
            val filteredTitle = title.filter { it.isDigit() }
            mutableStateOf(
                TextFieldValue(
                    text = filteredTitle,
                    selection = TextRange(filteredTitle.length)
                )
            )
        }

        val enableOk by remember {
            derivedStateOf { inputValue.text.isNotEmpty() }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = DialogContainerPadding, vertical = 16.dp)
        ) {
            BasicTextField(
                value = inputValue,
                onValueChange = { newValue ->
                    // Allow only digits to be entered to ensure valid port input
                    val filteredText = newValue.text.filter { it.isDigit() }
                    inputValue = newValue.copy(
                        text = filteredText,
                        selection = TextRange(filteredText.length)
                    )
                },
                cursorBrush = SolidColor(AppTheme.colors.contentAccent),
                textStyle = fieldTypography.copy(color = AppTheme.colors.contentPrimary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputValue.text.isEmpty()) {
                            Text(
                                text = "5555",
                                style = fieldTypography.copy(
                                    color = AppTheme.colors.contentPrimary.copy(.4f)
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Number,
                    autoCorrectEnabled = false
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Close the on-screen keyboard when the user finishes input
                        keyboardController?.hide()
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(DialogInsetShape)
                    .border(
                        shape = DialogInsetShape,
                        width = 1.dp,
                        color = AppTheme.colors.contentPrimary.copy(.08f)
                    )
                    .background(AppTheme.colors.surfaceLayer1)
            )
        }

        DialogButtons {
            DialogButton(
                text = stringResource(android.R.string.cancel).capitalizeFirstLetter(),
                onClick = onDismiss
            )

            DialogButton(
                text = stringResource(android.R.string.ok),
                kind = DialogButtonKind.Accent,
                enabled = enableOk,
                onClick = { onFinishInput(inputValue.text.toIntOrNull() ?: 0) }
            )
        }
    }
}
