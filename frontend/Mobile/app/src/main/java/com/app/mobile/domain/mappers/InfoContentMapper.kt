package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.info.InstructionSectionDomain
import com.app.mobile.presentation.models.info.HowToSectionUi

fun InstructionSectionDomain.toUiModel() = HowToSectionUi(
    title = title,
    body = body,
    showStepNumbers = showStepNumbers
)
