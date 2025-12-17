package com.app.mobile.presentation.validators

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.app.mobile.R

@Composable
fun ValidationError.toErrorMessage(): String {
    return when (this) {
        ValidationError.EmptyFieldError -> stringResource(R.string.error_empty_field)
        ValidationError.InvalidEmailError -> stringResource(R.string.error_invalid_email)
        ValidationError.PasswordTooShortError -> stringResource(R.string.error_password_too_short)
        ValidationError.PasswordTooWeakError -> stringResource(R.string.error_password_too_weak)
        ValidationError.PasswordsNotMatchError -> stringResource(R.string.error_passwords_not_match)
        ValidationError.InvalidCodeFormatError -> stringResource(R.string.error_invalid_code)
        ValidationError.InvalidNameError -> stringResource(R.string.error_invalid_name)
        ValidationError.NameTooShortError -> stringResource(R.string.error_name_too_short)
        ValidationError.NameTooLongError -> stringResource(R.string.error_name_too_long)
    }
}
