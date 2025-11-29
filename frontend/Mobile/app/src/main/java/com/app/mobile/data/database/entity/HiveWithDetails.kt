package com.app.mobile.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class HiveWithDetails(

    @Embedded val hive: HiveEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "hiveId"
    )
    val connectedHub: HubEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "hiveId"
    )
    val notifications: List<NotificationEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "hiveId"
    )
    val works: List<WorkEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "hiveId"
    )
    val queen: QueenEntity

)
