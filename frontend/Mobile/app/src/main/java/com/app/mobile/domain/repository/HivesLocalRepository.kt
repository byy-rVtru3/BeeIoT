package com.app.mobile.domain.repository

import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.HiveEditorDomain

interface HivesLocalRepository {

    suspend fun getHives(): List<HiveDomainPreview>

    suspend fun getHive(hiveId: String): HiveDomain?

    suspend fun getHivePreview(hiveId: String): HiveDomainPreview?

    suspend fun saveHive(hive: HiveEditorDomain)
}