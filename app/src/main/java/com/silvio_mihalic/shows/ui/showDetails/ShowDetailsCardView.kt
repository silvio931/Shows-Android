package com.silvio_mihalic.shows.ui.showDetails

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.silvio_mihalic.shows.databinding.CardShowDetailsBinding
import com.silvio_mihalic.shows.utils.loadShowImage

class ShowDetailsCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttrs) {

    private var binding: CardShowDetailsBinding

    init {
        binding = CardShowDetailsBinding.inflate(LayoutInflater.from(context), this)
    }

    fun setDescription(description: String) {
        binding.showDescription.text = description
    }

    fun setImage(url: String, networkAvailable: Boolean) {
        binding.showImage.loadShowImage(url, networkAvailable)
    }
}
