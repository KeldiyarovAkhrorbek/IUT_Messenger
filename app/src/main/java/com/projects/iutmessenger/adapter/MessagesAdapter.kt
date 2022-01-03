package com.projects.iutmessenger.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.projects.iutmessenger.databinding.ItemMessageBinding
import com.projects.iutmessenger.models.MessageToAdmin


class MessagesAdapter(var list: ArrayList<MessageToAdmin>, var listener: OnItemClickListener) :
    RecyclerView.Adapter<MessagesAdapter.VH>() {

    inner class VH(var itemMessageBinding: ItemMessageBinding) :
        RecyclerView.ViewHolder(itemMessageBinding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(messageToAdmin: MessageToAdmin) {
            itemMessageBinding.apply {
                groupName.text = messageToAdmin.groupName
                bodyMessage.text = messageToAdmin.messageBody
                if (messageToAdmin.done == true) {
                    card.setCardBackgroundColor(Color.parseColor("#DC75E67A"))
                    doneByText.visibility = View.VISIBLE
                    doneByText.text = "Done by: " + messageToAdmin.done_by
                } else {
                    card.setCardBackgroundColor(Color.parseColor("#C2C3B6"))
                    doneByText.visibility = View.GONE
                }

                card.setOnLongClickListener {
                    listener.onItemLongClick(messageToAdmin, list.indexOf(messageToAdmin))
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickListener {
        fun onItemLongClick(messageToAdmin: MessageToAdmin, position: Int)
    }
}