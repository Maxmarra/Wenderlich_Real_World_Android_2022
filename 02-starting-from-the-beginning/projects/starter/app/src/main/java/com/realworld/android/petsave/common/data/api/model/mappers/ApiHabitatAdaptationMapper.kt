package com.realworld.android.petsave.common.data.api.model.mappers

import com.realworld.android.petsave.common.data.api.model.ApiEnvironment
import com.realworld.android.petsave.common.domain.model.animal.details.HabitatAdaptation
import javax.inject.Inject

class ApiHabitatAdaptationMapper @Inject constructor():
    ApiMapper<ApiEnvironment?, HabitatAdaptation> {

  override fun mapToDomain(apiEntity: ApiEnvironment?): HabitatAdaptation {
    return HabitatAdaptation(
        goodWithChildren = apiEntity?.children ?: true,
        goodWithDogs = apiEntity?.dogs ?: true,
        goodWithCats = apiEntity?.cats ?: true
    )
  }
}
