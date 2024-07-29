package com.example.testingmyapp

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testingmyapp.model.MedicineScheduleItem
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class MedicineSetupFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var medicineScheduleAdapter: MedicineScheduleAdapter
    private val medicineScheduleList = mutableListOf<MedicineScheduleItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicine_setup, container, false)

        val pickTimeText: TextView = view.findViewById(R.id.pickTimetext)
        val timeSelectLay: View = view.findViewById(R.id.timeselectlay)
        val addButton: Button = view.findViewById(R.id.addscheduletext)
        val recyclerView: RecyclerView = view.findViewById(R.id.medicine_schedule_recyclerview)

        val medicineNameEditText: TextInputEditText = view.findViewById(R.id.medsnameinput)
        val dosageEditText: TextInputEditText = view.findViewById(R.id.dosageinput)

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

            if (medicineName.isNotBlank() && dosage.isNotBlank() && time.isNotBlank()) {
                val newMedicineSchedule = MedicineScheduleItem(medicineName, dosage, time)
                medicineScheduleList.add(newMedicineSchedule)
                medicineScheduleAdapter.notifyItemInserted(medicineScheduleList.size - 1)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        medicineScheduleAdapter = MedicineScheduleAdapter(medicineScheduleList)
        recyclerView.adapter = medicineScheduleAdapter

        return view
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MedicineSetupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
