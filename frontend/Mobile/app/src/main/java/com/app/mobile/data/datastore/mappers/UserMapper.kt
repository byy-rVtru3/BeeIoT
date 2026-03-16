package com.app.mobile.data.datastore.mappers

import com.app.mobile.data.datastore.entity.UserEntity
import com.app.mobile.domain.models.UserDomain


fun UserDomain.toEntity() = UserEntity(
    name = this.name,
    email = this.email,
    password = this.password
)