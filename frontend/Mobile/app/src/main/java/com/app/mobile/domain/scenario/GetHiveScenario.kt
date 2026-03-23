package com.app.mobile.domain.scenario

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.usecase.hives.hive.GetHiveUseCase
import com.app.mobile.domain.usecase.hives.hub.GetHubWithSensorsUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueenUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetHiveScenario(
	private val getHiveUseCase: GetHiveUseCase,
	private val getHubWithSensorsUseCase: GetHubWithSensorsUseCase,
	private val getQueenUseCase: GetQueenUseCase
) {

	suspend operator fun invoke(hiveName: String): ApiResult<HiveDomain> {
		val hiveResult = getHiveUseCase(hiveName)

		if (hiveResult !is ApiResult.Success) {
			@Suppress("UNCHECKED_CAST")
			return hiveResult as ApiResult<HiveDomain>
		}

		val hive = hiveResult.data

		return coroutineScope {
			val hubDeferred = hive.hub?.let { async { getHubWithSensorsUseCase(it) } }
			val queenDeferred = hive.queen?.let { async { getQueenUseCase(it) } }

			val hub = (hubDeferred?.await() as? ApiResult.Success)?.data
			val queen = (queenDeferred?.await() as? ApiResult.Success)?.data

			ApiResult.Success(
				HiveDomain(
					name = hive.name,
					hub = hub,
					queen = queen,
					active = hive.active ?: true
				)
			)
		}
	}
}