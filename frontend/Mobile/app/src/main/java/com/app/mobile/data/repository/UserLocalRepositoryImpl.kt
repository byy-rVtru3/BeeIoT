package com.app.mobile.data.repository

import com.app.mobile.data.datastore.dao.UserDao
import com.app.mobile.data.datastore.mappers.toEntity
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.models.UserDomain
import com.app.mobile.domain.repository.UserLocalRepository

class UserLocalRepositoryImpl(private val userDao: UserDao) : UserLocalRepository {

    override suspend fun addUser(userDomain: UserDomain) = userDao.addUser(userDomain.toEntity())

    override suspend fun updateUser(userDomain: UserDomain) =
        userDao.updateUser(userDomain.toEntity())

    override suspend fun deleteUser(userId: Int) = userDao.deleteUserById(userId)

    override suspend fun getUserById(userId: Int): UserDomain? =
        userDao.getUserById(userId)?.toDomain()

    override suspend fun getUserIdByEmail(email: String) = userDao.getUserIdByEmail(email)

}

