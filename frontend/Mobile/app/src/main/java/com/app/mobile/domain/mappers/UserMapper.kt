package com.app.mobile.domain.mappers

import com.app.mobile.data.database.entity.UserEntity
import com.app.mobile.domain.models.UserDomain
import com.app.mobile.presentation.models.account.UserInfoModel

fun UserEntity.toDomain() = UserDomain(
    name = this.name,
    email = this.email,
    password = this.password,
    jwtToken = this.jwtToken
)

fun UserDomain.toPresentation() = UserInfoModel(
    name = this.name,
    email = this.email,
    password = this.password
)