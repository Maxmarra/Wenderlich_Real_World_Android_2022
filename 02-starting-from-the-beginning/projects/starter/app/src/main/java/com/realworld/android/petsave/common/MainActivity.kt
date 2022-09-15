package com.realworld.android.petsave.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.realworld.android.petsave.R
import com.realworld.android.petsave.databinding.ActivityMainBinding

/**
 * Main Screen
 */
class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    // Switch to AppTheme for displaying the activity
    setTheme(R.style.AppTheme)

    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
  }
}
