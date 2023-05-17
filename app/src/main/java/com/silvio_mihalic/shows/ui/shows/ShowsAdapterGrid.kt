package com.silvio_mihalic.shows.ui.shows

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.silvio_mihalic.shows.databinding.ViewShowItemGridBinding
import com.silvio_mihalic.shows.model.entity.UiShow
import com.silvio_mihalic.shows.utils.loadShowImage

class ShowsAdapterGrid(
    private var items: List<UiShow>,
    private val networkAvailable: Boolean,
    private val onClickCallback: (String, Int) -> Unit
) : RecyclerView.Adapter<ShowsAdapterGrid.ShowGridViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowGridViewHolder {
        val binding = ViewShowItemGridBinding.inflate(LayoutInflater.from(parent.context))
        return ShowGridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShowGridViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ShowGridViewHolder(private val binding: ViewShowItemGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UiShow) = binding.apply {
            showName.text = item.title

            showImage.loadShowImage(item.imageUrl, networkAvailable)

            root.setOnClickListener {
                onClickCallback(item.id, items.indexOf(item))
            }
        }
    }
}
