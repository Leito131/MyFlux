package com.example.myflux10.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myflux10.R
import com.example.myflux10.databinding.FragmentTransformBinding
import com.example.myflux10.databinding.ItemTransformBinding

class ListFragment : Fragment() {

    private var _binding: FragmentTransformBinding? = null
    private val binding get() = _binding!!
    lateinit var listViewModel: ListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransformBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val factory = TransformViewModelFactory(requireContext())
        listViewModel = ViewModelProvider(this, factory).get(ListViewModel::class.java)
        val recyclerView = binding.recyclerviewTransform
        val adapter = TransformAdapter()

        recyclerView.adapter = adapter
        listViewModel.tasks.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        listViewModel.refreshTrigger.observe(viewLifecycleOwner) {
            if (it) {
                adapter.notifyDataSetChanged()
            }
        }

        listViewModel.refresh()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TransformAdapter : ListAdapter<Task, ListViewHolder>(ListViewHolder.DiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
            val binding =
                ItemTransformBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ListViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
            val task = getItem(position)
            holder.bind(task)
        }
    }

    class ListViewHolder(private val binding: ItemTransformBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val colorView: View = binding.colorView!!
        private val idTextView: TextView = binding.IDTextView!!
        private val descriptionTextView: TextView = binding.descriptionTextView!!
        private val dueDateTextView: TextView = binding.dueDateTextView!!
        private val checkBox: CheckBox = binding.checkBox!!

        fun bind(task: Task) {
            idTextView.text = task.id.toString()
            descriptionTextView.text = task.description
            dueDateTextView.text = "Data límite: ${task.dueDate}"
            checkBox.isChecked = task.checked == 1

            colorView.setBackgroundColor(
                ContextCompat.getColor(
                    colorView.context,
                    when (task.priority) {
                        "ALTA" -> R.color.colorHighPriority
                        "MEDIA" -> R.color.colorMediumPriority
                        "BAIXA" -> R.color.colorLowPriority
                        else -> android.R.color.transparent
                    }
                )
            )

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                task.checked = if (isChecked) 1 else 0
                val viewModel = ViewModelProvider(binding.root.findNavController().getBackStackEntry(R.id.nav_transform))
                    .get(ListViewModel::class.java)
                viewModel.onTaskCheckedChanged(task)
            }

        }

    /*    init {
            checkBox.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val adapter = binding.root.rootView.findViewById<RecyclerView>(R.id.recyclerview_transform).adapter as? TransformAdapter
                    val task = adapter?.currentList?.get(position)
                    if (task != null) {
                        task.checked = if (task.checked == 0) 1 else 0

                        val navController = binding.root.findNavController()
                        val currentFragment = navController.getBackStackEntry(R.id.nav_transform)
                            ?.savedStateHandle?.get<ListFragment>(Fragment::class.java.name)

                        (currentFragment as? ListFragment)?.listViewModel?.onTaskCheckedChanged(task)
                    }
                }
            }
        }*/

        class DiffCallback : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem
            }
        }
    }
}
