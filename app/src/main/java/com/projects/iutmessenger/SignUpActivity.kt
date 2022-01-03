package com.projects.iutmessenger

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.projects.iutmessenger.databinding.ActivitySignUpBinding
import com.projects.iutmessenger.databinding.DialogNotificationBinding
import com.projects.iutmessenger.models.Group
import com.projects.iutmessenger.models.MessageToAdmin
import com.projects.iutmessenger.models.Student
import com.projects.iutmessenger.notification.NotificationData
import com.projects.iutmessenger.notification.PushNotification
import com.projects.iutmessenger.notification.retrofit.RetrofitInstance
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var reference: StorageReference
    private lateinit var userReference: DatabaseReference
    private lateinit var groupReference: DatabaseReference
    private lateinit var messageReference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var groupSpinnerAdapter: ArrayAdapter<String>
    private var groupNameList = ArrayList<String>()
    private var imgUrl: String? = null
    private lateinit var student: Student
    private lateinit var groupList: ArrayList<Group>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val millis = System.currentTimeMillis()
        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference("$millis")
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        userReference = firebaseDatabase.getReference("users")
        groupReference = firebaseDatabase.getReference("groups")
        messageReference = firebaseDatabase.getReference("messages")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(binding.toolbar)
        fetchGroups()
        groupNameList = ArrayList()
        groupNameList.add("---YOUR GROUP---")
        groupSpinnerAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, groupNameList)
        binding.spinner.adapter = groupSpinnerAdapter

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
                    this@SignUpActivity,
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
                    val builder = AlertDialog.Builder(this@SignUpActivity)
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


    private fun publishUser() {
        userReference
            .child(auth.currentUser?.uid ?: "")
            .setValue(student)
        Toast.makeText(this, "Account has been successfully created!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@SignUpActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun fetchGroups() {
        groupList = ArrayList()
        groupReference.addListenerForSingleValueEvent(object : ValueEventListener {
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

    private fun formatDayMonth(text: Int): String {
        if (text.toString().length == 1) {
            return "0$text"
        }
        return text.toString()
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
        } else if (groupName == "---YOUR GROUP---") {
            Toast.makeText(this, "Choose group", Toast.LENGTH_SHORT).show()
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
        Dexter.withContext(this@SignUpActivity)
            .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    getImageContent.launch("image/*")
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri =
                            Uri.fromParts("package", this@SignUpActivity.packageName, null)
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
                    val builder = androidx.appcompat.app.AlertDialog.Builder(this@SignUpActivity)
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
                reference.putFile(it)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_register_from, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.report_menu -> {
                val builder = androidx.appcompat.app.AlertDialog.Builder(this@SignUpActivity)
                val binding: DialogNotificationBinding =
                    DialogNotificationBinding.inflate(layoutInflater)
                builder.setView(binding.root)
                val alertDialog = builder.create()


                binding.send.setOnClickListener {
                    binding.layoutGroup.error = null
                    binding.layoutGroup.isErrorEnabled = false
                    binding.layoutName.error = null
                    binding.layoutName.isErrorEnabled = false
                    val tgNick = binding.editTelegram.text.toString().trim()
                    val groupName = binding.editGroup.text.toString().trim()
                    if (tgNick.isEmpty()) {
                        YoYo.with(Techniques.Shake).duration(1000).playOn(binding.layoutName)
                        binding.layoutName.error = "Input telegram nickname!"
                        binding.editTelegram.requestFocus()
                    } else if (groupName.isEmpty()) {
                        binding.layoutGroup.error = "Input your group name!"
                        YoYo.with(Techniques.Shake).duration(1000).playOn(binding.layoutGroup)
                        binding.editGroup.requestFocus()
                    } else {
                        val message =
                            "Hi, my telegram is $tgNick , can you add group $groupName ?\nSo that I can register."
                        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val children = snapshot.children
                                for (child in children) {
                                    val student = child.getValue(Student::class.java)
                                    if (student?.role == "admin" || student?.role == "moderator") {
                                        val pushNotification = PushNotification(
                                            NotificationData(groupName, message),
                                            student.token.toString()
                                        )
                                        sendNotification(pushNotification)
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                        val time = Calendar.getInstance().time
                        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
                        val format = simpleDateFormat.format(time)
                        FirebaseMessaging.getInstance().token.addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    return@OnCompleteListener
                                }
                                val token = task.result
                                publishMessage(tgNick, message, groupName, format, auth.uid, token)
                            })

                        Toast.makeText(
                            this@SignUpActivity,
                            "Notification sent to admins!\nAdmin will contact you for more information through Telegram!",
                            Toast.LENGTH_SHORT
                        ).show()
                        alertDialog.dismiss()
                    }
                }
                binding.cancel.setOnClickListener {
                    alertDialog.dismiss()
                }
                alertDialog.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    private fun publishMessage(
        tgNick: String,
        message: String,
        groupName: String,
        format: String,
        uid: String?,
        token: String?
    ) {
        val messageToAdmin =
            MessageToAdmin(tgNick, message, groupName, format, uid, token, false, "nobody")
        messageReference.child(messageToAdmin.senderUID.toString())
            .setValue(messageToAdmin)
    }


}