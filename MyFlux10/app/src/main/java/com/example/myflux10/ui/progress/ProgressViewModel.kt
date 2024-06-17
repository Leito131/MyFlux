package com.example.myflux10.ui.progress

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myflux10.R
import com.example.myflux10.ui.database.TaskFirestoreHelper

class ProgressViewModel : ViewModel() {

    private val _reflowItems = MutableLiveData<List<ProgressItem>>()
    val reflowItems: LiveData<List<ProgressItem>> get() = _reflowItems

    private val taskFirestoreHelper = TaskFirestoreHelper()

    // Removed the init block as we will call fetchTasks with context later
    fun fetchTasks(context: Context) {
        taskFirestoreHelper.getAllTasks { tasks ->
            val itemsWithPlaceholders = mutableListOf<ProgressItem>()

            tasks.forEach { task ->
                val color = when (task.priority) {
                    "ALTA" -> ContextCompat.getColor(context, R.color.colorHighPriority)
                    "MEDIA" -> ContextCompat.getColor(context, R.color.colorMediumPriority)
                    "BAIXA" -> ContextCompat.getColor(context, R.color.colorLowPriority)
                    else -> Color.TRANSPARENT
                }

                if (task.status == "NOVA"){

                    itemsWithPlaceholders.add(ProgressItem(task.id, task.status, task.documentId, color, false))

                    itemsWithPlaceholders.add(ProgressItem(task.id * 100, "EN PROGRESO", "", Color.TRANSPARENT, true))

                    itemsWithPlaceholders.add(ProgressItem(task.id * 100, "EN PAUSA", "", Color.TRANSPARENT, true))

                    itemsWithPlaceholders.add(ProgressItem(task.id * 100, "FINALIZADA", "", Color.TRANSPARENT, true))



                } else if (task.status == "EN PROGRESO"){

                    itemsWithPlaceholders.add(ProgressItem(task.id * 100, "NOVA", "", Color.TRANSPARENT, true))

                    itemsWithPlaceholders.add(ProgressItem(task.id, task.status, task.documentId, color, false))

                    itemsWithPlaceholders.add(ProgressItem(task.id * 100, "EN PAUSA", "", Color.TRANSPARENT, true))

                    itemsWithPlaceholders.add(ProgressItem(task.id * 100, "FINALIZADA", "", Color.TRANSPARENT, true))

                } else if (task.status == "EN PAUSA"){

                    itemsWithPlaceholders.add(ProgressItem(task.id * 100, "NOVA", "", Color.TRANSPARENT, true))

                    itemsWithPlaceholders.add(ProgressItem(task.id * 100, "EN PROGRESO", "", Color.TRANSPARENT, true))

                    itemsWithPlaceholders.add(ProgressItem(task.id, task.status, task.documentId, color, false))

                    itemsWithPlaceholders.add(ProgressItem(task.id * 100, "FINALIZADA", "", Color.TRANSPARENT, true))

                } else if (task.status == "FINALIZADA"){

                    itemsWithPlaceholders.add(ProgressItem(task.id * 100, "NOVA", "", Color.TRANSPARENT, true))

                    itemsWithPlaceholders.add(ProgressItem(task.id * 100, "EN PROGRESO", "", Color.TRANSPARENT, true))

                    itemsWithPlaceholders.add(ProgressItem(task.id * 100, "EN PAUSA", "", Color.TRANSPARENT, true))

                    itemsWithPlaceholders.add(ProgressItem(task.id, task.status, task.documentId, color, false))

                }

            }

            _reflowItems.postValue(itemsWithPlaceholders)
        }
    }

    fun updateTaskStatus(context: Context, taskId: String, newStatus: String, callback: (Boolean) -> Unit) {
        taskFirestoreHelper.updateTaskStatus(taskId, newStatus) { success ->
            if (success) {
                fetchTasks(context) // Refresh tasks after update
            }
            callback(success)
        }
    }

}


