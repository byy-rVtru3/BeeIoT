package com.app.mobile.data.api.mappers

import com.app.mobile.data.api.models.task.TaskItemDto
import com.app.mobile.domain.models.hives.WorkDomain
import java.time.OffsetDateTime

fun TaskItemDto.toDomain() = WorkDomain(
    id = this.id,
    hiveId = this.hiveName,
    title = this.title,
    text = this.description ?: "",
    dateTime = OffsetDateTime.parse(this.createdAt).toLocalDateTime()
)
