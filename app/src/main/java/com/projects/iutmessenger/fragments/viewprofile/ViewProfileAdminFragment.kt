package com.projects.iutmessenger.fragments.viewprofile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.projects.iutmessenger.MainActivity
import com.projects.iutmessenger.R
import com.projects.iutmessenger.SignInActivity
import com.projects.iutmessenger.databinding.FragmentViewProfileAdminBinding
import com.projects.iutmessenger.models.Group
import com.projects.iutmessenger.models.Student
import com.romainpiel.shimmer.Shimmer
import com.squareup.picasso.Picasso

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ViewProfileAdminFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    private var _binding: FragmentViewProfileAdminBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var referenceGroup: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewProfileAdminBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        initialize()

        val bundle = arguments
        val studentID = bundle?.getString("studentID") as String
        reference.child(studentID).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                var student = snapshot.getValue(Student::class.java)!!
                binding.apply {
                    Picasso.get().load(student.imageUrl).placeholder(R.drawable.iut1)
                        .error(R.drawable.error).into(image)
                    if (student.role == "admin") {
                        usernameRainbow.visibility = View.VISIBLE
                    } else if (student.role == "moderator") {
                        val shimmer = Shimmer()
                            .setDuration(1000)
                            .setStartDelay(1000)
                            .setDirection(Shimmer.ANIMATION_DIRECTION_LTR)

                        shimmer.start(usernameShimmer)
                        usernameShimmer.visibility = View.VISIBLE
                    } else {
                        usernameSimple.visibility = View.VISIBLE
                    }
                    name.text = "Name: ${student.name}"
                    surname.text = "Surname: ${student.surname}"
                    birthdate.text = "Birth date: ${student.birthDate}"
                    telegramnickname.text = "TG: ${student.telegramNickName}"
                    emailname.text = "Email: ${student.email?.replace("@gmail.com", "")}"
                    (activity as MainActivity).supportActionBar?.title =
                        student.name + " " + student.surname
                    referenceGroup.child(student.groupID.toString() ?: "")
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
            ViewProfileAdminFragment().apply {
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
        inflater.inflate(R.menu.admin_see_profile, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.admin_edit_user -> {
                reference.child(auth.uid ?: "")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val value = snapshot.getValue(Student::class.java) as Student
                            if (value.role == "admin") {
                                findNavController().navigate(
                                    R.id.action_viewProfileAdminFragment_to_editUserFragment,
                                    arguments
                                )
                            } else {
                                findNavController().navigate(
                                    R.id.action_viewProfileAdminFragment_to_editSimpleFragment,
                                    arguments
                                )
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

            }

            R.id.delete_menu -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Deleting user!")
                builder.setMessage("Are you sure you want to delete this user?")
                builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        val bundle = arguments
                        val studentID = bundle?.getString("studentID") as String
                        reference.child(studentID).removeValue()
                        if (studentID == auth.uid) {
                            Toast.makeText(
                                requireContext(),
                                "You have been automatically logged out!",
                                Toast.LENGTH_SHORT
                            ).show()
                            auth.signOut()
                            val intent = Intent(requireActivity(), SignInActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "User has been deleted successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        }
                    }
                })
                builder.setNegativeButton("No", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {

                    }
                })
                builder.show()

                return true
            }


        }
        return super.onOptionsItemSelected(item)
    }
}