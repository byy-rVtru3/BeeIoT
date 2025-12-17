package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.models.hives.HubDomain

class GetHubsUseCase {
    suspend operator fun invoke(): List<HubDomain> {
        // получение списка хабов
        return emptyList()
    }
}