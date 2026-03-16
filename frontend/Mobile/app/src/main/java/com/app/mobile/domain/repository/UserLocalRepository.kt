package com.app.mobile.domain.repository

import com.app.mobile.domain.models.UserDomain

interface UserLocalRepository {

    suspend fun addUser(userDomain: UserDomain)

    suspend fun updateUser(userDomain: UserDomain)

    suspend fun deleteUser(userId: Int)

    suspend fun getUserById(userId: Int): UserDomain?

    suspend fun getUserIdByEmail(email: String): Int?
}