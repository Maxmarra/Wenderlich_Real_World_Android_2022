package com.realworld.android.petsave.common.data.di

import android.content.Context
import androidx.room.Room
import com.realworld.android.petsave.common.data.cache.Cache
import com.realworld.android.petsave.common.data.cache.PetSaveDatabase
import com.realworld.android.petsave.common.data.cache.RoomCache
import com.realworld.android.petsave.common.data.cache.daos.OrganizationsDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CacheModule {

  @Binds
  abstract fun bindCache(cache: RoomCache): Cache

  companion object {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): PetSaveDatabase {
      return Room.databaseBuilder(context, PetSaveDatabase::class.java, "petsave.db")
          .build()
    }

    @Provides
    fun provideOrganizationsDao(petSaveDatabase: PetSaveDatabase): OrganizationsDao =
        petSaveDatabase.organizationsDao()
  }
}