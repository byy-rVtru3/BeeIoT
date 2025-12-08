package com.app.mobile.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.app.mobile.R
import com.app.mobile.presentation.validators.ValidationError
import com.app.mobile.presentation.validators.toErrorMessage
import com.app.mobile.ui.theme.Dimens

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val borderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = Dimens.BorderWidthNormal.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.TextFieldPaddingHorizontal, vertical = Dimens.TextFieldPaddingVertical),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f).padding(end = Dimens.TextFieldIconEndPadding)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }

                if (trailingIcon != null) {
                    Box(
                        modifier = Modifier.size(Dimens.TextFieldIconSize),
                        contentAlignment = Alignment.Center
                    ) {
                        trailingIcon()
                    }
                } else {
                    Box(modifier = Modifier.size(Dimens.TextFieldIconSize))
                }
            }
        }
    )
}

@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: ValidationError? = null,
    supportingText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        CustomTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            isError = error != null,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon
        )

        // Текст для отображения: ошибка или подсказка
        val displayText = error?.toErrorMessage() ?: supportingText
        // Шаблон для резервирования высоты: подсказка или пустая строка (для одной строки)
        val templateText = supportingText ?: " "

        Box(modifier = Modifier.padding(start = Dimens.TextFieldPaddingHorizontal, top = Dimens.TextFieldErrorTopPadding)) {
            // Невидимый текст для резервирования высоты
            Text(
                text = templateText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0f)
            )
            // Видимый текст поверх
            Text(
                text = displayText ?: "",
                color = if (error != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: ValidationError? = null,
    supportingText: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        error = error,
        supportingText = supportingText,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            Icon(
                painter = painterResource(
                    id = if (passwordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                ),
                contentDescription = if (passwordVisible) {
                    stringResource(R.string.hide_password)
                } else {
                    stringResource(R.string.show_password)
                },
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(Dimens.TextFieldIconSize)
                    .clickable { passwordVisible = !passwordVisible }
            )
        }
    )
}

@Composable
fun OtpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    otpLength: Int = 6,
    isError: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    val borderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline

    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= otpLength && newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        modifier = modifier.focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.OtpCellSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(otpLength) { index ->
                    val char = value.getOrNull(index)?.toString() ?: ""

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .drawBehind {
                                val strokeWidth = Dimens.BorderWidthNormal.toPx()
                                val y = size.height - strokeWidth / 2
                                drawLine(
                                    color = borderColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth
                                )
                            }
                            .padding(top = Dimens.OtpCellPaddingTop, bottom = Dimens.OtpCellPaddingBottom),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
