package com.realworld.android.petsave.common.data.cache

import com.realworld.android.petsave.common.data.cache.model.cachedorganization.CachedOrganization

interface Cache {
  fun storeOrganizations(organizations: List<CachedOrganization>)
}