package com.silvio_mihalic.shows.ui.shows

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.silvio_mihalic.shows.databinding.ViewShowItemBinding
import com.silvio_mihalic.shows.model.entity.UiShow
import com.silvio_mihalic.shows.utils.loadShowImage

class ShowsAdapter(
    private var items: List<UiShow>,
    private val networkAvailable: Boolean,
    private val onClickCallback: (String, Int) -> Unit
) : RecyclerView.Adapter<ShowsAdapter.ShowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowViewHolder {
        val binding = ViewShowItemBinding.inflate(LayoutInflater.from(parent.context))
        return ShowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShowViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ShowViewHolder(private val binding: ViewShowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UiShow) = binding.apply {
            showName.text = item.title
            showDescription.text = item.description

            showImage.loadShowImage(item.imageUrl, networkAvailable)

            root.setOnClickListener {
                onClickCallback(item.id, items.indexOf(item))
            }
        }
    }
}
