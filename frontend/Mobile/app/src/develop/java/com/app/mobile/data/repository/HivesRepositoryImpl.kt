package com.app.mobile.data.repository

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.HiveResult
import com.app.mobile.domain.repository.datasource.HivesDataSource
import kotlinx.coroutines.delay

class HivesDataSourceImpl : HivesDataSource {

	private data class MockHive(
		val preview: HiveDomainPreview,
		val active: Boolean = true
	)

	private val mockHives = mutableListOf(
		MockHive(HiveDomainPreview(name = "Улей-1", sensor = "sensor-001", hub = "hub-001", queen = "Матка-1")),
		MockHive(HiveDomainPreview(name = "Улей-2", sensor = null, hub = null, queen = null)),
		MockHive(HiveDomainPreview(name = "Улей-3", sensor = "sensor-003", hub = null, queen = "Матка-2"))
	)

	override suspend fun getHives(active: Boolean?): ApiResult<List<HiveDomainPreview>> {
		delay(100)
		val filtered = if (active != null) mockHives.filter { it.active == active } else mockHives
		return ApiResult.Success(filtered.map { it.preview })
	}

	override suspend fun getHive(name: String): ApiResult<HiveResult> {
		delay(100)
		val hive = mockHives.find { it.preview.name == name }
		return if (hive != null) {
			ApiResult.Success(
				HiveResult(
					name = hive.preview.name,
					hub = hive.preview.hub,
					queen = hive.preview.queen,
					active = hive.active
				)
			)
		} else {
			ApiResult.HttpError(404, "Улей не найден")
		}
	}

	override suspend fun createHive(name: String): ApiResult<Unit> {
		delay(100)
		mockHives.add(MockHive(HiveDomainPreview(name = name)))
		return ApiResult.Success(Unit)
	}

	override suspend fun updateHive(oldName: String, newName: String?, active: Boolean?): ApiResult<Unit> {
		delay(100)
		val index = mockHives.indexOfFirst { it.preview.name == oldName }
		if (index != -1) {
			val current = mockHives[index]
			mockHives[index] = current.copy(
				preview = current.preview.copy(name = newName ?: current.preview.name),
				active = active ?: current.active
			)
		}
		return ApiResult.Success(Unit)
	}

	override suspend fun deleteHive(name: String): ApiResult<Unit> {
		delay(100)
		mockHives.removeAll { it.preview.name == name }
		return ApiResult.Success(Unit)
	}

	override suspend fun linkHubToHive(hiveName: String, hubId: String): ApiResult<Unit> {
		delay(100)
		val index = mockHives.indexOfFirst { it.preview.name == hiveName }
		if (index != -1) {
			mockHives[index] = mockHives[index].copy(
				preview = mockHives[index].preview.copy(hub = hubId)
			)
		}
		return ApiResult.Success(Unit)
	}

	override suspend fun linkQueenToHive(hiveName: String, queenName: String): ApiResult<Unit> {
		delay(100)
		val index = mockHives.indexOfFirst { it.preview.name == hiveName }
		if (index != -1) {
			mockHives[index] = mockHives[index].copy(
				preview = mockHives[index].preview.copy(queen = queenName)
			)
		}
		return ApiResult.Success(Unit)
	}
}
