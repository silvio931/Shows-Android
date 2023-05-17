package com.silvio_mihalic.shows.ui.showDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.silvio_mihalic.shows.R
import com.silvio_mihalic.shows.databinding.DialogAddReviewBinding
import com.silvio_mihalic.shows.databinding.FragmentShowDetailsBinding
import com.silvio_mihalic.shows.di.qualifiers.ViewModelInjection
import com.silvio_mihalic.shows.model.entity.UiReview
import com.silvio_mihalic.shows.utils.NetworkChecker
import com.silvio_mihalic.shows.utils.NetworkDetector
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ShowDetailsFragment : DaggerFragment() {

    companion object {
        private const val MIN_RATING = 1F
        private const val SWIPE_PROGRESS_OFFSET_START = 100
        private const val SWIPE_PROGRESS_OFFSET_END = 270
        private const val MAX_TITLE_CHARACTERS = 22
    }

    private var _binding: FragmentShowDetailsBinding? = null
    private val binding get() = _binding!!

    @Inject
    @ViewModelInjection
    lateinit var viewModel: ShowDetailsViewModel

    private val args: ShowDetailsFragmentArgs by navArgs()

    private var adapter: ReviewsAdapter? = null

    private lateinit var networkChecker: NetworkChecker
    private lateinit var networkDetector: NetworkDetector

    private var email: String? = ""
    private var showID: String = ""
    private var showName: String = ""
    private var showDescription: String = ""
    private var numberOfReviews: Int = 0
    private var averageRating: Int = 0
    private var showImageResourceID: String = ""
    private var recyclerViewAlreadyInitialized = false
    private var networkAvailable = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShowDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkInternetConnection()
        getArgs()
        getEmailFromSharedPrefs()
        initShowsObserver()
        initReviewsObserver()
        setUpToolbar()
        setUpBottomSheetButton()
        initCreateReviewResultObserver()
        initCreateReviewOfflineResultObserver()
        initOfflineReviewsSentToApiObserver()
        initSwipeRefresh()
        initNetworkDetectorObserver()
    }

    /**
     * Initialize show details observer, call live data function to get reviews for this show
     * and function to set review score
     * if (showName != show.title) will be true when fragment is opened, and then
     * show name, description, image and reviews are shown, this observer is called every time
     * user creates new review, if statement will be false, and only number of reviews and
     * average score will be updated
     */
    private fun initShowsObserver() {
        viewModel.getOneShow(showID)
        viewModel.oneShowLiveData.observe(viewLifecycleOwner, { show ->
            if (showName != show.title) {
                showName = show.title
                showDescription = show.description
                showImageResourceID = show.imageUrl
                setUpPageTitleNameDescription()
            }
            numberOfReviews = show.noOfReviews
            averageRating = show.averageRating
            setReviewScore()
        })
    }

    /**
     * Initialize reviews observer, call a function to update data on the fragment, initialize
     * recycler view first time
     */
    private fun initReviewsObserver() {
        viewModel.getReviews(showID, email.toString())
        viewModel.allReviewsLiveData.observe(viewLifecycleOwner, { reviewsList ->
            if (!recyclerViewAlreadyInitialized) {
                initRecyclerView(reviewsList)
            }
            updateUIElements(reviewsList)
            binding.swipeRefresh.isRefreshing = false
        })
    }

    private fun checkInternetConnection() {
        networkChecker = NetworkChecker()
        networkAvailable = networkChecker.internetIsConnected()
    }

    /**
     * Initialize observer for result of creating a new review, if successful add new review
     * in adapter and call a view model function to get last show data for average score and
     * number of reviews
     */
    private fun initCreateReviewResultObserver() {
        viewModel.getCreateReviewResult().observe(viewLifecycleOwner, { review ->
            if (review != null) {
                adapter?.addItem(review)
                viewModel.getOneShow(showID)
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.review_create_not_successful),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    /**
     * Initialize observer for creating reviews in offline mode, when review is saved in database
     * write a message to user
     */
    private fun initCreateReviewOfflineResultObserver() {
        viewModel.createReviewOfflineResult().observe(viewLifecycleOwner, {
            Toast.makeText(
                context,
                getString(R.string.create_review_offline_message),
                Toast.LENGTH_LONG
            ).show()
        })
    }

    /**
     * Initialize observer for uploading offline reviews to api, add them to adapter and
     * get data about show (score and number of reviews are updated), if upload is not successful
     * write a message
     */
    private fun initOfflineReviewsSentToApiObserver() {
        viewModel.getOfflineReviewsInsertResult().observe(viewLifecycleOwner, { review ->
            if (review != null) {
                adapter?.addItem(review)
                viewModel.getOneShow(showID)
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.offline_reviews_not_uploaded),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    /**
     * Get arguments sent from ShowsFragment about selected show id
     */
    private fun getArgs() {
        showID = args.showId
    }

    /**
     * Get email from shared prefs
     */
    private fun getEmailFromSharedPrefs() {
        viewModel.emailLiveData.observe(viewLifecycleOwner, { emailResult ->
            email = emailResult
        })
    }

    /**
     * Set refresh animation offset and call function to get reviews on refresh
     */
    private fun initSwipeRefresh() = binding.swipeRefresh.apply {
        setProgressViewOffset(false, SWIPE_PROGRESS_OFFSET_START, SWIPE_PROGRESS_OFFSET_END)
        setOnRefreshListener {
            viewModel.getReviews(showID, email.toString())
        }
    }

    /**
     * Set toolbar text and back button
     */
    private fun setUpToolbar() {
        activity?.setActionBar(binding.toolbar)
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.actionBar?.title = ""
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    }

    /**
     * Set show name as page title, show image and show description
     */
    private fun setUpPageTitleNameDescription() {
        if (showName.length > MAX_TITLE_CHARACTERS) {
            showName = "${showName.take(MAX_TITLE_CHARACTERS)}..."
        }
        binding.customTitle.text = showName
        binding.cardDetails.setDescription(showDescription)
        binding.cardDetails.setImage(showImageResourceID, networkAvailable)
    }

    /**
     * Show/hide empty state, call a function to update review score if reviews list is not empty,
     * call a function in reviews adapter to update its list and show in recycler view
     */
    private fun updateUIElements(reviews: List<UiReview>) {
        binding.ratingBarScore.isVisible = reviews.isNotEmpty()
        binding.ratingScore.isVisible = reviews.isNotEmpty()
        binding.noReviewsMessage.isVisible = reviews.isEmpty()

        if (reviews.isNotEmpty()) {
            setReviewScore()
            adapter?.updateList(reviews)
        }
    }

    /**
     * Initialize RecyclerView
     */
    private fun initRecyclerView(reviews: List<UiReview>) {
        if (reviews.isNotEmpty()) {
            binding.reviewsRecycler.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            adapter = ReviewsAdapter(reviews)
            binding.reviewsRecycler.adapter = adapter
            recyclerViewAlreadyInitialized = true
            binding.reviewsRecycler.addItemDecoration(
                DividerItemDecoration(
                    activity,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    /**
     * Every time a new review is added, number of reviews and average score are
     * updated on the screen
     */
    private fun setReviewScore() {
        binding.ratingScore.text =
            getString(R.string.review_score, numberOfReviews.toString(), averageRating.toString())
        binding.ratingBarScore.rating = averageRating.toFloat()
    }

    private fun setUpBottomSheetButton() {
        binding.addReviewButton.setOnClickListener {
            showBottomSheet()
        }
    }

    /**
     * Check if there is internet connection when fragment is created using NetworkChecker
     * class, change noInternetMessage visibility using NetworkDetector class
     */
    private fun initNetworkDetectorObserver() {
        networkChecker = NetworkChecker()
        binding.noInternetMessage.isVisible = !networkAvailable
        networkDetector = NetworkDetector(context)
        networkDetector.listenForInternetConnectivity()
        networkDetector.networkAvailableLiveData.observe(viewLifecycleOwner, { networkAvailable ->
            binding.noInternetMessage.isVisible = !networkAvailable
        })
    }

    /**
     * Create and show BottomSheet for adding reviews, create new review in show details live data
     * Check if rating value if 0 on submit button pressed, if true show error message, on rating
     * bar value change hide error message
     * Close BottomSheet on close button press
     */
    private fun showBottomSheet() {
        val dialog = activity?.let { BottomSheetDialog(it) }
        val dialogBinding = DialogAddReviewBinding.inflate(layoutInflater)
        dialog?.setContentView(dialogBinding.root)

        dialogBinding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            if (rating >= MIN_RATING) {
                dialogBinding.ratingBarError.isVisible = false
            }
        }

        dialogBinding.confirmButton.setOnClickListener {
            if (dialogBinding.ratingBar.rating < MIN_RATING) {
                dialogBinding.ratingBarError.isVisible = true
            } else {
                viewModel.createReview(
                    dialogBinding.ratingBar.rating.toInt(),
                    dialogBinding.reviewInput.editText?.text.toString(),
                    showID.toInt(),
                    email.toString()
                )

                dialog?.dismiss()
            }
        }

        dialogBinding.closeButton.setOnClickListener {
            dialog?.dismiss()
        }

        dialog?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
