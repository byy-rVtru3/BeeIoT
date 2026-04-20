package com.app.mobile.data.content

import android.content.Context
import com.app.mobile.R
import com.app.mobile.domain.models.info.InfoContentDomain
import com.app.mobile.domain.models.info.InstructionSectionDomain

class InfoContentDefaultsProvider(
    private val context: Context
) {

    fun getDefaultContent(): InfoContentDomain {
        return InfoContentDomain(
            aboutText = context.getString(R.string.app_info),
            howToSections = listOf(
                InstructionSectionDomain(
                    title = context.getString(R.string.how_to_use_section_title_1),
                    body = context.getString(R.string.how_to_use_section_body_1),
                    showStepNumbers = true
                ),
                InstructionSectionDomain(
                    title = context.getString(R.string.how_to_use_section_title_2),
                    body = context.getString(R.string.how_to_use_section_body_2),
                    showStepNumbers = false
                ),
                InstructionSectionDomain(
                    title = context.getString(R.string.how_to_use_section_title_3),
                    body = context.getString(R.string.how_to_use_section_body_3),
                    showStepNumbers = true
                )
            )
        )
    }
}
