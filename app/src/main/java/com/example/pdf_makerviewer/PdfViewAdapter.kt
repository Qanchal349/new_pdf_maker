package com.example.pdf_makerviewer

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

internal class PdfViewAdapter(private val renderer: PdfRendererCore) :
    RecyclerView.Adapter<PdfViewAdapter.PdfPageViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfPageViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_pdf_page, parent, false)
        return PdfPageViewHolder(v)
    }

    override fun getItemCount(): Int {
        return renderer.getPageCount()
    }

    override fun onBindViewHolder(holder: PdfPageViewHolder, position: Int) {
        holder.bind()
    }

    inner class PdfPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            with(itemView) {
                findViewById<ImageView>(R.id.pageView).setImageBitmap(null)
                renderer.renderPage(adapterPosition) { bitmap: Bitmap?, pageNo: Int ->
                    if (pageNo != adapterPosition)
                        return@renderPage
                    bitmap?.let {
                        findViewById<ImageView>(R.id.pageView).layoutParams =findViewById<ImageView>(R.id.pageView).layoutParams.apply {
                            height =
                                (findViewById<ImageView>(R.id.pageView).width.toFloat() / ((bitmap.width.toFloat() / bitmap.height.toFloat()))).toInt()
                        }
                        findViewById<ImageView>(R.id.pageView).setImageBitmap(bitmap)

//                        findViewById<ImageView>(R.id.pageView).animation = AlphaAnimation(0F, 1F).apply {
//                            interpolator = LinearInterpolator()
//                            duration = 300
//                        }
                    }

                }
            }
        }
    }

}