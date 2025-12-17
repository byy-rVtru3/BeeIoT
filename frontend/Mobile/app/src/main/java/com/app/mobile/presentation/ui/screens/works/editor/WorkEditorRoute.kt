package com.app.mobile.presentation.ui.screens.works.editor

import kotlinx.serialization.Serializable

@Serializable
data class WorkEditorRoute(val workId: String?, val hiveId: String)