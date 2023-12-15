package com.example.pdf_makerviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import com.example.pdf_makerviewer.model.Image
import kotlinx.android.synthetic.main.pdf_list_item.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DetailActivity : AppCompatActivity() {

    companion object{
        var imageList:ArrayList<Image> = ArrayList()
    }


    lateinit var doc:PdfDocument

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail2)

         val btn = findViewById<Button>(R.id.button)
         val open = findViewById<Button>(R.id.open)


        val intent = intent
        val  images = intent.getStringArrayListExtra("IMAGE")

        println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG  $images")


        if (images != null) {
            for (i in images ){
               val bitmap = getBitmapFromUri(Uri.parse(i))

                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                val inputStream = i?.let { contentResolver.openInputStream(Uri.parse(it)) }
                BitmapFactory.decodeStream(inputStream, null, options)
                val width = options.outWidth
                val height = options.outHeight

                val size = width*height
                if(size<=2527200){
                    val image = Image(bitmap,360000)
                    imageList.add(image)
                }else{
                    val image = Image(bitmap,990000)
                    imageList.add(image)
                }

            }

            if(imageList.size >0){
                btn.visibility= View.VISIBLE
                btn.setOnClickListener {

                    runBlocking {

                        val job = launch {
                          doc = PdfOperations.createPdf(imageList)
                        }
                        job.join()
                        if(doc!=null){
                           PdfOperations.savePdf(this@DetailActivity,doc)

                       }
                       open.visibility=View.VISIBLE

                   }

                }
            }

        }


    }



    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getBitmapFromUri(imageUri: Uri?):Bitmap {
        val stream = imageUri?.let { contentResolver.openInputStream(it) }
        var bitmap = BitmapFactory.decodeStream(stream)
         // bitmap = compressImage().compressImage(this,imageUri)
        return bitmap!!
    }

}


