package com.app.mobile.domain.mappers

import com.app.mobile.data.database.entity.UserEntity
import com.app.mobile.domain.models.UserDomain

fun UserEntity.toDomain() = UserDomain(
    name = this.name,
    email = this.email,
    jwtToken = this.jwtToken
)