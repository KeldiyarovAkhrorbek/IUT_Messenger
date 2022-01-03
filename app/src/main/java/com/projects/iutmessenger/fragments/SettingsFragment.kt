package com.projects.iutmessenger.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.projects.iutmessenger.MainActivity
import com.projects.iutmessenger.SignInActivity
import com.projects.iutmessenger.databinding.FragmentSettingsBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = "My settings"
        binding.apply {
            deleteAccount.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Delete account")
                builder.setMessage("Are you sure you want to delete your account?")
                builder.setNegativeButton("No", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                    }
                })
                builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        reference.child(auth.uid ?: "").removeValue().addOnSuccessListener {
                            val intent = Intent(requireActivity(), SignInActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                            Toast.makeText(
                                requireContext(),
                                "You have been automatically logged out!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
                builder.show()

            }
        }
        initialize()
        return binding.root
    }

    private fun initialize() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.getReference("users")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        (activity as MainActivity).hideNavigationIcon()
        super.onResume()
    }

}