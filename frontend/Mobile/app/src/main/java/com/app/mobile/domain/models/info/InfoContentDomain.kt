package com.app.mobile.domain.models.info

data class InfoContentDomain(
    val aboutText: String,
    val howToSections: List<InstructionSectionDomain>
)
