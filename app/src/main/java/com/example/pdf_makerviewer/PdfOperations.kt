package com.example.pdf_makerviewer


import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pdf_makerviewer.model.Media
import com.example.pdf_makerviewer.model.PDFData
import com.example.pdf_makerviewer.utils.Utils.hasSdkHigherThan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

object PdfOperations {

 lateinit var document: PdfDocument

 // query for pdf

 @RequiresApi(Build.VERSION_CODES.Q)
 @SuppressLint("Range")
  fun queryForPDF(context: Context): LiveData<List<PDFData>>{
  val pdfs = MutableLiveData<List<PDFData>>()
  val allpdfs = mutableListOf<PDFData>()

  CoroutineScope(IO).launch{

   val projection = arrayOf(
    MediaStore.Downloads._ID,
    MediaStore.Downloads.RELATIVE_PATH,
    MediaStore.Downloads.DISPLAY_NAME,
     MediaStore.Downloads.SIZE,
    MediaStore.Downloads.MIME_TYPE,
    MediaStore.Downloads.DATE_MODIFIED
   )
   val sortOrder = "${MediaStore.Downloads.DATE_MODIFIED} DESC"


   context.contentResolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
    projection,
    null,
    null,
    sortOrder)?.use { cursor ->

    while (cursor.moveToNext()) {
     val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Downloads._ID))
     //  val path = cursor.getString(cursor.getColumnIndex(MediaStore.Downloads.RELATIVE_PATH))
     val name = cursor.getString(cursor.getColumnIndex(MediaStore.Downloads.DISPLAY_NAME))
     val date = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED))
     val uri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
     val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads.SIZE))
     println("UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   $uri")

     //  length of the value StringBuffer is 4860024
     // UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU size 132448 and  135627685


     val data=PDFData(name,uri)
     allpdfs.add(data)
     pdfs.postValue(allpdfs)

    }
    cursor.close()
   }

  }
    return pdfs
 }




 //  check for android 11

   fun queryForAllPdf(context: Context):LiveData<List<PDFData>>{
    val pdfs = MutableLiveData<List<PDFData>>()
    val allpdfs = mutableListOf<PDFData>()

    val projection = arrayOf(
     MediaStore.Downloads._ID,
     MediaStore.Downloads.RELATIVE_PATH,
     MediaStore.Downloads.DISPLAY_NAME,
     MediaStore.Downloads.SIZE,
     MediaStore.Downloads.MIME_TYPE,
     MediaStore.Downloads.DATE_MODIFIED
    )

    val sortOrder = "${MediaStore.Downloads.DATE_MODIFIED} DESC"
     CoroutineScope(Default).launch {
          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
           // uri instead of path

           context.contentResolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
           )?.use { cursor ->
            while (cursor.moveToNext()) {
             val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
             val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads.SIZE))
             val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.RELATIVE_PATH))
             val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME))
             val date = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))
             val uri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
             val data = PDFData(name, uri)
             allpdfs.add(data)
             pdfs.postValue(allpdfs)

            }
            cursor.close()
           }

          }
           else{

           }



      }

    return pdfs
   }









 //  save pdf
 @RequiresApi(Build.VERSION_CODES.Q)
 suspend fun savePdf(context: Context, pdf:PdfDocument){
  withContext(IO){
   val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
   val dirDest = File(Environment.DIRECTORY_DOWNLOADS, context.getString(R.string.app_name))
   val date = System.currentTimeMillis()
   val name = UUID.randomUUID().toString()
   val newPDF = ContentValues().apply{
    put(MediaStore.Downloads.DISPLAY_NAME, "$name.pdf")
    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
    put(MediaStore.Downloads.DATE_ADDED, date)
    put(MediaStore.Downloads.DATE_MODIFIED, date)
    put(MediaStore.Downloads.RELATIVE_PATH, "$dirDest${File.separator}")
    put(MediaStore.Downloads.IS_PENDING, 1)
   }
   val newPDFUri = context.contentResolver.insert(collection, newPDF)
   println("LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL pdf name $newPDFUri")
   if (newPDFUri != null) {

//      // previous
//        context.contentResolver.openFileDescriptor(newPDFUri,"w").use {
//     FileOutputStream(it?.fileDescriptor).use {
//
//      pdf.writeTo(it)
//     }
//    }

   // new

    context.contentResolver.openFile(newPDFUri,"w",null).use {
             FileOutputStream(it?.fileDescriptor).use { item->
             pdf.writeTo(item)
             }
    }




//    context.contentResolver.openOutputStream(newPDFUri,"w").use {
//     pdf.writeTo(it)
//
//    }



   }
   newPDF.clear()
   newPDF.put(MediaStore.Downloads.IS_PENDING, 0)
   if (newPDFUri != null) {
    context.contentResolver.update(newPDFUri, newPDF, null, null)
   }
   println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Complete^^^^^^^^")
  }



 }





 suspend fun createPdf(imageList: ArrayList<com.example.pdf_makerviewer.model.Image>) : PdfDocument{
       if (imageList.size > 0) {
          document = PdfDocument()
            for (item in imageList) {
             var bitmap = item.bitmap
             var size = item.size
             println("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR $size")
             //  resize the image
             bitmap= ImageResizer.reduceBitmapSize(bitmap,size)
             withContext(IO){
              val pageInfo: PdfDocument.PageInfo =
               PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()

              val page: PdfDocument.Page = document.startPage(pageInfo)

              val canvas: Canvas = page.canvas
              val paint: Paint = Paint()
              val paint2: Paint = Paint()
              paint2.setColor(Color.BLUE)
              paint.setColor(Color.WHITE)
              canvas.drawPaint(paint)
              canvas.drawBitmap(bitmap, 0f, 1.0f, paint2)
              document.finishPage(page)

              // save this document


              }

            }

        } ;  return document
    }




 @SuppressLint("Range")
 suspend fun queryImagesOnDevice(context: Context, selection: String? = null): List<Media> {
  val images = mutableListOf<Media>()

  withContext(Dispatchers.IO) {
   var projection = arrayOf(MediaStore.Images.Media._ID,
    MediaStore.Images.Media.RELATIVE_PATH,
    MediaStore.Images.Media.DISPLAY_NAME,
    MediaStore.Images.Media.SIZE,
    MediaStore.Images.Media.MIME_TYPE,
    MediaStore.Images.Media.WIDTH,
    MediaStore.Images.Media.HEIGHT,
    MediaStore.Images.Media.DATE_MODIFIED)

   if (hasSdkHigherThan(Build.VERSION_CODES.Q)) {
    projection += arrayOf(MediaStore.Images.Media.IS_FAVORITE)
   }

   val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

   context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    projection,
    selection,
    null,
    sortOrder)?.use { cursor ->

    while (cursor.moveToNext()) {
     val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
     val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH))
     val name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
     val size = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
     val mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
     val width = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH))
     val height = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT))
     val date = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED))

     val favorite =
      if (hasSdkHigherThan(Build.VERSION_CODES.Q)) {
       cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.IS_FAVORITE))
      } else {
       "0"
      }

     val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

     // Discard invalid images that might exist on the device
     if (size == null) {
      continue
     }

     images += Media(id, uri, path, name, size, mimeType, width, height, date, favorite == "1")
    }

    cursor.close()
   }
  }

  return images
 }





}