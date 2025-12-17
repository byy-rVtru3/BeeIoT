package com.app.mobile.domain.repository

import com.app.mobile.domain.models.UserDomain

interface UserLocalRepository {

    suspend fun addUser(userDomain: UserDomain)

    suspend fun updateUser(userDomain: UserDomain)

    suspend fun deleteUser(userId: Int)

    suspend fun getUserById(userId: Int): UserDomain?

    suspend fun addTokenToUser(email: String, token: String): Int?

    suspend fun getUserToken(userId: Int): String?

    suspend fun updateTokenToUser(userId: Int, token: String)

    suspend fun deleteTokenFromUser(userId: Int)
}