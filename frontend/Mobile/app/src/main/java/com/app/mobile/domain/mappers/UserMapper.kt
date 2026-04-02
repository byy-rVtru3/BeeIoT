package com.app.mobile.domain.mappers

import com.app.mobile.data.api.models.account.UserInfoApiModel
import com.app.mobile.data.datastore.entity.UserEntity
import com.app.mobile.domain.models.UserDomain
import com.app.mobile.presentation.models.account.UserInfoModel

fun UserEntity.toDomain() = UserDomain(
    name = this.name,
    email = this.email
)

fun UserInfoApiModel.toDomain() = UserDomain(
    name = this.name,
    email = this.email
)

fun UserDomain.toPresentation() = UserInfoModel(
    name = this.name,
    email = this.email
)