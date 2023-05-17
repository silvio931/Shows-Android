package com.silvio_mihalic.shows.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.silvio_mihalic.shows.R
import com.silvio_mihalic.shows.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val LAST_CLICKED_SHOW_ID = "lastClickedShowId"
        private const val RECYCLER_SCROLL_DEFAULT_POSITION = -1
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ShowsSilvio_Mihalic)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        resetLastClickedShowIdSharedPrefs()
    }

    private fun resetLastClickedShowIdSharedPrefs() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        if (sharedPref != null) {
            with(sharedPref.edit()) {
                putInt(LAST_CLICKED_SHOW_ID, RECYCLER_SCROLL_DEFAULT_POSITION)
                apply()
            }
        }
    }
}
