package com.projects.iutmessenger.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.projects.iutmessenger.MainActivity
import com.projects.iutmessenger.R
import com.projects.iutmessenger.databinding.FragmentAddGroupBinding
import com.projects.iutmessenger.models.Group
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddGroupFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var _binding: FragmentAddGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private var imgUrl: String? = null
    private lateinit var group: Group

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddGroupBinding.inflate(inflater, container, false)
        initialize()
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = "Add group"
        binding.apply {
            chooseImage.setOnClickListener {
                onResultGallery()
            }

            submit.setOnClickListener {
                if (isValid()) {
                    reference.child(group.groupID.toString())
                        .setValue(group).addOnCompleteListener {
                            Toast.makeText(
                                requireContext(),
                                "Group has been added!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            findNavController().popBackStack()
                        }

                }
            }
        }


        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun isValid(): Boolean {
        binding.layoutName.error = null
        binding.layoutName.isErrorEnabled = false
        val name = binding.editName.text.toString().trim()
        if (imgUrl == null) {
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.userImage)
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.chooseImage)
            return false
        } else if (name.isEmpty()) {
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.layoutName)
            binding.layoutName.error = "Input name!"
            binding.editName.requestFocus()
            return false
        } else {
            val time = Calendar.getInstance().time
            val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
            val format = simpleDateFormat.format(time)
            val millis = System.currentTimeMillis()
            val key = reference.push().key
            group = Group(millis, name, imgUrl, format)
            return true
        }
    }

    private fun onResultGallery() {
        Dexter.withContext(requireActivity())
            .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    getImageContent.launch("image/*")
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri =
                            Uri.fromParts("package", requireActivity().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } else {
                        response.requestedPermission
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: com.karumi.dexter.listener.PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
                    builder.setTitle("Gallery permission!")
                    builder.setMessage("In order to use this app, you should allow GALLERY PERMISSION!")
                    builder.setPositiveButton("Allow!")
                    { _, _ ->
                        p1?.continuePermissionRequest()
                    }
                    builder.setNegativeButton("Don't ask again!") { _, _ -> p1?.cancelPermissionRequest() }
                    builder.show()

                }

            }).check()
    }

    private var getImageContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                storageReference.putFile(it)
                    .addOnSuccessListener { result ->
                        result.task.addOnSuccessListener {
                            val downloadUrl = result.metadata?.reference?.downloadUrl
                            downloadUrl?.addOnSuccessListener { uri ->
                                imgUrl = uri.toString()
                                Picasso.get().load(imgUrl).resize(200, 200)
                                    .error(R.drawable.error)
                                    .into(binding.userImage)
                            }
                        }
                    }
            }
        }


    private fun initialize() {
        database = FirebaseDatabase.getInstance()
        reference = database.getReference("groups")
        storage = FirebaseStorage.getInstance()
        val s = System.currentTimeMillis()
        storageReference = storage.getReference(s.toString())
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddGroupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showNavigationIcon()
    }
}