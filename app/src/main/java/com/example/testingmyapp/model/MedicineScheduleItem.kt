package com.example.testingmyapp.model

data class MedicineScheduleItem(
    var medicineName: String? = null,
    var dosage: String? = null,
    var time: String? = null,
    var mealTime: String? = null,
    var days: List<String>? = listOf()  // Ensure this is a List<String>
) {
    // No-argument constructor needed by Firebase
    constructor() : this(null, null, null, null, null)
}
