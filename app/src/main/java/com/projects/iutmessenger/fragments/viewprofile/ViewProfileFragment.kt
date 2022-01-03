package com.projects.iutmessenger.fragments.viewprofile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.projects.iutmessenger.MainActivity
import com.projects.iutmessenger.R
import com.projects.iutmessenger.databinding.FragmentViewProfileBinding
import com.projects.iutmessenger.models.Group
import com.projects.iutmessenger.models.Student
import com.romainpiel.shimmer.Shimmer
import com.squareup.picasso.Picasso

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ViewProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var _binding: FragmentViewProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var referenceGroup: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewProfileBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        initialize()

        val bundle = arguments
        val studentID = bundle?.getString("studentID") as String
        reference.child(studentID).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                val student = snapshot.getValue(Student::class.java)!!
                binding.apply {
                    Picasso.get().load(student.imageUrl).placeholder(R.drawable.iut1)
                        .error(R.drawable.error).into(image)
                    when (student.role) {
                        "admin" -> {
                            usernameRainbow.visibility = View.VISIBLE
                        }
                        "moderator" -> {
                            val shimmer = Shimmer()
                                .setDuration(1000)
                                .setStartDelay(1000)
                                .setDirection(Shimmer.ANIMATION_DIRECTION_LTR)
                            shimmer.start(usernameShimmer)
                            usernameShimmer.visibility = View.VISIBLE
                        }
                        else -> {
                            usernameSimple.visibility = View.VISIBLE
                        }
                    }
                    name.text = "Name: ${student.name}"
                    surname.text = "Surname: ${student.surname}"
                    birthdate.text = "Birth date: ${student.birthDate}"
                    telegramnickname.text = "TG: ${student.telegramNickName}"
                    emailname.text = "Email: ${student.email?.replace("@gmail.com", "")}"
                    (activity as MainActivity).supportActionBar?.title =
                        student.name + " " + student.surname
                    referenceGroup.child(student.groupID.toString())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val group = snapshot.getValue(Group::class.java)
                                groupnickname.text = "Group: " + group?.groupName
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


        return binding.root
    }

    private fun initialize() {
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("users")
        referenceGroup = firebaseDatabase.getReference("groups")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showBottomNavigation()
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        val bundle = arguments
        val studentID = bundle?.getString("studentID") as String
        if (auth.uid == studentID) {
            inflater.inflate(R.menu.menu_edit, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_self_user -> {
                findNavController().navigate(
                    R.id.action_viewProfileAdminFragment_to_editSimpleFragment,
                    arguments
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }
}