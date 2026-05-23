package com.app.mobile.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.mobile.domain.usecase.notifications.SendPushTokenUseCase
import org.koin.java.KoinJavaComponent.inject

class FcmTokenRetryWorker(
	context: Context,
	workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

	private val sendPushTokenUseCase: SendPushTokenUseCase by inject(SendPushTokenUseCase::class.java)

	override suspend fun doWork(): Result {
		return try {
			sendPushTokenUseCase()
			Result.success()
		} catch (_: Exception) {
			Result.retry()
		}
	}
}