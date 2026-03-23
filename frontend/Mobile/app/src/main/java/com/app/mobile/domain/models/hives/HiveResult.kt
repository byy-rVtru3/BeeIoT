package com.app.mobile.domain.models.hives

data class HiveResult(
	val name: String,
	val hub: String? = null,
	val queen: String? = null,
	val active: Boolean? = null
)
