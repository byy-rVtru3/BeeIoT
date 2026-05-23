package com.app.mobile.di

import com.google.firebase.Firebase
import com.google.firebase.installations.installations
import org.koin.dsl.module

val firebaseModule = module {
	single { Firebase.installations }
}