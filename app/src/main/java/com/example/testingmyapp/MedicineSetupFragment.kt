package com.example.testingmyapp

import android.app.TimePickerDialog
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

    private lateinit var medicineScheduleAdapter: MedicineAdapter
    private val medicineScheduleList = mutableListOf<MedicineScheduleItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Get the currently logged-in user's UID
        userId = auth.currentUser?.uid
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.hide()
        val view = inflater.inflate(R.layout.fragment_medicine_setup, container, false)

        val pickTimeText: TextView = view.findViewById(R.id.pickTimetext)
        val timeSelectLay: View = view.findViewById(R.id.timeselectlay)
        val addButton: Button = view.findViewById(R.id.addscheduletext)

        val recyclerView: RecyclerView = view.findViewById(R.id.medicine_schedule_recyclerview)
        val medicineNameEditText: TextInputEditText = view.findViewById(R.id.medsnameinput)
        val dosageEditText: TextInputEditText = view.findViewById(R.id.dosageinput)

        // Initialize RecyclerView and Adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        medicineScheduleAdapter = MedicineAdapter(medicineScheduleList)
        recyclerView.adapter = medicineScheduleAdapter

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

        addButton.setOnClickListener {
            val medicineName = medicineNameEditText.text.toString()
            val dosage = dosageEditText.text.toString()
            val time = pickTimeText.text.toString()

            // Get the selected mealTime from RadioGroup
            val mealTimeRadioGroup: RadioGroup = view.findViewById(R.id.radio_group)
            val selectedMealTimeId = mealTimeRadioGroup.checkedRadioButtonId
            val mealTime = if (selectedMealTimeId != -1) {
                val selectedMealTimeButton = view.findViewById<RadioButton>(selectedMealTimeId)
                selectedMealTimeButton.text.toString()
            } else {
                ""
            }

            if (medicineName.isNotBlank() && dosage.isNotBlank() && time.isNotBlank()) {
                val newMedicineSchedule = MedicineScheduleItem(medicineName, dosage, time, mealTime)

                // Log data before saving
                Log.d("MedicineSetupFragment", "Saving data: MedicineName: $medicineName, Dosage: $dosage, Time: $time, MealTime: $mealTime")

                // Check if user is authenticated and save to Firebase under the user's UID
                userId?.let { uid ->
                    val userMedicineRef = database.child("medicine_schedules").child(uid)
                    val medicineId = userMedicineRef.push().key
                    if (medicineId != null) {
                        userMedicineRef.child(medicineId).setValue(newMedicineSchedule)
                            .addOnSuccessListener {
                                Log.d("MedicineSetupFragment", "Medicine schedule saved to Realtime Database with mealTime: $mealTime")
                            }
                            .addOnFailureListener { e ->
                                Log.e("MedicineSetupFragment", "Failed to save medicine schedule in Realtime Database.", e)
                            }

                        // Update the RecyclerView
                        medicineScheduleList.add(newMedicineSchedule)
                        medicineScheduleAdapter.notifyItemInserted(medicineScheduleList.size - 1)

                        // Clear input fields
                        medicineNameEditText.text?.clear()
                        dosageEditText.text?.clear()
                        pickTimeText.text = ""
                    }
                } ?: run {
                    Log.e("MedicineSetupFragment", "User is not authenticated.")
                }
            } else {
                Log.e("MedicineSetupFragment", "Please fill in all fields.")
            }
        }


        return view
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
