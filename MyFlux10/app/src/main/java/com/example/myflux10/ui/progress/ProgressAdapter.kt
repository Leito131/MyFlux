package com.example.myflux10.ui.progress

import android.content.ClipData
import android.graphics.Color
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myflux10.R

class ProgressAdapter(
    private val viewModel: ProgressViewModel,
    private val spanCount: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_TEXT = 0
    private val VIEW_TYPE_CARD = 1
    private val VIEW_TYPE_PLACEHOLDER = 2

    private val headerTexts = listOf("NOVA", "EN PROGRESO", "EN PAUSA", "REVISIÓN")

    var reflowItems: List<ProgressItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewReflowItemStatus)
    }

    inner class PlaceholderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val view = layoutInflater.inflate(R.layout.reflow_item_text, parent, false)
                TextViewHolder(view)
            }
            VIEW_TYPE_CARD -> {
                val view = layoutInflater.inflate(R.layout.reflow_item_card, parent, false)
                CardViewHolder(view)
            }
            VIEW_TYPE_PLACEHOLDER -> {
                val view = layoutInflater.inflate(R.layout.reflow_item_placeholder, parent, false)
                PlaceholderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_TEXT -> {
                (holder as TextViewHolder).textView.text = headerTexts[position]
                // Adjust text size here if needed
                holder.textView.textSize = 14f // Example of setting the text size
            }
            VIEW_TYPE_CARD -> {
                val item = reflowItems[position - spanCount] // Offset by spanCount to account for the first row
                val priorityColor = item.color
                (holder as CardViewHolder).textView.text = item.id.toString()
                holder.itemView.setBackgroundColor(priorityColor)

                holder.itemView.setOnLongClickListener {
                    val dragData = ClipData.newPlainText("itemId", item.id.toString())
                    val dragShadowBuilder = View.DragShadowBuilder(it)
                    it.startDragAndDrop(dragData, dragShadowBuilder, item.id, 0)
                    true
                }
            }
            VIEW_TYPE_PLACEHOLDER -> {
                holder.itemView.setOnDragListener { view, event ->
                    when (event.action) {
                        DragEvent.ACTION_DRAG_STARTED -> true
                        DragEvent.ACTION_DRAG_ENTERED -> {
                            view.setBackgroundColor(Color.LTGRAY)
                            true
                        }
                        DragEvent.ACTION_DRAG_EXITED -> {
                            view.setBackgroundColor(Color.TRANSPARENT)
                            true
                        }
                        DragEvent.ACTION_DROP -> {
                            val itemIdString = event.clipData.getItemAt(0).text.toString()
                            val itemId = itemIdString.toIntOrNull()
                            if (itemId != null) {
                                val item = reflowItems.firstOrNull { it.id == itemId }
                                item?.let {
                                    val fromIndex = reflowItems.indexOfFirst { it.id == itemId }
                                    val toIndex = position - spanCount
                                    val newStatus = headerTexts[toIndex % spanCount] // Get new status based on column

                                    viewModel.updateTaskStatus(context = view.context,item.documentId.toString(), newStatus) { success ->
                                        if (success) {
                                            item.state = newStatus
                                            reflowItems = reflowItems.toMutableList().apply {
                                                removeAt(fromIndex)
                                                add(toIndex, item)
                                            }
                                            notifyDataSetChanged()
                                        } else {
                                            Log.e("ProgressAdapter", "Failed to update task status")
                                        }
                                    }
                                }
                            }
                            view.setBackgroundColor(Color.TRANSPARENT)
                            true
                        }
                        DragEvent.ACTION_DRAG_ENDED -> {
                            view.setBackgroundColor(Color.TRANSPARENT)
                            true
                        }
                        else -> false
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return spanCount + reflowItems.size // spanCount texts in the first row, plus other items
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position < spanCount -> VIEW_TYPE_TEXT
            reflowItems[position - spanCount].isPlaceholder -> VIEW_TYPE_PLACEHOLDER
            else -> VIEW_TYPE_CARD
        }
    }
}
