package com.app.mobile.data.datastore.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.app.mobile.data.datastore.entity.HubEntity

@Dao
interface HubDao {
    @Query("UPDATE hubs SET hiveId = :hiveId WHERE id = :hubId")
    suspend fun addHiveToHub(hubId: String, hiveId: String)

    @Query("SELECT * FROM hubs")
    suspend fun getHubs(): List<HubEntity>

    @Query("SELECT * FROM hubs WHERE id = :hubId")
    suspend fun getHubById(hubId: String): HubEntity?

    @Upsert
    suspend fun saveHub(hub: HubEntity)
}