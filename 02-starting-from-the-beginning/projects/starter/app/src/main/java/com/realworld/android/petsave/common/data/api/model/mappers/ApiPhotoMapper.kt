package com.realworld.android.petsave.common.data.api.model.mappers

import com.realworld.android.petsave.common.data.api.model.ApiPhotoSizes
import com.realworld.android.petsave.common.domain.model.animal.Media
import javax.inject.Inject

class ApiPhotoMapper @Inject constructor(): ApiMapper<ApiPhotoSizes?, Media.Photo> {

  override fun mapToDomain(apiEntity: ApiPhotoSizes?): Media.Photo {
    return Media.Photo(
        medium = apiEntity?.medium.orEmpty(),
        full = apiEntity?.full.orEmpty()
    )
  }
}
