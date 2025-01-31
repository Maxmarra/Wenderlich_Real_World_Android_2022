/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.realworld.android.petsave.core

import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.realworld.android.petsave.R
import com.realworld.android.petsave.animalsnearyou.presentation.AnimalsNearYouFragmentViewModel
import com.realworld.android.petsave.core.domain.model.user.User
import com.realworld.android.petsave.core.domain.repositories.UserRepository
import com.realworld.android.petsave.core.utils.Encryption.Companion.createLoginPassword
import com.realworld.android.petsave.core.utils.Encryption.Companion.decryptPassword
import com.realworld.android.petsave.core.utils.Encryption.Companion.generateSecretKey
import com.realworld.android.petsave.core.utils.FileConstants
import com.realworld.android.petsave.core.utils.PreferencesHelper
import com.realworld.android.petsave.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.util.concurrent.Executors

/**
 * Main Screen
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private val binding get() = _binding!!
  private var _binding: ActivityMainBinding? = null

  private val navController by lazy { findNavController(R.id.nav_host_fragment) }
  private val appBarConfiguration by lazy { AppBarConfiguration(topLevelDestinationIds = setOf(
      R.id.animalsNearYou, R.id.search, R.id.report)) }

  private val viewModel: AnimalsNearYouFragmentViewModel by viewModels()
  private var isSignedUp = false
  private var workingFile: File? = null

  private lateinit var biometricPrompt: BiometricPrompt
  private lateinit var promptInfo: BiometricPrompt.PromptInfo

  override fun onCreate(savedInstanceState: Bundle?) {
    // Switch to AppTheme for displaying the activity
    setTheme(R.style.AppTheme)

    super.onCreate(savedInstanceState)
    window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

    _binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupFragment()
    setupActionBar()
    setupBottomNav()
    setupWorkingFiles()
    updateLoggedInState()
  }

  override fun onSupportNavigateUp(): Boolean {
    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
  }

  private fun setupFragment() {
    val fragmentManager = supportFragmentManager
    val fragment = fragmentManager.findFragmentById(R.id.nav_host_fragment)
    fragment?.let {
      fragmentManager.beginTransaction()
        .hide(it)
        .commit()
    }
  }

  private fun setupActionBar() {
    setSupportActionBar(binding.toolbar)
    setupActionBarWithNavController(navController, appBarConfiguration)
  }

  private fun setupBottomNav() {
    binding.bottomNavigation.visibility = View.GONE
    binding.bottomNavigation.setupWithNavController(navController)
  }

  private fun setupWorkingFiles() {
    workingFile = File(filesDir.absolutePath + File.separator +
        FileConstants.DATA_SOURCE_FILE_NAME)
  }

  fun loginPressed(view: View) {
    val biometricManager = BiometricManager.from(this)
    when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
      BiometricManager.BIOMETRIC_SUCCESS ->
        displayLogin(view, false)
      BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
        displayLogin(view, true)
      BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
        toast("Biometric features are currently unavailable.")
      BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
        toast("Please associate a biometric credential with your account.")
      else ->
        toast("An unknown error occurred. Please check your Biometric settings")
    }
  }

  private fun updateLoggedInState() {
    val fileExists = workingFile?.exists() ?: false
    if (fileExists) {
      isSignedUp = true
      binding.loginButton.text = getString(R.string.login)
      binding.loginEmail.visibility = View.INVISIBLE
    } else {
      binding.loginButton.text = getString(R.string.signup)
    }
  }

  private fun displayLogin(view: View, fallback: Boolean) {
    val executor = Executors.newSingleThreadExecutor()
    biometricPrompt = BiometricPrompt(this, executor, // 1
        object : BiometricPrompt.AuthenticationCallback() {
          override fun onAuthenticationError(errorCode: Int,
              errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            runOnUiThread {
              toast("Authentication error: $errString")
            }
          }

          override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            runOnUiThread {
              toast("Authentication failed")
            }
          }

          override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { // 2
            super.onAuthenticationSucceeded(result)

            runOnUiThread {
              toast("Authentication succeeded!")
              if (!isSignedUp) {
                generateSecretKey() // 3
              }
              performLoginOperation(view)
            }
          }
        })

    if (fallback) {
      promptInfo = BiometricPrompt.PromptInfo.Builder()
          .setTitle("Biometric login for my app")
          .setSubtitle("Log in using your biometric credential")
          // Cannot call setNegativeButtonText() and
          // setDeviceCredentialAllowed() at the same time.
          // .setNegativeButtonText("Use account password")
          .setAllowedAuthenticators(DEVICE_CREDENTIAL) // 4
          .build()
    } else {
      promptInfo = BiometricPrompt.PromptInfo.Builder()
          .setTitle("Biometric login for my app")
          .setSubtitle("Log in using your biometric credential")
          .setNegativeButtonText("Use account password")
          .build()
    }
    biometricPrompt.authenticate(promptInfo)
  }

  private fun performLoginOperation(view: View) {
    var success = false

    workingFile?.let {
      //Check if already signed up
      if (isSignedUp) {
        val fileInputStream = FileInputStream(it)
        val objectInputStream = ObjectInputStream(fileInputStream)
        val list = objectInputStream.readObject() as ArrayList<User>
        val firstUser = list.first() as? User
        if (firstUser is User) { //2
          val password = decryptPassword(this,
              Base64.decode(firstUser.password, Base64.NO_WRAP))
          if (password.isNotEmpty()) {
            //Send password to authenticate with server etc
            success = true
          }
        }

        if (success) {
          toast("Last login: ${PreferencesHelper.lastLoggedIn(this)}")
        } else {
          toast("Please check your credentials and try again.")
        }

        objectInputStream.close()
        fileInputStream.close()
      } else {
        val encryptedInfo = createLoginPassword(this)
        UserRepository.createDataSource(applicationContext, it, encryptedInfo)
        success = true
      }
    }

    if (success) {
      PreferencesHelper.saveLastLoggedInTime(this)
      viewModel.setIsLoggedIn(true)

      //Show fragment
      binding.loginEmail.visibility = View.GONE
      binding.loginButton.visibility = View.GONE
      val fragmentManager = supportFragmentManager
      val fragment = fragmentManager.findFragmentById(R.id.nav_host_fragment)
      fragment?.let{
        fragmentManager.beginTransaction()
          .show(it)
          .commit()
      }
      fragmentManager.executePendingTransactions()
      binding.bottomNavigation.visibility = View.VISIBLE
    }
  }

  override fun onPause() {
    cacheDir.deleteRecursively()
    externalCacheDir?.deleteRecursively()

    super.onPause()
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }

  private fun toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }
}
