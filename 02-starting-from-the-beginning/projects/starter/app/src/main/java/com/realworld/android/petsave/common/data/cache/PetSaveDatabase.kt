package com.realworld.android.petsave.common.data.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import com.realworld.android.petsave.common.data.cache.daos.OrganizationsDao
import com.realworld.android.petsave.common.data.cache.model.cachedanimal.*
import com.realworld.android.petsave.common.data.cache.model.cachedorganization.CachedOrganization

@Database(
    entities = [
      CachedPhoto::class,
      CachedVideo::class,
      CachedTag::class,
      CachedAnimalTagCrossRef::class,
      CachedOrganization::class
    ],
    version = 1
)
abstract class PetSaveDatabase : RoomDatabase() {
  abstract fun organizationsDao(): OrganizationsDao
}