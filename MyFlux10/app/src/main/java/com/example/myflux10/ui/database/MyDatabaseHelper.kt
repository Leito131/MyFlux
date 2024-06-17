package com.example.myflux10.ui.database
import android.util.Log
import com.example.myflux10.ui.list.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference

class TaskFirestoreHelper {

    companion object {
        private const val TAG = "TaskFirestoreHelper"
    }

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private fun getUserTasksCollection(): CollectionReference? {
        val currentUserEmail = auth.currentUser?.email?.replace(".", "_")
        return currentUserEmail?.let {
            db.collection("user_tasks").document(it).collection("tasks")
        }
    }

    fun insertTask(task: Task, callback: (Boolean) -> Unit) {
        val collectionRef = getUserTasksCollection()
        if (collectionRef != null) {
            collectionRef.add(task)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener { e ->
                    callback(false)
                    Log.e(TAG, "Error adding task", e)
                }
        } else {
            callback(false)
            Log.e(TAG, "User is not authenticated")
        }
    }

    fun deleteAllTasks(callback: (() -> Unit)? = null) {
        val collectionRef = getUserTasksCollection()
        if (collectionRef != null) {
            collectionRef.get()
                .addOnSuccessListener { querySnapshot ->
                    val batch = db.batch()
                    querySnapshot.documents.forEach { document ->
                        batch.delete(document.reference)
                    }
                    batch.commit()
                        .addOnSuccessListener {
                            callback?.invoke()
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error deleting tasks", exception)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting documents", exception)
                }
        } else {
            Log.e(TAG, "User is not authenticated")
        }
    }

    fun getAllTasks(callback: (List<Task>) -> Unit) {
        val collectionRef = getUserTasksCollection()
        if (collectionRef != null) {
            collectionRef.get()
                .addOnSuccessListener { result ->
                    val tasksList = mutableListOf<Task>()
                    for (document in result) {
                        val task = document.toObject(Task::class.java)
                        task.documentId = document.id  // Set the document ID
                        tasksList.add(task)
                    }
                    callback(tasksList)
                }
                .addOnFailureListener { e ->
                    callback(emptyList())
                    Log.e(TAG, "Error getting tasks", e)
                }
        } else {
            callback(emptyList())
            Log.e(TAG, "User is not authenticated")
        }
    }

    fun updateTaskStatus(taskId: String, newStatus: String, callback: (Boolean) -> Unit) {
        val taskRef = getUserTasksCollection()?.document(taskId)
        if (taskRef != null) {
            taskRef.update("status", newStatus)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating status", e)
                    callback(false)
                }
        } else {
            callback(false)
            Log.e(TAG, "User is not authenticated")
        }
    }

    fun updateTask(task: Task, onComplete: (Boolean) -> Unit) {
        val taskRef = getUserTasksCollection()?.document(task.documentId ?: "")
        if (taskRef != null) {
            taskRef.update(
                mapOf(
                    "description" to task.description,
                    "dueDate" to task.dueDate,
                    "priority" to task.priority,
                    "status" to task.status,
                    "checked" to task.checked
                )
            ).addOnSuccessListener {
                Log.d(TAG, "Task updated with ID: ${task.documentId}")
                onComplete(true) // Signal successful update
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error updating task", e)
                onComplete(false) // Signal update failure
            }
        } else {
            onComplete(false)
            Log.e(TAG, "User is not authenticated")
        }
    }

    fun deleteTask(task: Task, onComplete: (Boolean) -> Unit) {
        val taskRef = getUserTasksCollection()?.document(task.documentId ?: "")
        if (taskRef != null) {
            taskRef.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Task deleted with ID: ${task.documentId}")
                    onComplete(true) // Signal successful deletion
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error deleting task", e)
                    onComplete(false) // Signal deletion failure
                }
        } else {
            onComplete(false)
            Log.e(TAG, "User is not authenticated")
        }
    }
}
