package com.app.mobile.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.mobile.data.database.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)

    @Query("UPDATE users SET jwtToken = :token WHERE email = :email")
    suspend fun addTokenToUser(email: String, token: String)

    @Query("SELECT id FROM users WHERE email = :email")
    suspend fun getUserIdByEmail(email: String): Int?

    @Query("SELECT jwtToken FROM users WHERE id = :userId")
    suspend fun getUserToken(userId: Int): String?

    @Query("UPDATE users SET jwtToken = :token WHERE id = :userId")
    suspend fun updateTokenToUser(userId: Int, token: String)

    @Query("UPDATE users SET jwtToken = null WHERE id = :userId")
    suspend fun deleteTokenFromUser(userId: Int)

}