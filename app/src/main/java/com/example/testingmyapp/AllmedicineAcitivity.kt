package com.example.testingmyapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.*
import com.example.testingmyapp.model.MedicineScheduleItem

class AllMedicineActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var adapter: MedicineAdapter
    private val medicineList = mutableListOf<MedicineScheduleItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allmedicine_acitivity)

        val recyclerView: RecyclerView = findViewById(R.id.AllmedicineRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Pass the lambda function to handle long press event
        adapter = MedicineAdapter(medicineList) { medicineItem ->
            deleteMedicine(medicineItem)
        }
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("medicine_schedules")

        fetchUserRoleAndLoadData()
    }

    private fun deleteMedicine(medicineItem: MedicineScheduleItem) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userId = currentUser.uid

        // Get user type from Firestore
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            val userType = documentSnapshot.getString("userType")

            if (userType == "Caregiver") {
                val caregiverCode = documentSnapshot.getString("caregiverCode")
                if (caregiverCode != null) {
                    fetchLinkedAdminUser(caregiverCode) { adminId ->
                        val adminMedicineRef = database.child(adminId)
                        deleteMedicineFromAdmin(adminMedicineRef, medicineItem)
                    }
                } else {
                    Log.e("AllMedicineActivity", "Caregiver code is null for caregiver.")
                }
            }

            else if (userType == "adminly") {
                // Admin - delete from their own medicine schedule
                val adminMedicineRef = database.child(userId)
                deleteMedicineFromAdmin(adminMedicineRef, medicineItem)
            }

            else {
                // Handle deletion for other user types if needed
                Log.e("AllMedicineActivity", "User type is not caregiver.")
            }
        }.addOnFailureListener { e ->
            Log.e("AllMedicineActivity", "Failed to fetch user data.", e)
        }
    }

    private fun deleteMedicineFromAdmin(adminMedicineRef: DatabaseReference, medicineItem: MedicineScheduleItem) {
        adminMedicineRef.orderByChild("medicineName").equalTo(medicineItem.medicineName)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        dataSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                Log.d("AllMedicineActivity", "Medicine item deleted successfully.")
                                adapter.removeItem(medicineItem)
                            }
                            .addOnFailureListener { e ->
                                Log.e("AllMedicineActivity", "Failed to delete medicine item.", e)
                            }
                    }
                } else {
                    Log.e("AllMedicineActivity", "Medicine item not found for deletion.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("AllMedicineActivity", "Failed to find medicine item for deletion.", e)
            }
    }

    private fun fetchLinkedAdminUser(caregiverCode: String, callback: (String) -> Unit) {
        val usersRef = FirebaseFirestore.getInstance().collection("users")

        usersRef.whereEqualTo("caregiverCode", caregiverCode)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.e("AllMedicineActivity", "No adminly users found linked to this caregiver.")
                } else {
                    val adminId = result.documents[0].id
                    Log.d("AllMedicineActivity", "Found admin ID: $adminId")
                    callback(adminId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("AllMedicineActivity", "Failed to fetch admin data.", e)
            }
    }


    private fun fetchUserRoleAndLoadData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)

            userRef.get().addOnSuccessListener { documentSnapshot ->
                Log.d("AllMedicineActivity", "Document snapshot: $documentSnapshot")

                val userType = documentSnapshot.getString("userType")
                Log.d("AllMedicineActivity", "User type: $userType")

                when (userType) {
                    "adminly" -> {
                        // User is an adminly Person
                        Log.d("AllMedicineActivity", "User is an adminly Person")
                        fetchMedicineSchedules(currentUser.uid)
                    }
                    "Elder" -> {
                        // User is an Elder
                        val caregiverCode = documentSnapshot.getString("caregiverCode")
                        if (caregiverCode != null) {
                            Log.d("AllMedicineActivity", "Caregiver code found for Elder: $caregiverCode")
                            fetchLinkedAdminUser(caregiverCode)
                        } else {
                            Log.e("AllMedicineActivity", "Caregiver code is null for Elder.")
                        }
                    }
                    "Caregiver" -> {
                        // User is a caregiver
                        val caregiverCode = documentSnapshot.getString("caregiverCode")
                        if (caregiverCode != null) {
                            fetchLinkedAdminlyPersonUid(caregiverCode)
                        } else {
                            Log.e("AllMedicineActivity", "Caregiver code is null.")
                        }
                    }
                    else -> {
                        Log.e("AllMedicineActivity", "Unknown user type.")
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("AllMedicineActivity", "Failed to load user data from Firestore.", e)
            }
        } else {
            Log.e("AllMedicineActivity", "User is not authenticated.")
        }
    }

    private fun fetchLinkedAdminUser(caregiverCode: String) {
        val usersRef = FirebaseFirestore.getInstance().collection("users")

        usersRef.whereEqualTo("caregiverCode", caregiverCode)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.e("AllMedicineActivity", "No adminly users found linked to this caregiver.")
                } else {
                    val adminlyUid = result.documents[0].id
                    Log.d("AllMedicineActivity", "Found adminly UID: $adminlyUid")
                    fetchMedicineSchedules(adminlyUid)
                }
            }
            .addOnFailureListener { e ->
                Log.e("AllMedicineActivity", "Failed to fetch adminly person data.", e)
            }
    }

    private fun fetchLinkedAdminlyPersonUid(caregiverCode: String) {
        fetchLinkedAdminUser(caregiverCode)
    }

    private fun fetchMedicineSchedules(userId: String) {
        val userMedicineRef = database.child(userId)

        userMedicineRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                medicineList.clear() // Clear the list to ensure fresh data

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val medicineItem = dataSnapshot.getValue(MedicineScheduleItem::class.java)
                        if (medicineItem != null) {
                            medicineList.add(medicineItem)
                        }
                    }
                    Log.d("AllMedicineActivity", "Medicine schedules fetched: ${medicineList.size} items.")
                } else {
                    Log.e("AllMedicineActivity", "No medicine schedules found for user ID: $userId")
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AllMedicineActivity", "Failed to load medicine schedules.", error.toException())
            }
        })
    }
}
