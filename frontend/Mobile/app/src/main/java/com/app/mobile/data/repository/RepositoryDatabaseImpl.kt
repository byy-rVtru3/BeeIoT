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

    override suspend fun deleteUser(email: String) = userDao.deleteUserByEmail(email)

    override suspend fun getUserByEmail(email: String): UserDomain? =
        userDao.getUserByEmail(email)?.toDomain()

    override suspend fun addTokenToUser(email: String, token: String) =
        userDao.addTokenToUser(email, token)
}