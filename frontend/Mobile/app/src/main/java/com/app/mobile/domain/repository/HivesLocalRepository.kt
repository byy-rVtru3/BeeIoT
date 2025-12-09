package com.app.mobile.domain.repository

import com.app.mobile.domain.models.hives.HiveDomain
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.QueenDomain

interface HivesLocalRepository {

    suspend fun getHives(): List<HiveDomainPreview>

    suspend fun getHive(hiveId: Int): HiveDomain?

    suspend fun getQueenByHiveId(hiveId: Int): QueenDomain?

    suspend fun getHivePreview(hiveId: Int): HiveDomainPreview?
}