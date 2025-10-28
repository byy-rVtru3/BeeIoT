package com.app.mobile.presentation.validators

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.app.mobile.R

@Composable
fun ValidationError.toErrorMessage(): String {
    return when (this) {
        is EmptyFieldError -> stringResource(R.string.error_empty_field)
        is InvalidEmailError -> stringResource(R.string.error_invalid_email)
        is PasswordTooShortError -> stringResource(R.string.error_password_too_short)
        is PasswordTooWeakError -> stringResource(R.string.error_password_too_weak)
        is PasswordsNotMatchError -> stringResource(R.string.error_passwords_not_match)
        is InvalidCodeFormatError -> stringResource(R.string.error_invalid_code)
        is InvalidNameError -> stringResource(R.string.error_invalid_name)
        is NameTooShortError -> stringResource(R.string.error_name_too_short)
        is NameTooLongError -> stringResource(R.string.error_name_too_long)
        else -> stringResource(R.string.error_unknown)
    }
}
