package com.silvio_mihalic.shows.ui.showDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.silvio_mihalic.shows.databinding.ItemReviewBinding
import com.silvio_mihalic.shows.model.entity.UiReview
import com.silvio_mihalic.shows.utils.loadProfilePicture

private const val EMAIL_SPLIT_SYMBOL = "@"
private const val MAX_USERNAME_CHARACTERS = 30

class ReviewsAdapter(
    private var items: List<UiReview>
) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem(newReview: UiReview) {
        items = items + newReview
        items = items.reversed()
        notifyItemInserted(0)
    }

    fun updateList(reviews: List<UiReview>) {
        items = reviews
        notifyDataSetChanged()
    }

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Set values for each review, if review comment is empty, TextView is hidden
         */
        fun bind(item: UiReview) = binding.apply {
            var userName = item.userEmail.split(EMAIL_SPLIT_SYMBOL)?.first()
            if (userName.length > MAX_USERNAME_CHARACTERS) {
                userName = "${userName.take(MAX_USERNAME_CHARACTERS)}..."
            }
            username.text = userName
            if (item.comment.isNotEmpty()) {
                review.isVisible = true
                review.text = item.comment
            }

            profilePicture.loadProfilePicture(item.userImageUrl)

            ratingValue.text = item.rating.toString()
        }
    }
}
