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

    override suspend fun getUserIdByEmail(email: String) = MockDataProvider.getUser(context).id
}

