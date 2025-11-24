package com.app.mobile.data.repository

import com.app.mobile.data.database.dao.UserDao
import com.app.mobile.data.database.mappers.toEntity
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.models.UserDomain
import com.app.mobile.domain.repository.RepositoryDatabase

class RepositoryDatabaseImpl(private val userDao: UserDao) : RepositoryDatabase {

    override suspend fun addUser(userDomain: UserDomain) = userDao.addUser(userDomain.toEntity())

    override suspend fun updateUser(userDomain: UserDomain) =
        userDao.updateUser(userDomain.toEntity())

    override suspend fun deleteUser(userId: Int) = userDao.deleteUserById(userId)

    override suspend fun getUserById(userId: Int): UserDomain? =
        userDao.getUserById(userId)?.toDomain()

    override suspend fun addTokenToUser(email: String, token: String): Int? {
        val id = userDao.getUserIdByEmail(email) ?: return null
        userDao.addTokenToUser(email, token)
        return id
    }
}