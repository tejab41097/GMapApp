package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.myapplication.R
import com.example.myapplication.database.model.Marker
import com.example.myapplication.databinding.ItemTravelBinding

enum class Action {
    DELETE, CLICK
}

class TravelAdapter : RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {

    interface ClickListener {
        fun onClick(action: Action, marker: Marker, position: Int)
    }

    private var list = mutableListOf<Marker>()
    var clickListener: ClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TravelViewHolder(
        ItemTravelBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        val marker = list[position]
        holder.binding.root.setOnClickListener {
            clickListener?.onClick(Action.CLICK, marker, position)
        }
        holder.binding.fbDeleteMarker.setOnClickListener {
            clickListener?.onClick(Action.DELETE, marker, position)
        }
        holder.binding.tvTitle.text =
            holder.binding.root.context.getString(R.string.property_name2, marker.title)
        holder.binding.tvLatitude.text =
            holder.binding.root.context.getString(R.string.latitude, marker.latitude)
        holder.binding.tvLongitude.text =
            holder.binding.root.context.getString(R.string.longitude, marker.longitude)
    }

    override fun getItemCount() = list.size

    inner class TravelViewHolder(val binding: ItemTravelBinding) : ViewHolder(binding.root)

    fun setData(list: List<Marker>) {
        this.list.clear()
        this.list.addAll(list)
        notifyItemRangeInserted(0, list.size)
    }
}
