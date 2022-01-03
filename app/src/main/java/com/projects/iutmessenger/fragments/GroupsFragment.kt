package com.projects.iutmessenger.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.projects.iutmessenger.MainActivity
import com.projects.iutmessenger.R
import com.projects.iutmessenger.SignInActivity
import com.projects.iutmessenger.adapter.GroupAdapter
import com.projects.iutmessenger.databinding.FragmentGroupsBinding
import com.projects.iutmessenger.models.Group
import com.projects.iutmessenger.models.Student


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class GroupsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var referenceStudent: DatabaseReference
    private lateinit var groupList: ArrayList<Group>
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var student: Student
    private val TAG = "GroupsFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        student = Student()
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("groups")
        referenceStudent = firebaseDatabase.getReference("users")
        groupList = ArrayList()
        fetchGroups()
        fetchStudent()
        groupAdapter = GroupAdapter(groupList, object : GroupAdapter.OnItemClickListener {
            override fun onItemGroup(group: Group) {
                val bundle = Bundle()
                bundle.putLong("groupID", group.groupID ?: 0)
                findNavController().navigate(R.id.action_nav_groups_to_insideGroupFragment2, bundle)
            }

        })

        binding.rv.adapter = groupAdapter
        getToken()
        return binding.root
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            referenceStudent.child(auth.uid ?: "").child("token").setValue(token)
        })
    }

    private fun fetchStudent() {
        student = Student()
        referenceStudent.child(auth.currentUser?.uid ?: "")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    student = snapshot.getValue(Student::class.java)!!
                    val string = "Hello, " + student.name
                    (activity as MainActivity).supportActionBar?.title = string ?: ""
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun fetchGroups() {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                for (child in children) {
                    val value = child.getValue(Group::class.java)
                    value?.let { groupList.add(it) }
                }
                groupAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        var student: Student
        referenceStudent.child(auth.currentUser?.uid ?: "")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    student = snapshot.getValue(Student::class.java)!!
                    if (student.role == "admin" || student.role == "moderator") {
                        inflater.inflate(R.menu.admin_add_group, menu)
                    } else {
                        inflater.inflate(R.menu.user_group, menu)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_group_menu -> {
                findNavController().navigate(R.id.action_nav_groups_to_addGroupFragment)
            }
            R.id.sms_menu -> {
                findNavController().navigate(R.id.action_nav_groups_to_messagesFragment)
            }
            R.id.logout_group_menu -> {
                auth.signOut()
                val intent = Intent(requireActivity(), SignInActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
            R.id.logout_user_menu -> {
                auth.signOut()
                val intent = Intent(requireActivity(), SignInActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        (activity as MainActivity).hideNavigationIcon()
        super.onResume()
    }

}