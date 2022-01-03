package com.projects.iutmessenger.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.projects.iutmessenger.MainActivity
import com.projects.iutmessenger.R
import com.projects.iutmessenger.adapter.StudentAdapter
import com.projects.iutmessenger.databinding.FragmentInsideGroupBinding
import com.projects.iutmessenger.models.Group
import com.projects.iutmessenger.models.Student

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class InsideGroupFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        (activity as MainActivity).hideBottomNavigation()
        (activity as MainActivity).showNavigationIcon()
        super.onResume()
    }

    override fun onPause() {
        (activity as MainActivity).showBottomNavigation()
        super.onPause()
    }

    private var _binding: FragmentInsideGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var referenceGroup: DatabaseReference
    private lateinit var referenceUser: DatabaseReference
    private var studentList = ArrayList<Student>()
    private lateinit var studentAdapter: StudentAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsideGroupBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        initialize()
        studentAdapter = StudentAdapter(studentList, object : StudentAdapter.OnItemClickListener {
            override fun onItemGroup(student: Student) {
                var student1: Student
                referenceUser.child(auth.currentUser?.uid ?: "")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            student1 = snapshot.getValue(Student::class.java)!!
                            if (student1.role == "admin" || student1.uid == student.uid) {
                                val bundle = Bundle()
                                bundle.putString("studentID", student.uid)
                                findNavController().navigate(
                                    R.id.action_insideGroupFragment2_to_viewProfileAdminFragment,
                                    bundle
                                )
                            } else {
                                val bundle = Bundle()
                                bundle.putString("studentID", student.uid)
                                findNavController().navigate(
                                    R.id.action_insideGroupFragment2_to_viewProfileFragment,
                                    bundle
                                )
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
            }

        })
        binding.rv.adapter = studentAdapter
        val bundle = arguments
        fetchStudents(bundle!!.getLong("groupID"))
        fetchGroup()
        return binding.root
    }

    private fun initialize() {
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        referenceGroup = firebaseDatabase.getReference("groups")
        referenceUser = firebaseDatabase.getReference("users")
    }

    private fun fetchStudents(groupID: Long) {
        studentList.clear()
        referenceUser.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                for (child in children) {
                    val value = child.getValue(Student::class.java)
                    if (value?.groupID == groupID) {
                        studentList.add(value)
                    }
                }
                if (studentList.size != 0) {
                    binding.sadImg.visibility = View.GONE
                } else {
                    binding.rv.visibility = View.GONE
                    binding.sadImg.visibility = View.VISIBLE
                }
                studentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InsideGroupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        var student: Student
        referenceUser.child(auth.currentUser?.uid ?: "")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bundle = arguments
                    val uidGroup = bundle?.getLong("groupID")
                    student = snapshot.getValue(Student::class.java)!!
                    if (student.role == "admin" || student.role == "moderator" || uidGroup == student.groupID) {
                        inflater.inflate(R.menu.inside_group_menu, menu)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun fetchGroup() {
        val bundle = arguments
        val uidGroup = bundle?.getLong("groupID")
        referenceGroup.child(uidGroup.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Group::class.java)
                    if (value != null) {
                        (activity as MainActivity).supportActionBar?.title = value.groupName
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.inside_group -> {
                val bundle = arguments
                findNavController().navigate(
                    R.id.action_insideGroupFragment2_to_groupChatFragment,
                    bundle
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}