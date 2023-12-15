package com.example.pdf_makerviewer

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal class PdfRendererCore(private val context: Context, pdfFile: File, private var pdfQuality: PdfQuality
) {
    companion object {
        private const val PREFETCH_COUNT = 3
    }

    private val cachePath = "___pdf___cache___"
    private var pdfRenderer: PdfRenderer? = null

    init {

        initCache()
        openPdfFile(pdfFile)
    }

    private fun initCache() {
        val cache = File(context.cacheDir, cachePath)
        if (cache.exists())
            cache.deleteRecursively()
        cache.mkdirs()
    }

    private fun getBitmapFromCache(pageNo: Int): Bitmap? {
        val loadPath = File(File(context.cacheDir, cachePath), pageNo.toString())
        if (!loadPath.exists())
            return null

        return try {
            BitmapFactory.decodeFile(loadPath.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    @Throws(IOException::class)
    private fun writeBitmapToCache(pageNo: Int, bitmap: Bitmap) {
        val savePath = File(File(context.cacheDir, cachePath), pageNo.toString())
        savePath.createNewFile()
        val fos = FileOutputStream(savePath)
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
        println("========================================  ${savePath.readBytes()}")
        fos.flush()
        fos.close()
    }

    private fun openPdfFile(pdfFile: File) {
        try {



//         val fileDescriptor =
//                ParcelFileDescriptor.open(File("content://media/external/downloads/20081"), ParcelFileDescriptor.MODE_READ_ONLY)
//            println(".........................................  $fileDescriptor")

            val fileDescriptor = context.contentResolver.openFileDescriptor(
                Uri.parse("content://media/external/downloads/21497"),
                "r"
            ) ?: return

            pdfRenderer = PdfRenderer(fileDescriptor)


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPageCount(): Int = pdfRenderer?.pageCount ?: 0

    fun renderPage(pageNo: Int, onBitmapReady: ((bitmap: Bitmap?, pageNo: Int) -> Unit)? = null) {
        if (pageNo >= getPageCount())
            return

//        GlobalScope.async {
//            synchronized(this@PdfRendererCore) {
//                buildBitmap(pageNo) { bitmap ->
//                    GlobalScope.launch(Dispatchers.Main) { onBitmapReady?.invoke(bitmap, pageNo) }
//                }
//                onBitmapReady?.let {
//                    prefetchNext(pageNo + 1)
//                }
//            }
//        }


        CoroutineScope(Dispatchers.IO).launch {
            val mutex = Mutex()
            println("}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}" + Thread.currentThread().name)
            mutex.withLock {
                GlobalScope.launch(Dispatchers.IO) {
                    buildBitmap(pageNo) { bitmap ->
                        GlobalScope.launch(Dispatchers.Main) {
                            onBitmapReady?.invoke(bitmap, pageNo)
                        }
                    }
                }


                onBitmapReady?.let {
                    prefetchNext(pageNo + 1)
                }
            }

        }


    }

    private  fun prefetchNext(pageNo: Int) {
        val countForPrefetch = min(getPageCount(), pageNo + PREFETCH_COUNT)
        for (pageToPrefetch in pageNo until countForPrefetch) {
            renderPage(pageToPrefetch)
        }
    }

    private fun buildBitmap(pageNo: Int, onBitmap: (Bitmap?) -> Unit) {
        var bitmap = getBitmapFromCache(pageNo)
        bitmap?.let {
            onBitmap(it)
            return@buildBitmap
        }

        val startTime = System.currentTimeMillis()

        try {

            val pdfPage = pdfRenderer!!.openPage(pageNo)
            bitmap = Bitmap.createBitmap(
                pdfPage.width * pdfQuality.ratio,
                pdfPage.height * pdfQuality.ratio,
                Bitmap.Config.ARGB_8888
            )
            bitmap ?: return
            pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdfPage.close()
            writeBitmapToCache(pageNo, bitmap)

            onBitmap(bitmap)
            pdfRenderer?.openPage(pageNo)?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closePdfRender() {
        if (pdfRenderer != null)
            try {
                pdfRenderer!!.close()
            } catch (e: Exception) {
                Log.e("PdfRendererCore", e.toString())
            }
    }

}







//
//
//
//UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21517
//2022-09-25 11:42:20.740 11294-11328/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21513
//2022-09-25 11:42:20.740 11294-11328/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21502
//2022-09-25 11:42:20.740 11294-11328/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21499
//2022-09-25 11:42:20.741 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21517
//2022-09-25 11:42:20.741 11294-11328/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21498
//2022-09-25 11:42:20.741 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21513
//2022-09-25 11:42:20.741 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21502
//2022-09-25 11:42:20.741 11294-11328/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21497
//2022-09-25 11:42:20.741 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21499
//2022-09-25 11:42:20.742 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21498
//2022-09-25 11:42:20.742 11294-11328/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21496
//2022-09-25 11:42:20.742 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21497
//2022-09-25 11:42:20.742 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21496
//2022-09-25 11:42:20.742 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21495
//2022-09-25 11:42:20.742 11294-11328/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21495
//2022-09-25 11:42:20.742 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21494
//2022-09-25 11:42:20.743 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21493
//2022-09-25 11:42:20.743 11294-11328/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21494
//2022-09-25 11:42:20.743 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21492
//2022-09-25 11:42:20.743 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21491
//2022-09-25 11:42:20.743 11294-11328/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21493
//2022-09-25 11:42:20.744 11294-11329/com.example.pdf_makerviewer I/System.out: UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU   content://media/external/downloads/21490
