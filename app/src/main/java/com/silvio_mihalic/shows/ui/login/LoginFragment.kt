package com.silvio_mihalic.shows.ui.login

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.silvio_mihalic.shows.R
import com.silvio_mihalic.shows.databinding.FragmentLoginBinding
import com.silvio_mihalic.shows.di.qualifiers.ViewModelInjection
import com.silvio_mihalic.shows.utils.NetworkChecker
import com.silvio_mihalic.shows.utils.NetworkDetector
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class LoginFragment : DaggerFragment() {

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @Inject
    @ViewModelInjection
    lateinit var viewModel: LoginViewModel

    private lateinit var networkChecker: NetworkChecker
    private lateinit var networkDetector: NetworkDetector

    private lateinit var email: String
    private lateinit var password: String
    private var emailOK: Boolean = false
    private var passwordOK: Boolean = false

    private var cameFromRegistration: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getSharedPrefs()
        initEmailCheck()
        initPasswordCheck()
        initLoginButtonListener()
        initRegisterButtonListener()
        closeAppOnBackPressed()
        initLoginSuccessfulObserver()
        initNetworkDetectorObserver()
    }

    private fun initLoginSuccessfulObserver() {
        viewModel.loginResultLiveData
            .observe(this.viewLifecycleOwner) { isLoginSuccessful ->
                if (isLoginSuccessful) {
                    continueToShowsFragment()
                } else {
                    binding.errorMessage.isVisible = true
                }
            }
    }

    /**
     * If login was successful, set emailOK and passwordOK back to default false, save values
     * in shared prefs and navigate to shows fragment
     */
    private fun continueToShowsFragment() {
        emailOK = false
        passwordOK = false
        saveEmailAndUserLoggedInSharedPrefs()
        findNavController().navigate(R.id.action_login_to_shows)
    }

    /**
     * Check if there is internet connection when fragment is created using NetworkChecker
     * class, change noInternetMessage visibility using NetworkDetector class
     */
    private fun initNetworkDetectorObserver() {
        networkChecker = NetworkChecker()
        binding.noInternetMessage.isVisible = !networkChecker.internetIsConnected()
        networkDetector = NetworkDetector(context)
        networkDetector.listenForInternetConnectivity()
        networkDetector.networkAvailableLiveData.observe(viewLifecycleOwner, { networkAvailable ->
            binding.noInternetMessage.isVisible = !networkAvailable
        })
    }

    /**
     * Get shared preferences
     */
    private fun getSharedPrefs() {
        viewModel.cameFromRegistrationLiveData
            .observe(this.viewLifecycleOwner) {
                cameFromRegistration = it
                checkIfUserCameFromRegistration()
            }
    }

    /**
     * Check if user came from successful registration, if true change title fom Login to Registration
     * Successful! and hide Registration button
     */
    private fun checkIfUserCameFromRegistration() {
        if (cameFromRegistration == true) {
            binding.loginTitle.text = context?.getString(R.string.registration_successful_title)
            binding.registerButton.isVisible = false
            setRegistrationSuccessfulPrefs()
        }
    }

    /**
     * Change shared prefs REGISTRATION_SUCCESSFUL value back to false so LoginFragment title
     * isn't change in the future, only when user come back from RegistrationFragment after successful
     * registration
     */
    private fun setRegistrationSuccessfulPrefs() {
        viewModel.setRegistrationSuccessfulPrefs()
    }

    /**
     * Initialize listeners for email edit text
     */
    private fun initEmailCheck() = binding.emailInput.apply {
        editText?.addTextChangedListener {
            emailFormatCheck()
            buttonCheck()
        }

        editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                setEmailError()
            }
        }
    }

    /**
     * Initialize listeners for password edit text
     */
    private fun initPasswordCheck() = binding.passwordInput.apply {
        isEndIconVisible = false
        editText?.addTextChangedListener {
            passwordFormatCheck()
            buttonCheck()
        }

        editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                setPasswordError()
            }
        }
    }

    /**
     * Check if email matches given regex expression
     */
    private fun emailFormatCheck() = binding.emailInput.apply {
        error = null
        email = editText?.text.toString().trim()
        emailOK = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Sets error for email editText if email format is not correct
     */
    private fun setEmailError() = binding.emailInput.apply {
        val email = binding.emailInput.editText?.text.toString()
        if (!emailOK && email.isNotEmpty())
            error = context.getString(R.string.login_email_error)
        else {
            error = null
        }
    }

    /**
     * Check if password contains more than 5 characters,
     * if edit text is not empty, show/hide password symbol is shown
     */
    private fun passwordFormatCheck() = binding.passwordInput.apply {
        error = null
        password = editText?.text.toString()
        passwordOK = password.length >= MIN_PASSWORD_LENGTH
        isEndIconVisible = password.isNotEmpty()
    }

    /**
     * Sets error for password editText if password contains less than 6 characters
     */
    private fun setPasswordError() = binding.passwordInput.apply {
        val password = binding.passwordInput.editText?.text.toString()
        if (!passwordOK && password.isNotEmpty())
            error = context.getString(R.string.login_password_error)
        else {
            error = null
        }
    }

    /**
     * Enable login button if email and password are correct, disable if not
     */
    private fun buttonCheck() = binding.loginButton.apply {
        if (emailOK && passwordOK) {
            isEnabled = true
            setTextColor(getColor(requireActivity(), R.color.login_button_enabled_text_color))
        } else {
            isEnabled = false
            setTextColor(getColor(requireActivity(), R.color.login_button_disabled_text_color))
        }
    }

    /**
     * Initialize button listener for opening ShowsFragment
     */
    private fun initLoginButtonListener() {
        binding.loginButton.setOnClickListener {
            viewModel.login(email, password)
        }
    }

    /**
     * Initialize button listener for opening RegistrationFragment
     */
    private fun initRegisterButtonListener() {
        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_registration)
        }
    }

    /**
     * Save user email and flag USER_LOGGED_IN as true if user checked Remember me check box
     * in shared preferences
     */
    private fun saveEmailAndUserLoggedInSharedPrefs() {
        viewModel.saveEmailAndUserLoggedInSharedPrefs(email, binding.rememberMeCheckBox.isChecked)
    }

    /**
     * Close app on back pressed in LoginFragment
     */
    private fun closeAppOnBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(this) {
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
