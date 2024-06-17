package com.example.myflux10.ui.list

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myflux10.ui.database.TaskFirestoreHelper
import com.example.myflux10.ui.list.Task

class ListViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDatabaseHelper: TaskFirestoreHelper = TaskFirestoreHelper()

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> get() = _tasks

    private val _refreshTrigger = MutableLiveData<Boolean>()
    val refreshTrigger: LiveData<Boolean> get() = _refreshTrigger

    init {
        loadTasks()
    }

    fun onTaskCheckedChanged(task: Task) {
        // Update the task in Firestore
        taskDatabaseHelper.updateTask(task) { success ->
            if (success) {
                // Optionally, trigger a refresh of the task list if needed
                refresh()
            } else {
                // Handle the update failure, e.g., show an error message
            }
        }
    }

    private fun loadTasks() {
        taskDatabaseHelper.getAllTasks { tasks: List<Task> ->
            _tasks.value = tasks
        }
    }

    fun refresh() {
        loadTasks() // Reload tasks
        _refreshTrigger.value = true // Trigger UI refresh
    }

    fun deleteCheckedTasks() {
        val tasksToDelete = tasks.value?.filter { it.checked == 1 }
        tasksToDelete?.forEach { task ->
            taskDatabaseHelper.deleteTask(task) { success ->
                if (success) {
                    Log.e("Borrado", "exitoso")
                } else {
                    // Handle deletion failure
                }
            }
        }
    }

    fun deleteAllTasks() {
        taskDatabaseHelper.deleteAllTasks()
        refresh() // Refresh the task list after deletion
    }

}


class TransformViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListViewModel(context.applicationContext as Application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}




