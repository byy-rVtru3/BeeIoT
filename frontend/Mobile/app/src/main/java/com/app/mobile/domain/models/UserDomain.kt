package com.app.mobile.domain.models

data class UserDomain(
    val name: String,
    val email: String,
    val password: String,
    val jwtToken: String?
)
