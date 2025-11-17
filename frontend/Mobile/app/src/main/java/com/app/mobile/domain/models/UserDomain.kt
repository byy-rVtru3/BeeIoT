package com.app.mobile.domain.models

data class UserDomain(
    val name: String,
    val email: String,
    val jwtToken: String?
)
