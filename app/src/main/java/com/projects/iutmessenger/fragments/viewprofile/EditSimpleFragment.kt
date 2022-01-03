package com.projects.iutmessenger.fragments.viewprofile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.projects.iutmessenger.MainActivity
import com.projects.iutmessenger.R
import com.projects.iutmessenger.databinding.FragmentEditSimpleBinding
import com.projects.iutmessenger.models.Group
import com.projects.iutmessenger.models.Student
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class EditSimpleFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var _binding: FragmentEditSimpleBinding? = null
    private val binding get() = _binding!!
    private var imgUrl: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var referenceGroup: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var referenceStorage: StorageReference
    private lateinit var groupSpinnerAdapter: ArrayAdapter<String>
    private var groupNameList = ArrayList<String>()
    private lateinit var student: Student
    private lateinit var groupList: ArrayList<Group>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditSimpleBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        initialize()
        groupList = ArrayList()
        groupNameList.add("---GROUP---")
        fetchGroups()
        groupSpinnerAdapter =
            ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                groupNameList
            )
        binding.spinner.adapter = groupSpinnerAdapter
        val bundle = arguments
        val studentID = bundle?.getString("studentID") as String
        reference.child(studentID).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                var student = snapshot.getValue(Student::class.java)!!
                binding.apply {
                    editName.setText(student.name)
                    editSurname.setText(student.surname)
                    editTelegram.setText(student.telegramNickName)
                    Picasso.get().load(student.imageUrl).placeholder(R.drawable.iut1)
                        .error(R.drawable.error).into(userImage)
                    imgUrl = student.imageUrl
                    editDate.setText(student.birthDate)
                    referenceGroup.child(student.groupID.toString() ?: "")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val group = snapshot.getValue(Group::class.java)
                                binding.spinner.setSelection(groupNameList.indexOf(group?.groupName))
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        binding.apply {
            chooseImage.setOnClickListener {
                onResultGallery()
            }
            editDate.setOnClickListener {
                val calendar = Calendar.getInstance()
                val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
                val month1: Int = calendar.get(Calendar.MONTH)
                val year1: Int = calendar.get(Calendar.YEAR)
                val datePickerDialog = DatePickerDialog(
                    requireActivity(),
                    android.R.style.Theme_Holo_Light_Dialog,
                    { view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                        val date = "${formatDayMonth(dayOfMonth)}.${formatDayMonth(month + 1)}.${
                            formatDayMonth(
                                year
                            )
                        }"
                        binding.editDate.setText(date)
                    }, year1, month1, day
                )
                datePickerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                datePickerDialog.show()
            }
            userImage.setOnClickListener {
                onResultGallery()
            }
            submit.setOnClickListener {
                if (isValid()) {
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setTitle("!!!")
                    builder.setMessage("Are you sure all the information is correct?")
                    builder.setPositiveButton("Yes")
                    { _, _ ->
                        publishUser()
                    }
                    builder.setNegativeButton("No") { _, _ -> }
                    builder.show()
                }
            }


        }

        return binding.root
    }

    private fun publishUser() {
        val bundle = arguments
        val studentID = auth.uid.toString()
        reference.child(studentID).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                var studentSS = snapshot.getValue(Student::class.java)!!
                studentSS.name = student.name
                studentSS.surname = student.surname
                studentSS.birthDate = student.birthDate
                studentSS.telegramNickName = student.telegramNickName
                studentSS.groupID = student.groupID
                studentSS.imageUrl = student.imageUrl
                reference
                    .child(studentSS.uid ?: "")
                    .setValue(studentSS)
                Toast.makeText(
                    requireContext(),
                    "Account has been successfully changed!",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showBottomNavigation()
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun initialize() {
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("users")
        referenceGroup = firebaseDatabase.getReference("groups")
        storage = FirebaseStorage.getInstance()
        referenceStorage = storage.getReference("${System.currentTimeMillis()}")
    }

    private fun formatDayMonth(text: Int): String {
        if (text.toString().length == 1) {
            return "0$text"
        }
        return text.toString()
    }

    private fun fetchGroups() {
        groupList = ArrayList()
        referenceGroup.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                for (child in children) {
                    val value = child.getValue(Group::class.java)
                    value?.let { groupList.add(it) }
                    value?.groupName?.let { groupNameList.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun isValid(): Boolean {
        binding.layoutDate.error = null
        binding.layoutDate.isErrorEnabled = false
        binding.layoutName.error = null
        binding.layoutName.isErrorEnabled = false
        binding.layoutSurname.error = null
        binding.layoutSurname.isErrorEnabled = false
        binding.layoutTelegram.error = null
        binding.layoutTelegram.isErrorEnabled = false
        val uid = auth.currentUser?.uid
        val name = binding.editName.text.toString().trim()
        val surname = binding.editSurname.text.toString().trim()
        val date = binding.editDate.text.toString().trim()
        val telegram = binding.editTelegram.text.toString()
        val groupName = groupNameList[binding.spinner.selectedItemPosition]
        if (imgUrl == null) {
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.userImage)
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.chooseImage)
            return false
        } else if (name.isEmpty()) {
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.layoutName)
            binding.layoutName.error = "Input name!"
            binding.editName.requestFocus()
            return false
        } else if (surname.isEmpty()) {
            binding.layoutSurname.error = "Input surname!"
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.layoutSurname)
            binding.editSurname.requestFocus()
            return false
        } else if (date.isEmpty()) {
            binding.layoutDate.error = "Input date!"
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.layoutDate)
            binding.editDate.requestFocus()
            return false
        } else if (telegram.isEmpty() || telegram == "@") {
            binding.layoutTelegram.error = "Input telegram nickname!"
            binding.editTelegram.requestFocus()
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.layoutTelegram)
            return false
        } else if (groupName == "---GROUP---") {
            Toast.makeText(requireContext(), "Choose group", Toast.LENGTH_SHORT).show()
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.layoutSpinner)
            return false
        } else {
            val groupId = groupList[binding.spinner.selectedItemPosition - 1].groupID
            student = Student(
                uid,
                date,
                groupId,
                imgUrl,
                name,
                surname,
                telegram,
                "user",
                "token",
                auth.currentUser?.email
            )
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
                referenceStorage.putFile(it)
                    .addOnSuccessListener { result ->
                        result.task.addOnSuccessListener {
                            val downloadUrl = result.metadata?.reference?.downloadUrl
                            downloadUrl?.addOnSuccessListener { uri ->
                                imgUrl = uri.toString()
                                Picasso.get().load(imgUrl).resize(200, 200)
                                    .error(R.drawable.error)
                                    .into(binding.userImage)
                                binding.progress.visibility = View.GONE
                                binding.nested.alpha = 1F
                            }
                        }
                    }.addOnProgressListener { result ->
                        binding.progress.visibility = View.VISIBLE
                        binding.nested.alpha = 0.3F
                    }
            }
        }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditUserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}