package com.app.mobile.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.app.mobile.domain.repository.notifications.TokenRetryScheduler
import java.util.concurrent.TimeUnit

class FcmTokenRetryScheduler(
	private val context: Context
) : TokenRetryScheduler {

	private companion object {

		const val RETRY_DELAY_MINUTES = 10L
		const val BACKOFF_DELAY_MINUTES = 10L
	}

	override fun scheduleRetry() {
		WorkManager.getInstance(context).enqueue(
			OneTimeWorkRequestBuilder<FcmTokenRetryWorker>()
				.setConstraints(
					Constraints.Builder()
						.setRequiredNetworkType(NetworkType.CONNECTED)
						.build()
				)
				.setInitialDelay(RETRY_DELAY_MINUTES, TimeUnit.MINUTES)
				.setBackoffCriteria(
					BackoffPolicy.EXPONENTIAL,
					BACKOFF_DELAY_MINUTES,
					TimeUnit.MINUTES
				)
				.build()
		)
	}
}