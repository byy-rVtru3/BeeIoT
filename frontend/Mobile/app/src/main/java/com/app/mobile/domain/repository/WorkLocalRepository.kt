package com.app.mobile.domain.repository

import com.app.mobile.domain.models.hives.WorkDomain

interface WorkLocalRepository {
    suspend fun getWork(workId: String): WorkDomain?

    suspend fun getWorks(hiveId: String): List<WorkDomain>

    suspend fun saveWork(work: WorkDomain)
}