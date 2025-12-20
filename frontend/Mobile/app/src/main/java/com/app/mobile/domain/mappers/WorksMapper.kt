package com.app.mobile.domain.mappers

import com.app.mobile.data.database.entity.WorkEntity
import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.presentation.models.hive.WorkUi

fun WorkEntity.toDomain() = WorkDomain(
    id = this.id,
    hiveId = this.hiveId,
    title = this.title,
    text = this.text,
    dateTime = this.dateTime
)

fun WorkDomain.toUiModel() = WorkUi(
    id = this.id,
    hiveId = this.hiveId,
    title = this.title,
    text = this.text,
    dateTime = localDateTimeFormatter(this.dateTime)
)

fun WorkDomain.toEntity() = WorkEntity(
    id = this.id,
    hiveId = this.hiveId,
    title = this.title,
    text = this.text,
    dateTime = this.dateTime
)

fun WorkUi.toDomain() = WorkDomain(
    id = this.id,
    hiveId = this.hiveId,
    title = this.title,
    text = this.text,
    dateTime = localDateTimeParser(this.dateTime)
)