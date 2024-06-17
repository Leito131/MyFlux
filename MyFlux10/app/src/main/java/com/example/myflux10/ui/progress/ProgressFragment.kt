package com.example.myflux10.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myflux10.databinding.FragmentReflowBinding

class ProgressFragment : Fragment() {

    private lateinit var binding: FragmentReflowBinding
    private lateinit var viewModel: ProgressViewModel
    private lateinit var progressAdapter: ProgressAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReflowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(ProgressViewModel::class.java)

        // Initialize RecyclerView
        val spanCount = 4
        val spacing = 16
        progressAdapter = ProgressAdapter(viewModel, spanCount)

        binding.recyclerViewProgress.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = progressAdapter
            addItemDecoration(GridDividerItemDecoration(spanCount, spacing))
        }

        // Observe progress items
        viewModel.reflowItems.observe(viewLifecycleOwner, { items ->
            progressAdapter.reflowItems = items
        })

        // Fetch tasks with context
        viewModel.fetchTasks(requireContext())
    }
}


