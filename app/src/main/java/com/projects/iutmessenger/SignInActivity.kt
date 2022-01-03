package com.projects.iutmessenger

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.projects.iutmessenger.databinding.ActivitySignInBinding
import com.projects.iutmessenger.models.MessageToAdmin
import com.projects.iutmessenger.models.Student

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private val client_id: String =
        "537602585176-rs6716d54322qaq76ao34t7hec1llo0g.apps.googleusercontent.com"
    private lateinit var googleSignInClient: GoogleSignInClient
    private var RC_SIGN_IN = 0
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var referenceMessages: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private val TAG = "SignInActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseDatabase = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(client_id)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()
        binding.sign.setOnClickListener {
            signIn()
        }
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("users")
        referenceMessages = firebaseDatabase.getReference("messages")
        getToken()
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            referenceMessages.child(auth.uid.toString())
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var exists = true
                        try {
                            val value =
                                snapshot.getValue(MessageToAdmin::class.java) as MessageToAdmin
                        } catch (e: Exception) {
                            exists = false
                            Toast.makeText(this@SignInActivity, "Not exists", Toast.LENGTH_SHORT)
                                .show()
                        } finally {
                            if (exists) {
                                referenceMessages.child(auth.uid ?: "").child("senderTOKEN")
                                    .setValue(token)
                                Toast.makeText(this@SignInActivity, "exists", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        })
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            val user = auth.currentUser
            reference.child(user?.uid ?: "")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val intent = Intent(this@SignInActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            binding.apply {
                                welcomeTv.visibility = View.VISIBLE
                                progress.visibility = View.GONE
                                logo.visibility = View.VISIBLE
                                sign.visibility = View.VISIBLE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        } else {
            binding.apply {
                welcomeTv.visibility = View.VISIBLE
                progress.visibility = View.GONE
                logo.visibility = View.VISIBLE
                sign.visibility = View.VISIBLE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(ContentValues.TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    reference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var isHave = false
                            val children = snapshot.children
                            for (child in children) {
                                val value = child.getValue(Student::class.java)
                                if (value?.uid.toString() == user?.uid.toString()) {
                                    isHave = true
                                    break
                                }
                            }
                            if (isHave) {
                                val intent = Intent(this@SignInActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
                                startActivity(intent)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                } else {
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }
}