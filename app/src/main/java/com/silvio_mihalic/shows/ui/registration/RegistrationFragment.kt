package com.silvio_mihalic.shows.ui.registration

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.silvio_mihalic.shows.R
import com.silvio_mihalic.shows.databinding.FragmentRegistrationBinding
import com.silvio_mihalic.shows.di.qualifiers.ViewModelInjection
import com.silvio_mihalic.shows.utils.NetworkChecker
import com.silvio_mihalic.shows.utils.NetworkDetector
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class RegistrationFragment : DaggerFragment() {

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    @Inject
    @ViewModelInjection
    lateinit var viewModel: RegistrationViewModel

    private lateinit var networkChecker: NetworkChecker
    private lateinit var networkDetector: NetworkDetector

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var repeatedPassword: String
    private var emailOK: Boolean = false
    private var passwordOK: Boolean = false
    private var repeatedPasswordOK: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initEmailCheck()
        initPasswordCheck()
        initRepeatedPasswordCheck()
        initButtonListener()
        initRegistrationSuccessfulObserver()
        initNetworkDetectorObserver()
    }

    /**
     * Initialize registration successful observer, if registration is successful, set shared
     * prefs and navigate to LoginFragment, else write a message on the screen
     */
    private fun initRegistrationSuccessfulObserver() {
        viewModel.registerResultLiveData
            .observe(this.viewLifecycleOwner) { isRegisterSuccessful ->
                if (isRegisterSuccessful) {
                    emailOK = false
                    passwordOK = false
                    repeatedPasswordOK = false
                    setRegistrationSuccessfulInSharedPrefs()
                    findNavController().navigate(R.id.action_registration_to_login)
                } else {
                    binding.errorMessage.isVisible = true
                }
            }
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
     * Initialize listeners for repeated password edit text
     */
    private fun initRepeatedPasswordCheck() = binding.repeatedPasswordInput.apply {
        isEndIconVisible = false
        editText?.addTextChangedListener {
            repeatedPasswordCheck()
            buttonCheck()
        }

        editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                setRepeatedPasswordError()
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
        val email = editText?.text.toString()
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
        val password = editText?.text.toString()
        if (!passwordOK && password.isNotEmpty())
            error = context.getString(R.string.login_password_error)
        else {
            error = null
        }
    }

    /**
     * Check if repeated password is equal to password,
     * if edit text is not empty, show/hide password symbol is shown
     */
    private fun repeatedPasswordCheck() = binding.repeatedPasswordInput.apply {
        error = null
        repeatedPassword = editText?.text.toString()
        repeatedPasswordOK = repeatedPassword == password
        isEndIconVisible = repeatedPassword.isNotEmpty()
    }

    /**
     * Sets error for repeated password editText if it's not the same as password
     */
    private fun setRepeatedPasswordError() = binding.repeatedPasswordInput.apply {
        val repeatedPassword = editText?.text.toString()
        if (!repeatedPasswordOK && repeatedPassword.isNotEmpty())
            error = context.getString(R.string.registration_repeated_password_error)
        else {
            error = null
        }
    }

    /**
     * Enable register button if email, password and repeated password are correct, disable if not
     */
    private fun buttonCheck() = binding.loginButton.apply {
        if (emailOK && passwordOK && repeatedPasswordOK) {
            isEnabled = true
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.login_button_enabled_text_color
                )
            )
        } else {
            isEnabled = false
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.login_button_disabled_text_color
                )
            )
        }
    }

    /**
     * Initialize button listener for opening LoginFragment after successful registration
     */
    private fun initButtonListener() {
        binding.loginButton.setOnClickListener {
            viewModel.register(email, password, repeatedPassword)
        }
    }

    /**
     * Set true for registration successful in shared preferences
     */
    private fun setRegistrationSuccessfulInSharedPrefs() {
        viewModel.setRegistrationSuccessfulInSharedPrefs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
