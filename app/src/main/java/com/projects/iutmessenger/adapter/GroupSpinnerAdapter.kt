package com.projects.iutmessenger.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.projects.iutmessenger.R
import com.projects.iutmessenger.databinding.ItemGroupSpinnerBinding
import com.projects.iutmessenger.models.Group
import com.squareup.picasso.Picasso

class GroupSpinnerAdapter(val list: ArrayList<Group>, var context: Context) :
    BaseAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(p0: Int): Group {
        return list[p0]
    }

    override fun getItemId(p0: Int): Long {
        return list.size.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val binding = ItemGroupSpinnerBinding.inflate(LayoutInflater.from(context), p2, false)

        binding.apply {
            Picasso.get().load(list[p0].imgUrl).placeholder(R.drawable.iut1).error(R.drawable.error)
                .into(binding.imgGroup)
            groupNameTv.text = list[p0].groupName
        }
        return binding.root
    }

}