package com.example.pdf_makerviewer

import android.content.ClipData
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pdf_makerviewer.model.Media
import kotlinx.android.synthetic.main.activity_select_images.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class SelectImagesActivity : AppCompatActivity() {

    var images: List<Media>? = null
    private lateinit var tracker: SelectionTracker<String>

    private val imageAdapter by lazy {
        MediaAdapter()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_images)

        println("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ ")

        val spacing = 4 / 2
        rv_images.apply {
            setHasFixedSize(true)
            setPadding(spacing, spacing, spacing, spacing)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(rect: Rect,
                                            view: View,
                                            parent: RecyclerView,
                                            state: RecyclerView.State) {
                    rect.set(spacing, spacing, spacing, spacing)
                }
            })
            layoutManager = GridLayoutManager(this@SelectImagesActivity, 3)
        }
        rv_images.adapter = imageAdapter

         runBlocking{
             val job = launch {
                 images = PdfOperations.queryImagesOnDevice(this@SelectImagesActivity,null)
             }

             job.join()
             imageAdapter.submitList(images)

          }



        tracker = SelectionTracker.Builder(
            "imagesSelection",
            rv_images,
            ImageKeyProvider(imageAdapter),
            ImageItemDetailsLookup(rv_images),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        tracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()

                    val items = tracker.selection!!.size()
                    if (items > 0) {
                       // actionMode?.title = getString(R.string.action_selected, items)
                    } else {
                       // actionMode?.finish()
                    }
                }
            })

        imageAdapter.tracker = tracker


        done.setOnClickListener {

            createBitmap()

            // navigate to detailActivity with image array

        }



    }


    private fun createBitmap() {

        val media = imageAdapter.currentList.filter {
            tracker.selection.contains("${it.id}")

        }



        var imageUri1:ArrayList<String> = ArrayList()
        for (i in media){
            imageUri1.add(i.uri.toString())
        }

        val intent = Intent(this,DetailActivity::class.java).apply{
            putStringArrayListExtra("IMAGE",imageUri1)
        }
        startActivity(intent)
             finish()
    }


}