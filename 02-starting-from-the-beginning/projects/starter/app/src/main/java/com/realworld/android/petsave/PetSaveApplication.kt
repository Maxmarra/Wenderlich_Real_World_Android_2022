package com.realworld.android.petsave

import android.app.Application
import com.realworld.android.logging.Logger

class PetSaveApplication: Application() {

  // initiate analytics, crashlytics, etc

  override fun onCreate() {
    super.onCreate()

    initLogger()
  }

  private fun initLogger() {
    Logger.init()
  }
}