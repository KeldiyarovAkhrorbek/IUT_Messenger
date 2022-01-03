package com.projects.iutmessenger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.projects.iutmessenger.R
import com.projects.iutmessenger.databinding.ItemStudentBinding
import com.projects.iutmessenger.models.Student
import com.romainpiel.shimmer.Shimmer
import com.squareup.picasso.Picasso


class StudentAdapter(var list: ArrayList<Student>, var listener: OnItemClickListener) :
    RecyclerView.Adapter<StudentAdapter.VH>() {

    inner class VH(var itemStudentBinding: ItemStudentBinding) :
        RecyclerView.ViewHolder(itemStudentBinding.root) {
        fun onBind(student: Student) {
            itemStudentBinding.apply {
                if (student.role == "admin") {
                    usernameRainbow.visibility = View.VISIBLE
                    usernameRainbow.text = student.name + " " + student.surname
                    usernameRainbow.isSelected = true
                    usernameShimmer.visibility = View.GONE
                    usernameSimple.visibility = View.GONE
                } else if (student.role == "moderator") {
                    usernameShimmer.visibility = View.VISIBLE
                    usernameRainbow.visibility = View.GONE
                    usernameSimple.visibility = View.GONE
                    val shimmer = Shimmer()
                        .setDuration(1000)
                        .setStartDelay(1000)
                        .setDirection(Shimmer.ANIMATION_DIRECTION_LTR)
                    shimmer.start(usernameShimmer)
                    usernameShimmer.text = student.name + "\n" + student.surname
                    usernameShimmer.isSelected = true
                } else {
                    usernameSimple.visibility = View.VISIBLE
                    usernameShimmer.visibility = View.GONE
                    usernameRainbow.visibility = View.GONE
                    usernameSimple.text = student.name + " " + student.surname
                    usernameSimple.isSelected = true
                }
                layout.setOnClickListener {
                    listener.onItemGroup(student)
                }
                Picasso.get().load(student.imageUrl).error(R.drawable.error)
                    .placeholder(R.drawable.iut1).into(userImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickListener {
        fun onItemGroup(student: Student)
    }
}