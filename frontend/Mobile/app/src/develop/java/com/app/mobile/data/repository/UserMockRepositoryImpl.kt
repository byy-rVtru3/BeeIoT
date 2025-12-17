package com.app.mobile.data.repository

import android.content.Context
import com.app.mobile.data.mock.MockDataProvider
import com.app.mobile.domain.models.UserDomain
import com.app.mobile.domain.repository.UserLocalRepository

class UserMockRepositoryImpl(private val context: Context) : UserLocalRepository {

    private val mockUsers = mutableMapOf<Int, UserDomain>()

    init {
        val mockUser = MockDataProvider.getUserDomain(context)
        mockUsers[MockDataProvider.getUser(context).id] = mockUser
    }

    override suspend fun addUser(userDomain: UserDomain) {
        val userId = MockDataProvider.getUser(context).id
        mockUsers[userId] = userDomain
    }

    override suspend fun updateUser(userDomain: UserDomain) {
        val userId = MockDataProvider.getUser(context).id
        mockUsers[userId] = userDomain
    }

    override suspend fun deleteUser(userId: Int) {
        mockUsers.remove(userId)
    }

    override suspend fun getUserById(userId: Int): UserDomain? {
        return mockUsers[userId] ?: MockDataProvider.getUserDomain(context)
    }

    override suspend fun addTokenToUser(email: String, token: String): Int? {
        val mockUserData = MockDataProvider.getUser(context)
        mockUsers[mockUserData.id] = UserDomain(
            name = mockUserData.name,
            email = mockUserData.email,
            password = mockUserData.password,
            jwtToken = token
        )
        return mockUserData.id
    }

    override suspend fun updateTokenToUser(userId: Int, token: String) {
        mockUsers[userId]?.let {
            mockUsers[userId] = it.copy(jwtToken = token)
        }
    }

    override suspend fun getUserToken(userId: Int): String? {
        return mockUsers[userId]?.jwtToken ?: MockDataProvider.getUser(context).jwtToken
    }

    override suspend fun deleteTokenFromUser(userId: Int) {
        mockUsers[userId]?.let {
            mockUsers[userId] = it.copy(jwtToken = null)
        }
    }
}

