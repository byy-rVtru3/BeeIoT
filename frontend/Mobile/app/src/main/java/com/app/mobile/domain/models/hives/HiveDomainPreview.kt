package com.app.mobile.domain.models.hives

data class HiveDomainPreview(
    val name: String,
    val sensor: String? = null,
    val hub: String? = null,
    val queen: String? = null
)
