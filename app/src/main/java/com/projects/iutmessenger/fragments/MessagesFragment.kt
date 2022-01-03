package com.projects.iutmessenger.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.projects.iutmessenger.MainActivity
import com.projects.iutmessenger.adapter.MessagesAdapter
import com.projects.iutmessenger.databinding.FragmentMessagesBinding
import com.projects.iutmessenger.models.MessageToAdmin
import com.projects.iutmessenger.models.Student
import com.projects.iutmessenger.notification.NotificationData
import com.projects.iutmessenger.notification.PushNotification
import com.projects.iutmessenger.notification.retrofit.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MessagesFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var messagesList: ArrayList<MessageToAdmin>
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var referenceUser: DatabaseReference
    private lateinit var referenceMessages: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        initialize()
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = "Messages"
        messagesList = ArrayList()
        messagesAdapter =
            MessagesAdapter(messagesList, object : MessagesAdapter.OnItemClickListener {
                override fun onItemLongClick(messageToAdmin: MessageToAdmin, position: Int) {
                    if (messageToAdmin.done == false) {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setMessage("Are you sure you want to mark \"${messageToAdmin.groupName}\" as opened?")
                        builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                referenceUser.child(auth.uid ?: "")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val student =
                                                snapshot.getValue(Student::class.java) as Student
                                            messageToAdmin.done_by =
                                                student.name + " " + student.surname
                                            messageToAdmin.done = true
                                            referenceMessages.child(messageToAdmin.senderUID ?: "")
                                                .setValue(messageToAdmin)
                                                .addOnSuccessListener {
                                                    val pushNotification = PushNotification(
                                                        NotificationData(
                                                            messageToAdmin.groupName.toString(),
                                                            "Dear user, your group has been created by ${student.name} ${student.surname},\n" +
                                                                    "You can now register!"
                                                        ), messageToAdmin.senderTOKEN.toString()
                                                    )
                                                    sendNotification(pushNotification)
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Notification sent to user!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    messagesList.clear()
                                                }
                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }
                                    })
                            }

                        })

                        builder.setNegativeButton("No", object : DialogInterface.OnClickListener {
                            override fun onClick(p0: DialogInterface?, p1: Int) {

                            }

                        })

                        builder.show()
                    }

                }
            })


        Log.d("TAG", "fetchMessages: ")
        referenceMessages.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesList.clear()
                val children = snapshot.children
                for (child in children) {
                    val value = child.getValue(MessageToAdmin::class.java)
                    if (value != null)
                        messagesList.add(value)
                }
                messagesAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        binding.rv.adapter = messagesAdapter
        return binding.root
    }

    private fun fetchMessages() {

    }

    private fun sendNotification(pushNotification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(pushNotification)
                if (response.isSuccessful) {
                    Log.d("TAG", "Response: ")
                } else {
                    Log.e("TAG", response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e("TAG", e.toString())
            }
        }

    private fun initialize() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        referenceUser = database.getReference("users")
        referenceMessages = database.getReference("messages")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MessagesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideBottomNavigation()
        (activity as MainActivity).showNavigationIcon()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPause() {
        (activity as MainActivity).showBottomNavigation()
        super.onPause()
    }
}