package com.example.myflux10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myflux10.databinding.ActivityMainBinding
import com.example.myflux10.ui.database.TaskFirestoreHelper
import com.example.myflux10.ui.list.Task
import com.example.myflux10.ui.list.ListFragment
import com.example.myflux10.ui.list.ListViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var taskDatabaseHelper: TaskFirestoreHelper
    private lateinit var auth: FirebaseAuth
    private var taskIdCounter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        taskDatabaseHelper = TaskFirestoreHelper()
        getLastTaskId { lastTaskId ->
            taskIdCounter = lastTaskId + 1
        }

        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment?)!!
        val navController = navHostFragment.navController

        val listViewModel = ViewModelProvider(
            navHostFragment
        ).get(ListViewModel::class.java)



        binding.appBarMain.fab?.setOnClickListener { view ->
            createTaskAlertDialog(binding.activityContainer.context)
        }

        findViewById<ImageView>(R.id.delete_button)?.setOnClickListener {
            // Handle refresh button click
            showDeleteConfirmationDialog()
        }

        binding.appBarMain.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.nav_settings -> {
                    navController.navigate(R.id.nav_settings)
                    true
                }
                R.id.nav_delete -> {
                    taskDatabaseHelper.deleteAllTasks()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_content_main, ListFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        binding.navView?.let {
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_transform, R.id.nav_reflow, R.id.nav_settings
                ),
                binding.drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            it.setupWithNavController(navController)
        }

        binding.appBarMain.contentMain.bottomNavView?.let {
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_transform, R.id.nav_reflow, R.id.nav_settings
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            it.setupWithNavController(navController)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        val navView: NavigationView? = findViewById(R.id.nav_view)
        if (navView == null) {
            menuInflater.inflate(R.menu.overflow, menu)
        }
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.nav_settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun createTaskAlertDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        val customLayout = inflater.inflate(R.layout.task_alert_dialog, null, false)
        val descriptionEditText = customLayout.findViewById<EditText>(R.id.descriptionEditText)
        val dueDateEditText = customLayout.findViewById<EditText>(R.id.dueDateEditText)
        val priorityComboBox = customLayout.findViewById<Spinner>(R.id.priorityComboBox)

        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Nova tarefa")
            .setView(customLayout)
            .setPositiveButton("Crear") { _, _ ->
                val description = descriptionEditText.text.toString()
                val dueDate = dueDateEditText.text.toString()
                val priority = priorityComboBox.selectedItem as String

                val newTaskId = if (taskIdCounter == 1) 1000 else taskIdCounter
                val newTask = Task(
                    id = newTaskId,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    status = "NOVA",
                    checked = 0
                )

                taskDatabaseHelper.insertTask(newTask) { success ->
                    if (success) {
                        Toast.makeText(context, "Tarefa creada con éxito", Toast.LENGTH_LONG).show()
                        taskIdCounter += if (taskIdCounter == 1) 1000 else 1
                        updateUI()
                    } else {
                        Toast.makeText(context, "Non se puido crear a tarefa", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        alertDialog.show()
    }

    private fun getLastTaskId(callback: (Int) -> Unit) {
        taskDatabaseHelper.getAllTasks { tasks ->
            val lastTaskId = tasks.maxByOrNull { it.id }?.id ?: 0
            callback(lastTaskId)
        }
    }

    private fun updateUI() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Borrar tarefas checadas")
            .setMessage("¿Estás seguro?")
            .setPositiveButton("Si") { _, _ ->
                val listViewModel = ViewModelProvider(
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as Fragment
                ).get(ListViewModel::class.java)
                updateUI()
                listViewModel.deleteCheckedTasks()
                updateUI()
            }
            .setNegativeButton("Non", null)
            .show()
    }
}
