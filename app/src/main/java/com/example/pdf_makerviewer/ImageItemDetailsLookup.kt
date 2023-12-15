package com.example.pdf_makerviewer

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.selection.ItemDetailsLookup

class ImageItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {

    override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as MediaAdapter.MainViewHolder).getImageDetails()
        }
        return null
    }


}