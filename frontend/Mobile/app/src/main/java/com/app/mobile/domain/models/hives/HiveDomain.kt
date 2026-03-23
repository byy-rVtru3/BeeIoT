package com.app.mobile.domain.models.hives

import com.app.mobile.domain.models.hives.queen.QueenDomain

data class HiveDomain(
    val name: String,
    val hub: HubDomain?,
    val queen: QueenDomain?,
    val active: Boolean
)
