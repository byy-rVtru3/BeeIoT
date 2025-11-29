package com.app.mobile.domain.mappers

import com.app.mobile.data.database.entity.WorkEntity
import com.app.mobile.domain.models.hives.WorkDomain

fun WorkEntity.toDomain() = WorkDomain(
    id = this.id,
    hiveId = this.hiveId,
    title = this.title,
    text = this.text,
    dateTime = this.dateTime
)