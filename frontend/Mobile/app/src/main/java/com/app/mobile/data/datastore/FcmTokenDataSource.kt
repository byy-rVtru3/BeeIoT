package com.app.mobile.data.datastore

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

interface FcmTokenDataSource {

	suspend fun getToken(): String
}

class FcmTokenDataSourceImpl : FcmTokenDataSource {

	override suspend fun getToken(): String = FirebaseMessaging.getInstance().token.await()
}