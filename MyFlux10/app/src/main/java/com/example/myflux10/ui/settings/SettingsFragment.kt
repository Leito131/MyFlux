package com.example.myflux10.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myflux10.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize UI elements
        val textView: TextView = binding.textSettings
        val checkboxOption1: CheckBox = binding.checkboxOption1
        val checkboxOption2: CheckBox = binding.checkboxOption2
        val buttonSave: Button = binding.buttonSave

        // Observe ViewModel data
        settingsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        settingsViewModel.enableNotifications.observe(viewLifecycleOwner) { isChecked ->
            checkboxOption1.isChecked = isChecked
        }

        settingsViewModel.enableDarkMode.observe(viewLifecycleOwner) { isChecked ->
            checkboxOption2.isChecked = isChecked
        }

        // Set up save button click listener
        buttonSave.setOnClickListener {
            settingsViewModel.setEnableNotifications(checkboxOption1.isChecked)
            settingsViewModel.setEnableDarkMode(checkboxOption2.isChecked)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
