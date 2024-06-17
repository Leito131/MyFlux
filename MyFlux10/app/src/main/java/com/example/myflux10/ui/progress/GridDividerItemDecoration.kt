package com.example.myflux10.ui.progress

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridDividerItemDecoration(private val spanCount: Int, private val spacing: Int) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.TRANSPARENT // Blue color
        strokeWidth = 4f
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount
        outRect.left = spacing - column * spacing / spanCount
        outRect.right = (column + 1) * spacing / spanCount

        if (position < spanCount) {
            outRect.top = spacing
        }
        outRect.bottom = spacing
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val left = child.left - params.leftMargin
            val right = child.right + params.rightMargin
            val top = child.top - params.topMargin
            val bottom = child.bottom + params.bottomMargin

            c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }
}
