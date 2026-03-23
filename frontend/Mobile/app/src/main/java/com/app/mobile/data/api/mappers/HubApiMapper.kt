package com.app.mobile.data.api.mappers

import com.app.mobile.data.api.models.hub.HubListItemDto
import com.app.mobile.domain.models.hives.HubDomain

fun HubListItemDto.toDomain() = HubDomain(
    id = this.id,
    name = this.name
)
