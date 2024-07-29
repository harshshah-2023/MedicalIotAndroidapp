package com.example.testingmyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testingmyapp.model.MedicineScheduleItem

class MedicineScheduleAdapter(private val medicineScheduleList: List<MedicineScheduleItem>) :
    RecyclerView.Adapter<MedicineScheduleAdapter.MedicineScheduleViewHolder>() {

    class MedicineScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val medicineName: TextView = view.findViewById(R.id.medicine_name)
        val dosage: TextView = view.findViewById(R.id.dosage)
        val time: TextView = view.findViewById(R.id.time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine_schedule, parent, false)
        return MedicineScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineScheduleViewHolder, position: Int) {
        val item = medicineScheduleList[position]
        holder.medicineName.text = item.medicineName
        holder.dosage.text = item.dosage
        holder.time.text = item.time
    }

    override fun getItemCount(): Int {
        return medicineScheduleList.size
    }
}
