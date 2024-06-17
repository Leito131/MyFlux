package com.example.myflux10.ui.list

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Task(
    val id: Int = 0,
    val description: String? = "",
    val dueDate: String? = "",
    val priority: String? = "",
    var status: String? = "",
    var checked: Int = 0,
    var documentId: String? = ""
)
