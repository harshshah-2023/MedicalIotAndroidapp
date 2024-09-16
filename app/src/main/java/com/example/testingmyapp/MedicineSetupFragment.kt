package com.example.testingmyapp

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testingmyapp.model.MedicineScheduleItem
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MedicineSetupFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null
    private var userType: String? = null

    private lateinit var medicineScheduleAdapter: MedicineAdapter
    private val medicineScheduleList = mutableListOf<MedicineScheduleItem>()

    // Hardcoded admin UID
    private val adminUid = "XDqvL91fvVT4LT0cf5LphpauYxt2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Get the currently logged-in user's UID
        userId = auth.currentUser?.uid

        // Fetch the user's role
        fetchUserRole()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.hide()

        val view = inflater.inflate(R.layout.fragment_medicine_setup, container, false)

        // Initialize views
        val pickTimeText: TextView = view.findViewById(R.id.pickTimetext)
        val timeSelectLay: View = view.findViewById(R.id.timeselectlay)
        val addButton: Button = view.findViewById(R.id.addscheduletext)
        val recyclerView: RecyclerView = view.findViewById(R.id.medicine_schedule_recyclerview)
        val medicineNameEditText: TextInputEditText = view.findViewById(R.id.medsnameinput)
        val dosageEditText: TextInputEditText = view.findViewById(R.id.dosageinput)
        val chipGroup: ChipGroup = view.findViewById(R.id.chipgroup)
        val mealTimeRadioGroup: RadioGroup = view.findViewById(R.id.radio_group)

        val AllmedicineTextView = view.findViewById<TextView>(R.id.Allmedicine_textview)
        AllmedicineTextView.setOnClickListener {
            val intent = Intent(activity, AllMedicineActivity::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView and Adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        medicineScheduleAdapter = MedicineAdapter(medicineScheduleList)
        recyclerView.adapter = medicineScheduleAdapter

        // Time picker for selecting medicine time
        timeSelectLay.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                requireContext(), { _, selectedHour, selectedMinute ->
                    pickTimeText.text = String.format("%02d:%02d", selectedHour, selectedMinute)
                }, hour, minute, true
            )
            timePickerDialog.show()
        }

        // Add schedule button click listener
        // ...

// Add schedule button click listener
        addButton.setOnClickListener {
            if (userType == "adminly" || userType == "Caregiver") {
                val medicineName = medicineNameEditText.text.toString().trim()
                val dosage = dosageEditText.text.toString().trim()
                val time = pickTimeText.text.toString().trim()

                val selectedMealTimeId = mealTimeRadioGroup.checkedRadioButtonId
                val mealTime = if (selectedMealTimeId != -1) {
                    val selectedMealTimeButton = view.findViewById<RadioButton>(selectedMealTimeId)
                    selectedMealTimeButton.text.toString()
                } else {
                    ""
                }

                val selectedDays = mutableListOf<String>()
                for (i in 0 until chipGroup.childCount) {
                    val chip = chipGroup.getChildAt(i) as Chip
                    if (chip.isChecked) {
                        selectedDays.add(chip.text.toString())
                    }
                }

                if (medicineName.isNotBlank() && dosage.isNotBlank() && time.isNotBlank()) {
                    val newMedicineSchedule = MedicineScheduleItem(
                        medicineName = medicineName,
                        dosage = dosage,
                        time = time,
                        mealTime = mealTime,
                        days = selectedDays
                    )

                    Log.d("MedicineSetupFragment", "Saving data: MedicineName: $medicineName, Dosage: $dosage, Time: $time, MealTime: $mealTime, Days: ${selectedDays.joinToString()}")

                    // Save medicine schedule to Realtime Database under admin's section
                    val adminMedicineRef = database.child("medicine_schedules").child(adminUid)
                    val medicineId = adminMedicineRef.push().key
                    if (medicineId != null) {
                        adminMedicineRef.child(medicineId).setValue(newMedicineSchedule)
                            .addOnSuccessListener {
                                Log.d("MedicineSetupFragment", "Medicine schedule saved to Realtime Database")
                            }
                            .addOnFailureListener { e ->
                                Log.e("MedicineSetupFragment", "Failed to save medicine schedule.", e)
                            }

                        // Add medicine schedule to adapter
                        medicineScheduleList.add(newMedicineSchedule)
                        medicineScheduleAdapter.notifyItemInserted(medicineScheduleList.size - 1)

                        medicineNameEditText.text?.clear()
                        dosageEditText.text?.clear()
                        pickTimeText.text = ""
                    }
                } else {
                    Log.e("MedicineSetupFragment", "Please fill in all fields.")
                }
            } else {
                Log.e("MedicineSetupFragment", "You do not have permission to add medicine schedules.")
            }
        }

// ...
        return view
    }

    private fun fetchUserRole() {
        userId?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    userType = documentSnapshot.getString("userType")
                    Log.d("MedicineSetupFragment", "User type: $userType")
                }
                .addOnFailureListener { e ->
                    Log.e("MedicineSetupFragment", "Failed to fetch user role.", e)
                }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MedicineSetupFragment().apply {
                arguments = Bundle().apply {
                    putString("param1", param1)
                    putString("param2", param2)
                }
            }
    }
}
