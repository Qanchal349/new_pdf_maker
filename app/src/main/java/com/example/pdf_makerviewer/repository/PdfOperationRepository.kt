package com.example.pdf_makerviewer.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.example.pdf_makerviewer.PdfOperations
import com.example.pdf_makerviewer.model.Image
import com.example.pdf_makerviewer.model.PDFData

class PdfOperationRepository {



    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun pdfFromGalleryImages(context: Context, pdf: PdfDocument) {
         PdfOperations.savePdf(context,pdf)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun pdfFromCameraImages(context: Context, pdf:PdfDocument){
        PdfOperations.savePdf(context,pdf)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
     fun showAllPdfFiles(context: Context):LiveData<List<PDFData>>{
       return PdfOperations.queryForPDF(context)
    }

    suspend fun showSinglePdfFile(uri:String){

    }

    suspend fun deleteSinglePdfFile(uri: String){

    }

    suspend fun deleteAllPdfFile(){

    }

    suspend fun createPdfFromGallery(imageList:ArrayList<Image>):PdfDocument{
        val document= PdfOperations.createPdf(imageList)
        return document
    }

    suspend fun createPdfFromCamera(imageList:ArrayList<Image>):PdfDocument{
        val document= PdfOperations.createPdf(imageList)
        return document
    }

    suspend fun createPdf(imageList: ArrayList<Image>){
        PdfOperations.createPdf(imageList)
    }

}