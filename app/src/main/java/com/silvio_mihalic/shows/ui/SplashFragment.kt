package com.silvio_mihalic.shows.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.silvio_mihalic.shows.R
import com.silvio_mihalic.shows.databinding.FragmentSplashBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SplashFragment : DaggerFragment() {

    companion object {
        private const val USER_LOGGED_IN = "loggedIn"
        private const val LOGO_ANIMATION_START_DISTANCE = -1000f
        private const val LOGO_ANIMATION_DURATION = 1000L
        private const val LOGO_ANIMATION_TRANSLATION = 0f
        private const val TITLE_ANIMATION_START_SCALE = 0f
        private const val TITLE_ANIMATION_END_SCALE = 1f
        private const val TITLE_ANIMATION_DURATION = 500L
        private const val TITLE_INTERPOLATOR_TENSION = 3f
        private const val SLEEP_DURATION = 2000L
    }

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var prefs: SharedPreferences

    private var loggedIn: Boolean? = null
    private var appOpened = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeAppOnBackPressed()
    }

    /**
     * If app is paused on splash screen, next time the app is opened, onResume will be
     * called, which will activate animations and navigation to login/shows fragment
     * If these methods are called in onViewCreated, when this screen is paused and user returns to
     * the app, the app will be stuck on SplashFragment
     */
    override fun onResume() {
        super.onResume()
        setStartingPointForTitleAnimation()
        getSharedPrefs()
        animateLogo()
    }

    /**
     * Get shared preferences
     */
    private fun getSharedPrefs() {
        loggedIn = prefs?.getBoolean(USER_LOGGED_IN, false)
    }

    /**
     * Run logo animation, call animateTitle function after logo animation is over
     */
    private fun animateLogo() = binding.splashTriangle.apply {
        translationY = LOGO_ANIMATION_START_DISTANCE
        animate()
            .translationY(LOGO_ANIMATION_TRANSLATION)
            .setDuration(LOGO_ANIMATION_DURATION)
            .setInterpolator(BounceInterpolator())
            .withEndAction { animateTitle() }
            .start()
    }

    /**
     * Hide title at the beginning and set point from which title will expand
     */
    private fun setStartingPointForTitleAnimation() = binding.splashTitle.apply {
        scaleX = TITLE_ANIMATION_START_SCALE
        scaleY = TITLE_ANIMATION_START_SCALE
    }

    /**
     * Run title animation, after it's done, wait 2 seconds and call checkIsUserLoggedIn function
     */
    private fun animateTitle() = binding.splashTitle.apply {
        animate()
            .scaleX(TITLE_ANIMATION_END_SCALE)
            .scaleY(TITLE_ANIMATION_END_SCALE)
            .setDuration(TITLE_ANIMATION_DURATION)
            .setInterpolator(OvershootInterpolator(TITLE_INTERPOLATOR_TENSION))
            .withEndAction {
                Handler(Looper.getMainLooper()).postDelayed({
                    checkIsUserLoggedIn()
                }, SLEEP_DURATION)
            }
            .start()
    }

    /**
     * Check if user has checked Remember me check box before
     * logging in successfully, if true go directly to ShowsFragment
     */
    private fun checkIsUserLoggedIn() {
        if (appOpened) {
            if (loggedIn == true) {
                findNavController().navigate(R.id.action_splash_to_shows)
            } else {
                findNavController().navigate(R.id.action_splash_to_login)
            }
        }
    }

    private fun closeAppOnBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(this) {
            appOpened = false
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
