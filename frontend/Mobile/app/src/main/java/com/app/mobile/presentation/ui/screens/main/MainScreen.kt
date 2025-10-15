package com.app.mobile.presentation.ui.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.mobile.presentation.ui.screens.registration.RegistrationScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun MyApp() {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        RegistrationScreen(
            registrationViewModel = koinViewModel(), modifier = Modifier.padding
                (paddingValues)
        )
    }
}