package com.silvio_mihalic.shows.ui.shows

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.silvio_mihalic.shows.R
import com.silvio_mihalic.shows.databinding.DialogProfileBinding
import com.silvio_mihalic.shows.databinding.FragmentShowsBinding
import com.silvio_mihalic.shows.di.qualifiers.ViewModelInjection
import com.silvio_mihalic.shows.model.entity.UiShow
import com.silvio_mihalic.shows.utils.FileUtil
import com.silvio_mihalic.shows.utils.GetFileUtil
import com.silvio_mihalic.shows.utils.NetworkChecker
import com.silvio_mihalic.shows.utils.NetworkDetector
import com.silvio_mihalic.shows.utils.loadProfilePicture
import com.silvio_mihalic.shows.utils.preparePermissionsContract
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ShowsFragment : DaggerFragment() {

    companion object {
        private const val PHOTOS_MIME_TYPE = "image/*"
        private const val FAB_ANIMATION_START_POINT = 0f
        private const val FAB_ANIMATION_END_POINT = 90f
        private const val FAB_ANIMATION_DURATION = 400L
        private const val RECYCLER_VIEW_LIST = 0
        private const val RECYCLER_VIEW_GRID = 1
        private const val RECYCLER_SCROLL_DEFAULT_POSITION = -1
    }

    private var _binding: FragmentShowsBinding? = null
    private val binding get() = _binding!!

    private var email: String = ""

    private var currentRecyclerViewType = 0

    @Inject
    @ViewModelInjection
    lateinit var viewModel: ShowsViewModel

    private lateinit var networkChecker: NetworkChecker
    private lateinit var networkDetector: NetworkDetector

    private var linearLayout: LinearLayoutManager? = null
    private var gridLayout: GridLayoutManager? = null

    private var adapterList: ShowsAdapter? = null
    private var adapterGrid: ShowsAdapterGrid? = null

    private lateinit var dialogBinding: DialogProfileBinding
    private var dialog: BottomSheetDialog? = null
    private var avatarUri: Uri? = null
    private var profileImageUrl: String? = null
    private var showTopRated = false
    private var showId = -1
    private var networkAvailable = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShowsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()
        initLayouts()
        checkInternetConnection()
        initNetworkDetectorObserver()
        initProfilePictureUploadedObserver()
        setUpBottomSheet()
        closeAppOnBackPressed()
    }

    private fun initObservers() {
        viewModel.showDetailsStateLiveData.observe(viewLifecycleOwner, { state ->
            email = state.email
            avatarUri = state.avatarUri
            profileImageUrl = state.profileImageUrl
            currentRecyclerViewType = state.currentRecyclerViewType
            showTopRated = state.showTopRated
            showId = state.showId

            setProfilePictures()
            initFabListener()
            initTopRatedButtonListener()
            initShowsObserver()
        })
    }

    /**
     * Create list and grid layout which is user for recycler view layout
     */
    private fun initLayouts() {
        linearLayout = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        gridLayout = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
    }

    private fun checkInternetConnection() {
        networkChecker = NetworkChecker()
        networkAvailable = networkChecker.internetIsConnected()
    }

    /**
     * Initialize observer for shows list, if showsList is empty, empty list is passed to function
     * checkIfShowsExist in order to show empty state
     */
    private fun initShowsObserver() {
        viewModel.getShows(showTopRated)
        viewModel.allShowsLiveData.observe(viewLifecycleOwner, { showsList ->
            if (showsList != null) {
                createAdapters(showsList)
                checkIfShowsExist(showsList)
            } else {
                checkIfShowsExist(emptyList())
            }
        })
    }

    /**
     * Create adapter for both list and grid view to set it later on FAB click
     */
    private fun createAdapters(shows: List<UiShow>) {
        adapterList = ShowsAdapter(shows, networkAvailable) { showID, itemPosition ->
            saveShowIdToSharedPrefs(itemPosition)
            val action = ShowsFragmentDirections.actionShowsToShowDetails(
                showID
            )
            findNavController().navigate(action)
        }

        adapterGrid = ShowsAdapterGrid(shows, networkAvailable) { showID, itemPosition ->
            saveShowIdToSharedPrefs(itemPosition)
            val action = ShowsFragmentDirections.actionShowsToShowDetails(
                showID
            )
            findNavController().navigate(action)
        }
    }

    private fun saveShowIdToSharedPrefs(showId: Int) {
        viewModel.saveShowIdToSharedPrefs(showId)
    }

    /**
     * Initialize profile picture upload result observer, if upload is successful, user is our
     * user with image url, which is set it shared prefs and profile picture image views are updated,
     * if upload is not successful, user is null and error message is shown
     */
    private fun initProfilePictureUploadedObserver() {
        viewModel.imageUploadResultLiveData.observe(viewLifecycleOwner, { user ->
            if (user != null) {
                profileImageUrl = user.imageUrl
                setProfilePictureUrlInSharedPrefs()
                setProfilePictures()
                dialogBinding.errorMessage.visibility = View.GONE
            } else {
                dialogBinding.errorMessage.isVisible = true
            }
        })
    }

    /**
     * Function which handles floating action button click, it runs icon animation,
     * changes recycler view type and save type in shared prefs
     * If currentRecyclerViewType is equal to RECYCLER_VIEW_GRID on fragment create, set icon
     * in correct position
     */
    private fun initFabListener() = binding.floatingActionButton.apply {
        if (currentRecyclerViewType == RECYCLER_VIEW_GRID) {
            animate()
                .rotation(FAB_ANIMATION_END_POINT)
                .setDuration(0)
                .start()
        }
        setOnClickListener {
            if (currentRecyclerViewType == RECYCLER_VIEW_GRID) {
                animate()
                    .rotation(FAB_ANIMATION_START_POINT)
                    .setDuration(FAB_ANIMATION_DURATION)
                    .start()
                currentRecyclerViewType = RECYCLER_VIEW_LIST
                initRecyclerViewList()
            } else {
                animate()
                    .rotation(FAB_ANIMATION_END_POINT)
                    .setDuration(FAB_ANIMATION_DURATION)
                    .start()
                currentRecyclerViewType = RECYCLER_VIEW_GRID
                initRecyclerViewGrid()
            }
            setRecyclerViewLayoutInSharedPrefs()
        }
    }

    /**
     * Set recycler view layout type in shared prefs
     */
    private fun setRecyclerViewLayoutInSharedPrefs() {
        viewModel.setRecyclerViewLayoutInSharedPrefs(currentRecyclerViewType)
    }

    /**
     * Initialize RecyclerView list
     */
    private fun initRecyclerViewList() {
        binding.showsRecycler.layoutManager = linearLayout
        binding.showsRecycler.adapter = adapterList
    }

    /**
     * Initialize RecyclerView grid
     */
    private fun initRecyclerViewGrid() {
        binding.showsRecycler.layoutManager = gridLayout
        binding.showsRecycler.adapter = adapterGrid
    }

    /**
     * Sets click listener for top rated button, call view model function to get shows, if isChecked
     * parameter is true then top rated shows will be retrieved, else regular list of shows will
     * be retrieved
     */
    private fun initTopRatedButtonListener() = binding.topRatedButton.apply {
        isChecked = showTopRated

        setOnClickListener {
            showId = RECYCLER_SCROLL_DEFAULT_POSITION
            viewModel.getShows(isChecked)
            setTopRatedInSharedPrefs(isChecked)
            saveShowIdToSharedPrefs(RECYCLER_SCROLL_DEFAULT_POSITION)
        }
    }

    /**
     * Set true if top rated shows are shown, false if regular list of shows is shown in shared prefs
     */
    private fun setTopRatedInSharedPrefs(showTopRated: Boolean) {
        viewModel.setTopRatedInSharedPrefs(showTopRated)
    }

    /**
     * Hide RecyclerView and show empty state if shows list is empty, show RecyclerView
     * if shows list is not empty
     */
    private fun checkIfShowsExist(shows: List<UiShow>) {
        binding.emptyState.isVisible = shows.isEmpty()
        binding.showsRecycler.isVisible = shows.isNotEmpty()
        if (shows.isNotEmpty()) {
            if (currentRecyclerViewType == 0) {
                initRecyclerViewList()
            } else {
                initRecyclerViewGrid()
            }
            if (showId != RECYCLER_SCROLL_DEFAULT_POSITION) {
                binding.showsRecycler.scrollToPosition(showId)
                saveShowIdToSharedPrefs(RECYCLER_SCROLL_DEFAULT_POSITION)
            }
        }
    }

    /**
     * Initialize bottom sheet and profile button
     */
    private fun setUpBottomSheet() {
        dialog = activity?.let { BottomSheetDialog(it) }
        dialogBinding = DialogProfileBinding.inflate(layoutInflater)
        dialog?.setContentView(dialogBinding.root)
        binding.profileButton.setOnClickListener {
            showBottomSheet()
        }
    }

    /**
     * Show bottom sheet and set values for its elements, initialize logout button and change
     * profile picture button listeners
     */
    private fun showBottomSheet() {
        dialogBinding.email.text = email

        dialogBinding.changeProfilePhotoButton.setOnClickListener {
            initChangeProfilePictureDialog()
        }

        dialogBinding.logoutButton.setOnClickListener {
            dialog?.dismiss()
            initLogoutAlertDialog()
        }

        dialog?.show()
    }

    /**
     * Check if there is internet connection when fragment is created using NetworkChecker
     * class, change noInternetMessage visibility using NetworkDetector class
     */
    private fun initNetworkDetectorObserver() {
        binding.noInternetMessage.isVisible = !networkAvailable
        networkDetector = NetworkDetector(context)
        networkDetector.listenForInternetConnectivity()
        networkDetector.networkAvailableLiveData.observe(viewLifecycleOwner, { networkAvailable ->
            binding.noInternetMessage.isVisible = !networkAvailable
        })
    }

    /**
     * Initialize alert dialog for a user to select if they want to take a new picture or to choose an
     * image from a gallery
     */
    private fun initChangeProfilePictureDialog() {
        val items = arrayOf(
            getString(R.string.dialog_profile_picture_new_photo),
            getString(R.string.dialog_profile_picture_gallery_photo)
        )
        activity?.let { fragmentActivity ->
            MaterialAlertDialogBuilder(fragmentActivity)
                .setTitle(getString(R.string.dialog_change_profile_picture_options))
                .setItems(items) { _, which ->
                    if (which == items.indexOf(getString(R.string.dialog_profile_picture_new_photo))) {
                        cameraPermissionContract.launch(arrayOf(Manifest.permission.CAMERA))
                    }
                    if (which == items.indexOf(getString(R.string.dialog_profile_picture_gallery_photo))) {
                        galleryPermissionContract.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                    }
                }
                .show()
        }
    }

    /**
     * Initialize camera permission contract and if granted call a function for taking a new picture
     */
    private val cameraPermissionContract = preparePermissionsContract(onPermissionsGranted = {
        if (!Uri.EMPTY.equals(avatarUri)) {
            createNewUri()
        }
        takePicture.launch(avatarUri)
    })

    /**
     * Open a camera and take a picture, on success call live data function to upload an image
     * to server
     */
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                val file = FileUtil.getImageFile(activity)
                if (file != null) {
                    viewModel.uploadImage(file)
                }
            }
        }

    /**
     * Initialize gallery permission contract and if granted call a function to open a gallery
     */
    private val galleryPermissionContract = preparePermissionsContract(onPermissionsGranted = {
        if (!Uri.EMPTY.equals(avatarUri)) {
            createNewUri()
        }
        choosePhoto.launch(PHOTOS_MIME_TYPE)
    })

    /**
     * Open a gallery and let user to choose an image, create file from that uri, upload image,
     * change profile pictures
     */
    private val choosePhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val file = GetFileUtil.getFile(activity, uri)
                viewModel.uploadImage(file)
            }
        }

    /**
     * Create a new uri for photos and save that uri in shared prefs
     */
    private fun createNewUri() {
        avatarUri = activity?.let { fragmentActivity ->
            activity?.let { FileUtil.createImageFile(it) }
                ?.let { file ->
                    FileProvider.getUriForFile(
                        fragmentActivity,
                        fragmentActivity.applicationContext.packageName.toString() + ".fileprovider",
                        file
                    )
                }
        }
        setProfilePictureUriInSharedPrefs()
    }

    /**
     * Save avatarUri in shared prefs to it can be accessed in the future
     */
    private fun setProfilePictureUriInSharedPrefs() {
        viewModel.setProfilePictureUriInSharedPrefs(avatarUri)
    }

    /**
     * Save profileImageUrl in shared prefs to it can be accessed in the future
     */
    private fun setProfilePictureUrlInSharedPrefs() {
        viewModel.setProfilePictureUrlInSharedPrefs(profileImageUrl)
    }

    /**
     * Set profile picture on profile button and bottom sheet
     */
    private fun setProfilePictures() {
        binding.profileButton.loadProfilePicture(profileImageUrl)
        dialogBinding.profilePicture.loadProfilePicture(profileImageUrl)
    }

    /**
     * Initialize and set alert dialog text when user wants to log out
     */
    private fun initLogoutAlertDialog() {
        activity?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(resources.getString(R.string.logout_alert_dialog_title))
                .setMessage(resources.getString(R.string.logout_alert_dialog_message))
                .setNegativeButton(resources.getString(R.string.logout_alert_dialog_decline)) { alertDialog, _ ->
                    alertDialog.dismiss()
                }
                .setPositiveButton(resources.getString(R.string.logout_alert_dialog_accept)) { _, _ ->
                    logout()
                }
                .show()
        }
    }

    /**
     * Change shared preferences USER_LOGGED_IN value to false, navigate to login fragment
     */
    private fun logout() {
        viewModel.logout()
        findNavController().navigate(R.id.action_shows_to_login)
    }

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
