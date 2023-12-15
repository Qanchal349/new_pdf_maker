package com.example.pdf_makerviewer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.example.pdf_makerviewer.databinding.PdfListItemBinding
import com.example.pdf_makerviewer.model.PDFData


class PDFListAdapter(
    val requireContext: Context,
    private val  list: ArrayList<PDFData>,
    private val listener: OnItemClickListener,
    private val listener2: OnItemLongClickListener
) : RecyclerView.Adapter<PDFListAdapter.PdfViewHolder>() {



   inner  class PdfViewHolder(val binding: PdfListItemBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener,View.OnLongClickListener {

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }

       override fun onLongClick(v: View?): Boolean {
           val position = adapterPosition
           if (position != RecyclerView.NO_POSITION) {
               listener.onItemClick(position)
           }; return true
       }


   }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        return PdfViewHolder(
            PdfListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val data = list[position]
        holder.binding.name.text = "currently"
    }

    override fun getItemCount() = list.size

    interface OnItemClickListener {
        fun onItemClick(position: Int)

    }


     fun updateList(newList: ArrayList<PDFData>){
         list.clear()
         list.addAll(newList)

     }


    interface OnItemLongClickListener{
        fun OnLongItemClickListener(position: Int)
    }

}
