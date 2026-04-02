package com.app.mobile.domain.usecase.account

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.session.manager.TokenManager
import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.domain.repository.RepositoryApi
import com.app.mobile.domain.repository.ThemeRepository
import com.app.mobile.domain.usecase.theme.GetThemeInitialUseCase
import com.app.mobile.domain.usecase.theme.ObserveThemeUseCase
import com.app.mobile.domain.usecase.theme.SetThemeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountAndThemeUseCasesTest {

    // region IsTokenExistUseCase

    @Test
    fun `IsTokenExistUseCase - returns true when token exists`() {
        val tokenManager = mockk<TokenManager>()
        every { tokenManager.getToken() } returns "valid_token"

        val result = IsTokenExistUseCase(tokenManager).invoke()

        assertTrue(result)
    }

    @Test
    fun `IsTokenExistUseCase - returns false when token is null`() {
        val tokenManager = mockk<TokenManager>()
        every { tokenManager.getToken() } returns null

        val result = IsTokenExistUseCase(tokenManager).invoke()

        assertFalse(result)
    }

    // endregion

    // region AuthorizationAccountUseCase

    @Test
    fun `AuthorizationAccountUseCase - success - saves token and returns result`() = runTest {
        val repositoryApi = mockk<RepositoryApi>()
        val tokenManager = mockk<TokenManager>()
        val dispatcher = StandardTestDispatcher(testScheduler)

        val authModel = AuthorizationModel(email = "user@test.com", password = "pass")
        coEvery { repositoryApi.authorizationAccount(authModel) } returns ApiResult.Success("jwt_token")
        every { tokenManager.saveToken("jwt_token") } returns Unit

        val result = AuthorizationAccountUseCase(
            repositoryApi, tokenManager, dispatcher
        ).invoke(authModel)

        assertTrue(result is ApiResult.Success)
        assertEquals("jwt_token", (result as ApiResult.Success<String>).data)
        verify { tokenManager.saveToken("jwt_token") }
    }

    @Test
    fun `AuthorizationAccountUseCase - failure - does not save token`() = runTest {
        val repositoryApi = mockk<RepositoryApi>()
        val tokenManager = mockk<TokenManager>()
        val dispatcher = StandardTestDispatcher(testScheduler)

        val authModel = AuthorizationModel(email = "user@test.com", password = "wrong")
        coEvery { repositoryApi.authorizationAccount(authModel) } returns ApiResult.HttpError(401)

        val result = AuthorizationAccountUseCase(
            repositoryApi, tokenManager, dispatcher
        ).invoke(authModel)

        assertTrue(result is ApiResult.HttpError)
        verify(exactly = 0) { tokenManager.saveToken(any()) }
    }

    // endregion

    // region Theme UseCases

    @Test
    fun `SetThemeUseCase - delegates to repository`() = runTest {
        val themeRepository = mockk<ThemeRepository>()
        coEvery { themeRepository.setDarkTheme(true) } returns Unit

        SetThemeUseCase(themeRepository).invoke(true)

        coVerify(exactly = 1) { themeRepository.setDarkTheme(true) }
    }

    @Test
    fun `GetThemeInitialUseCase - returns current theme value`() = runTest {
        val themeRepository = mockk<ThemeRepository>()
        coEvery { themeRepository.isDarkThemeSync() } returns true

        val result = GetThemeInitialUseCase(themeRepository).invoke()

        assertTrue(result)
    }

    @Test
    fun `ObserveThemeUseCase - returns Flow from repository`() = runTest {
        val themeRepository = mockk<ThemeRepository>()
        val expectedFlow = flowOf(false, true)
        every { themeRepository.isDarkTheme() } returns expectedFlow

        val flow = ObserveThemeUseCase(themeRepository).invoke()

        assertEquals(expectedFlow, flow)
    }

    // endregion
}
