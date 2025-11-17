package com.app.mobile.domain.repository

import com.app.mobile.domain.models.UserDomain

interface RepositoryDatabase {

    suspend fun addUser(userDomain: UserDomain)

    suspend fun updateUser(userDomain: UserDomain)

    suspend fun deleteUser(email: String)

    suspend fun getUserByEmail(email: String): UserDomain?

    suspend fun addTokenToUser(email: String, token: String)
}