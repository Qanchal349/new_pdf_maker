package com.example.pdf_makerviewer.viewmodels

import android.app.Application
import android.content.Context
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.pdf_makerviewer.PdfOperations
import com.example.pdf_makerviewer.model.Image
import com.example.pdf_makerviewer.model.PDFData
import com.example.pdf_makerviewer.repository.PdfOperationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@RequiresApi(Build.VERSION_CODES.Q)
class MainActivityViewModel(application: Application) : AndroidViewModel(application) {


  var repository: PdfOperationRepository


  init {
      repository= PdfOperationRepository()
  }






   @RequiresApi(Build.VERSION_CODES.Q)
   suspend  fun pdfFromGalleryImages(context: Context, pdf: PdfDocument){
       runBlocking {
           val job = async{
               repository.pdfFromGalleryImages(context,pdf)

           }
           job.await()
           println("YYYYYYYYYYYYYYYYYYYYYYYYYYYY  ${job}")
       }
   }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun pdfFromCameraImages(context: Context, pdf: PdfDocument){
        viewModelScope.launch {
            repository.pdfFromCameraImages(context,pdf)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun showAllPdfs(context: Context):LiveData<List<PDFData>> {
        return repository.showAllPdfFiles(context)

    }

     @RequiresApi(Build.VERSION_CODES.Q)
    fun createPdfFromGallery(context:Context, imageList:ArrayList<Image>){
         viewModelScope.launch {
             val document= repository.createPdfFromGallery(imageList)
             pdfFromGalleryImages(context,document)

         }
     }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun createPdfFromCamera(context: Context, imageList:ArrayList<Image>){
        viewModelScope.launch {
           val document= repository.createPdfFromCamera(imageList)
            pdfFromCameraImages(context,document)
        }
    }

    fun createPdf(imageList: ArrayList<Image>){
        viewModelScope.launch {
            repository.createPdf(imageList)
        }
    }

}