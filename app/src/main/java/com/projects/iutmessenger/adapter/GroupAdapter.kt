package com.projects.iutmessenger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.projects.iutmessenger.R
import com.projects.iutmessenger.databinding.ItemGroupBinding
import com.projects.iutmessenger.models.Group
import com.squareup.picasso.Picasso

class GroupAdapter(var list: ArrayList<Group>, var listener: OnItemClickListener) :
    RecyclerView.Adapter<GroupAdapter.VH>() {

    inner class VH(var itemGroupBinding: ItemGroupBinding) :
        RecyclerView.ViewHolder(itemGroupBinding.root) {
        fun onBind(group: Group) {
            itemGroupBinding.apply {
                groupNameTv.text = group.groupName
                layout.setOnClickListener {
                    listener.onItemGroup(group)
                }
                Picasso.get().load(group.imgUrl).error(R.drawable.error)
                    .placeholder(R.drawable.iut1).into(groupImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickListener {
        fun onItemGroup(group: Group)
    }
}