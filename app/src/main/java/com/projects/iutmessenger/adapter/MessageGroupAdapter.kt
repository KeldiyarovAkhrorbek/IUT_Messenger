package com.projects.iutmessenger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.projects.iutmessenger.databinding.ItemFromGroupBinding
import com.projects.iutmessenger.databinding.ItemToBinding
import com.projects.iutmessenger.models.ChatMessage
import com.squareup.picasso.Picasso

class MessageGroupAdapter(var currentUid: String, var list: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class FromVh(var itemFromGroupBinding: ItemFromGroupBinding) :
        RecyclerView.ViewHolder(itemFromGroupBinding.root) {

        fun onBind(messageGroup: ChatMessage) {
            itemFromGroupBinding.apply {
                tv.text = messageGroup.message
                date.text = messageGroup.date
                Picasso.get().load(messageGroup.photoUrl).into(image)
                username.text = messageGroup.userName
            }
        }
    }

    inner class ToVh(var itemToBinding: ItemToBinding) :
        RecyclerView.ViewHolder(itemToBinding.root) {
        fun onBind(messageGroup: ChatMessage) {
            itemToBinding.apply {
                tv.text = messageGroup.message
                date.text = messageGroup.date
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            FromVh(
                ItemFromGroupBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            ToVh(ItemToBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FromVh) {
            holder.onBind(list[position])
        } else {
            val toVh = holder as ToVh
            toVh.onBind(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        if (list[position].from == currentUid) {
            return 2
        }
        return 1
    }
}