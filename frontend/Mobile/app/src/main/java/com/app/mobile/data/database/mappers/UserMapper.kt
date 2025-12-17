package com.app.mobile.data.database.mappers

import com.app.mobile.data.database.entity.UserEntity
import com.app.mobile.domain.models.UserDomain


fun UserDomain.toEntity() = UserEntity(
    name = this.name,
    email = this.email,
    password = this.password,
    jwtToken = this.jwtToken
)