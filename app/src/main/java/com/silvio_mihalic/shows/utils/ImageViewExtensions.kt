package com.silvio_mihalic.shows.utils

import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.silvio_mihalic.shows.R

private const val CIRCULAR_PROGRESS_STROKE_WIDTH = 6f
private const val CIRCULAR_PROGRESS_STROKE_RADIUS = 50f

fun ImageView.loadProfilePicture(imageUrl: String?) {
    Glide.with(this)
        .load(imageUrl)
        .placeholder(R.drawable.ic_profile_placeholder)
        .into(this)
}

fun ImageView.loadShowImage(imageUrl: String, networkAvailable: Boolean) {
    if (networkAvailable) {
        val circularProgress = CircularProgressDrawable(context)
        circularProgress.strokeWidth = CIRCULAR_PROGRESS_STROKE_WIDTH
        circularProgress.centerRadius = CIRCULAR_PROGRESS_STROKE_RADIUS
        circularProgress.start()

        Glide.with(this)
            .load(imageUrl)
            .placeholder(circularProgress)
            .into(this)
    } else {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_loading_placeholder)
            .into(this)
    }
}
