package com.example.testingmyapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var caregiverCodeEditText: EditText
    private lateinit var nextButton: Button
    private lateinit var userTypeRadioGroup: RadioGroup
    private var selectedUserType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge experience
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_registration)

        // Apply window insets to adjust padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Reference UI elements
        nameEditText = findViewById(R.id.name)
        emailEditText = findViewById(R.id.regEmail)
        passwordEditText = findViewById(R.id.regPass)
        caregiverCodeEditText = findViewById(R.id.caregiverCodeEditText)
        nextButton = findViewById(R.id.regButtonTohome)
        userTypeRadioGroup = findViewById(R.id.userTypeRadioGroup)

        // Handle RadioGroup selection
        userTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedUserType = when (checkedId) {
                R.id.caregiverRadioButton -> "Caregiver"
                R.id.elderlyRadioButton -> "Elderly"
                else -> null
            }

            // Show or hide the caregiver code field based on selection
            caregiverCodeEditText.visibility = if (selectedUserType == "Caregiver") {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        // Handle sign-up
        nextButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()
            val caregiverCode = caregiverCodeEditText.text.toString().trim()

            // Input validation
            if (TextUtils.isEmpty(email)) {
                emailEditText.error = "Email is required"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.error = "Password is required"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(name)) {
                nameEditText.error = "Name is required"
                return@setOnClickListener
            }

            if (selectedUserType == null) {
                Toast.makeText(this, "Please select a user type.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedUserType == "Caregiver" && TextUtils.isEmpty(caregiverCode)) {
                caregiverCodeEditText.error = "Caregiver code is required"
                return@setOnClickListener
            }

            // Log the registration attempt
            Log.d("Registration", "Attempting to register user with email: $email as $selectedUserType")

            // Create user with Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        Log.d("Registration", "User created successfully with UID: $userId")

                        if (userId != null) {
                            if (selectedUserType == "Elderly") {
                                createElderlyUser(userId, email, name)
                            } else {
                                linkCaregiverToElderly(caregiverCode, userId)
                            }
                        } else {
                            Log.e("Registration", "User ID is null after creation.")
                        }
                    } else {
                        handleRegistrationError(task.exception)
                    }
                }
        }

        findViewById<TextView>(R.id.loginRedirectText).setOnClickListener {
            goToLogin()
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Function to generate a unique code
    private fun generateUniqueCode(): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { characters.random() }
            .joinToString("")
    }

    private fun createElderlyUser(userId: String, email: String, name: String) {
        val elderlyCode = generateUniqueCode()
        val medicineScheduleId = firestore.collection("medicine_schedules").document().id

        val elderlyUser = hashMapOf(
            "name" to name,
            "email" to email,
            "userType" to "Elderly",
            "caregiverCode" to elderlyCode,
            "medicineScheduleId" to medicineScheduleId,
            "caregivers" to emptyList<String>()
        )

        firestore.collection("users").document(userId).set(elderlyUser)
            .addOnSuccessListener {
                Log.d("Firestore", "Elderly account created and stored successfully.")
                Toast.makeText(
                    baseContext,
                    "Elderly account created successfully.",
                    Toast.LENGTH_SHORT
                ).show()

                // Navigate to HomeActivity or other fragment
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("fragment", "HomeeFragment")
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to store elderly user details.", e)
                Toast.makeText(
                    baseContext,
                    "Failed to store elderly user details.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun linkCaregiverToElderly(caregiverCode: String, caregiverUid: String) {
        Log.d("CaregiverLink", "Attempting to link caregiver with code: $caregiverCode")

        firestore.collection("users")
            .whereEqualTo("caregiverCode", caregiverCode)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.e("CaregiverLink", "Invalid caregiver code: No elderly user found.")
                    Toast.makeText(
                        baseContext,
                        "Invalid caregiver code.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }

                val elderlyUserId = result.documents[0].id
                val elderlyUser = result.documents[0].data

                Log.d("CaregiverLink", "Found elderly user with UID: $elderlyUserId")

                // Create caregiver user
                val caregiverUser = hashMapOf(
                    "name" to nameEditText.text.toString().trim(),
                    "email" to emailEditText.text.toString().trim(),
                    "userType" to "Caregiver",
                    "caregiverCode" to caregiverCode,
                    "medicineScheduleId" to elderlyUser?.get("medicineScheduleId") as String?,
                    "caregivers" to emptyList<String>()
                )

                firestore.collection("users").document(caregiverUid).set(caregiverUser)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Caregiver account created and stored successfully.")
                        Toast.makeText(
                            baseContext,
                            "Caregiver account created successfully.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Update elderly user's caregivers list and navigate
                        updateElderlyCaregivers(elderlyUserId, caregiverUid)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreUpdate", "Failed to store caregiver details.", e)
                        Toast.makeText(
                            baseContext,
                            "Failed to store caregiver details.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("CaregiverLink", "Failed to fetch elderly user.", e)
                Toast.makeText(
                    baseContext,
                    "Failed to verify caregiver code.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateElderlyCaregivers(elderlyUserId: String, caregiverUid: String) {
        val caregiverMap = mapOf(caregiverUid to true)

        // Update the caregivers map under the elderlyUserId
        firestore.collection("users").document(elderlyUserId)
            .update("caregivers.$caregiverUid", true) // Adding the caregiver UID as a key with value true
            .addOnSuccessListener {
                Log.d("Firestore", "Elderly user's caregivers list updated successfully.")

                // Navigate to HomeActivity or other fragment
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("fragment", "HomeeFragment")
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUpdate", "Failed to update elderly user's caregivers list.", e)
                Toast.makeText(
                    baseContext,
                    "Failed to update elderly user's caregivers list.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun handleRegistrationError(exception: Exception?) {
        Log.e("Registration", "User registration failed.", exception)

        if (exception is FirebaseAuthUserCollisionException) {
            Toast.makeText(
                baseContext,
                "This email is already registered.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                baseContext,
                "Registration failed. Please try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
