package com.app.mobile.presentation.ui.screens.hub.details.models

data class HubActions(
	val onEditClick: () -> Unit,
	val onDeleteClick: () -> Unit,
	val onTemperatureClick: () -> Unit,
	val onNoiseClick: () -> Unit,
	val onWeightClick: () -> Unit
)
