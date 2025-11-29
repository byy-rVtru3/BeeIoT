package com.app.mobile.di

import androidx.room.Room
import com.app.mobile.data.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    single { get<AppDatabase>().userDao() }
}