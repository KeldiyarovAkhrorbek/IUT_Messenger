package com.projects.iutmessenger.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.projects.iutmessenger.MainActivity
import com.projects.iutmessenger.adapter.MessageGroupAdapter
import com.projects.iutmessenger.databinding.FragmentGroupChatBinding
import com.projects.iutmessenger.models.ChatMessage
import com.projects.iutmessenger.models.Student
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class GroupChatFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var _binding: FragmentGroupChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var messageGroupAdapter: MessageGroupAdapter
    private var messageList = ArrayList<ChatMessage>()
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var referenceStudent: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupChatBinding.inflate(inflater, container, false)
        initialize()

        messageGroupAdapter = MessageGroupAdapter(auth.uid.toString(), messageList)
        binding.rv.adapter = messageGroupAdapter
        fetchMessages()
        setHasOptionsMenu(true)
        binding.textField.requestFocus()
        binding.sendButton.setOnClickListener {
            val messageText = binding.textField.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val date = Calendar.getInstance().time
                val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
                val dateString = simpleDateFormat.format(date)
                referenceStudent.child(auth.uid ?: "")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val student = snapshot.getValue(Student::class.java)!!
                            val messageGroup =
                                ChatMessage(
                                    auth.uid ?: "",
                                    messageText,
                                    dateString,
                                    student.imageUrl,
                                    student.name + " " + student.surname
                                )
                            val key = databaseReference.push().key
                            val bundle = arguments
                            val groupID = bundle?.getLong("groupID") as Long
                            databaseReference.child(groupID.toString() ?: "")
                                .child("messages")
                                .child(key ?: "")
                                .setValue(messageGroup)
                            binding.textField.setText("")
                            messageList.clear()
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

            }
        }
        return binding.root
    }

    private fun initialize() {
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("groups")
        referenceStudent = firebaseDatabase.getReference("users")
    }

    private fun fetchMessages() {
        val bundle = arguments
        messageList.clear()
        val groupID = bundle?.getLong("groupID") as Long
        databaseReference.child(groupID.toString()).child("messages")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    for (child in children) {
                        val value = child.getValue(ChatMessage::class.java)
                        if (value != null) {
                            messageList.add(value)
                        }
                    }
                    messageGroupAdapter.notifyDataSetChanged()
                    binding.rv.scrollToPosition(messageList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    override fun onResume() {
        (activity as MainActivity).hideBottomNavigation()
        (activity as MainActivity).showNavigationIcon()
        super.onResume()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }
}