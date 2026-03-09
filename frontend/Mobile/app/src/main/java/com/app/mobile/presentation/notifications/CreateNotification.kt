package com.app.mobile.presentation.notifications

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.mobile.R

const val NOTIFICATION_THRESHOLD = 2
const val SUMMARY_NOTIFICATION_ID = 0

/**
 * Функция для отображния уведомлений.
 *
 * @param channelId Идентификатор канала уведомлений.
 * @param id Уникальный идентификатор уведомления.
 * @param groupKey Ключ, по которому будут группироваться уведомления.
 * @param intent Интент, применяемый к уведомлению.
 * @param intentRequestCode Код ответа интента.
 * @param context Контекст для создания уведомления.
 * @param category Категория уведомления. Значение берется из [NotificationCompat]
 * @param priority Приоритет уведомления. Значение по умолчанию - [NotificationCompat.PRIORITY_DEFAULT].
 * @param visibility Видимость уведомления. Значение по умолчанию - [NotificationCompat.VISIBILITY_PRIVATE].
 */
@SuppressLint("MissingPermission")
fun createNotification(
	channelId: String,
	id: Int,
	title: String,
	body: String,
	groupKey: String,
	intent: Intent,
	intentRequestCode: Int,
	context: Context,
	category: String,
	priority: Int = NotificationCompat.PRIORITY_DEFAULT,
	visibility: Int = NotificationCompat.VISIBILITY_PRIVATE,
) {
	val notificationManager = NotificationManagerCompat.from(context)

	val pendingIntent = PendingIntent.getActivity(
		context,
		intentRequestCode,
		intent,
		PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
	)

	val builder = NotificationCompat.Builder(context, channelId)
		.setContentTitle(title)
		.setContentText(body)
		.setSmallIcon(R.drawable.ic_logo) // TODO Нужно переделать иконку
		.setStyle(NotificationCompat.BigTextStyle().bigText(body))
		.setPriority(priority)
		.setCategory(category)
		.setVisibility(visibility)
		.setContentIntent(pendingIntent)
		.setAutoCancel(true)
		.setGroup(groupKey)
		.build()

	notificationManager.notify(id, builder)
}

/**
 * Функция для обновления состояния группового уведомления.
 * Если оно не создано и количество уведомлений > [NOTIFICATION_THRESHOLD], то оно создается.
 * Иначе отменяется
 *
 * @param channelId Идентификатор канала уведомлений.
 * @param context Контекст для создания уведомления.
 * @param groupKey Общий ключ для группы уведомлений.
 * @param summaryNotificationText Общее название для группы уведомлений.
 * @param category Категория уведомления. Значение берется из [NotificationCompat]
 * @param priority Приоритет уведомления. Значение по умолчанию - [NotificationCompat.PRIORITY_DEFAULT].
 * @param visibility Видимость уведомления. Значение по умолчанию - [NotificationCompat.VISIBILITY_PRIVATE].
 */
@SuppressLint("MissingPermission")
fun updateSummaryNotification(
	channelId: String,
	context: Context,
	groupKey: String,
	category: String,
	summaryNotificationText: String,
	priority: Int = NotificationCompat.PRIORITY_DEFAULT,
	visibility: Int = NotificationCompat.VISIBILITY_PRIVATE,
) {
	val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: error("Error receiving NotificationManager")
	val activeNotifications = notificationManager.activeNotifications.map { it.id to it.notification }

	if (activeNotifications.size > NOTIFICATION_THRESHOLD) {
		val summaryBuilder = NotificationCompat.Builder(context, channelId)
			.setContentTitle(summaryNotificationText)
			.setPriority(priority)
			.setCategory(category)
			.setSmallIcon(R.drawable.ic_logo)
			.setVisibility(visibility)
			.setGroup(groupKey)
			.setGroupSummary(true)
			.build()

		notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryBuilder)
	} else {
		notificationManager.cancel(SUMMARY_NOTIFICATION_ID)
	}
}