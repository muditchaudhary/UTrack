package edu.cuhk.csci3310.utrack

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

//Entry point login
class MainActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    val providers = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build(),
        AuthUI.IdpConfig.PhoneBuilder().build(),
        AuthUI.IdpConfig.EmailBuilder().build()
    )
    val RC_SIGN_IN = 1
    var user: FirebaseUser? = null
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        //If user is active, switch to dashboard
        if (user != null) {
            addUserAndSwitch()
        } else {    //If no user, signup/login
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                    providers
                ).setIsSmartLockEnabled(false).setLogo(R.drawable.utrack_logo).setTheme(R.style.LoginTheme).build(),
                RC_SIGN_IN
            )
        }

    }

    //Login result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                user = FirebaseAuth.getInstance().currentUser
                addUserAndSwitch()
            } else {
                Toast.makeText(this, "Login Failed. Please Try Again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Add user if not present in database and switch to dashboard
    fun addUserAndSwitch() {
        if (user != null) {
            var newUser = db.collection("users").document(user!!.uid)
            val userHash = hashMapOf(
                "uid" to user!!.uid,
                "user name" to user!!.displayName,
                "user data" to user!!.email
            )
            newUser.set(userHash, SetOptions.merge())
            var intent = Intent(this, uniViewer::class.java)
            startActivity(intent)
        }
    }
}
