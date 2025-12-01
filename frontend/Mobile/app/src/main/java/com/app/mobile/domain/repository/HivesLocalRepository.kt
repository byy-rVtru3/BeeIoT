package com.app.mobile.domain.repository

import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.models.hives.HiveDomainPreview

interface HivesLocalRepository {

    suspend fun getHives(): List<HiveDomainPreview>

    suspend fun getHive(hiveId: Int): HiveDomain?
}