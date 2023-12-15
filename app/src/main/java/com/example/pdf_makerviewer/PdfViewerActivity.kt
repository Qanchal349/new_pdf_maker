package com.example.pdf_makerviewer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class PdfViewerActivity : AppCompatActivity() {

    private var permissionGranted: Boolean? = false
    private var menuItem: MenuItem? = null
    private var fileUrl: String? = null


    companion object {
        const val FILE_URL = "pdf_file_url"
        private const val FILE_DIRECTORY = "pdf_file_directory"
        private const val FILE_TITLE = "pdf_file_title"
        const val ENABLE_FILE_DOWNLOAD = "enable_download"
        const val FROM_ASSETS = "from_assests"
        var engine = PdfEngine.INTERNAL
        var enableDownload = true
        var isPDFFromPath = false
        var isFromAssets = false
        var PERMISSION_CODE = 4040



//        fun launchPdfFromUrl(context: Context?, pdfUrl: String?, pdfTitle: String?, directoryName: String?, enableDownload: Boolean = true): Intent {
//            val intent = Intent(context, PdfViewerActivity::class.java)
//            intent.putExtra(FILE_URL, pdfUrl)
//            intent.putExtra(FILE_TITLE, pdfTitle)
//            intent.putExtra(FILE_DIRECTORY, directoryName)
//            intent.putExtra(ENABLE_FILE_DOWNLOAD, enableDownload)
//            isPDFFromPath = false
//            return intent
//        }

        fun launchPdfFromPath(context: Context?, path: String?, pdfTitle: String?, directoryName: String?, enableDownload: Boolean = true, fromAssets: Boolean = false): Intent {
            val intent = Intent(context, PdfViewerActivity::class.java)
            intent.putExtra(FILE_URL, path)
            intent.putExtra(FILE_TITLE, pdfTitle)
            intent.putExtra(FILE_DIRECTORY, directoryName)
            intent.putExtra(ENABLE_FILE_DOWNLOAD, enableDownload)
            //  intent.putExtra(FROM_ASSETS, fromAssets)
            isPDFFromPath = true
            return intent
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

//        setUpToolbar(
//            intent.extras!!.getString(
//                FILE_TITLE,
//                "PDF"
//            )
//        )

        enableDownload = intent.extras!!.getBoolean(
            ENABLE_FILE_DOWNLOAD,
            true
        )

        isFromAssets = intent.extras!!.getBoolean(
            FROM_ASSETS,
            false
        )

        engine = PdfEngine.INTERNAL

        init()
    }

    private fun init() {
        if (intent.extras!!.containsKey(FILE_URL)) {
            fileUrl = intent.extras!!.getString(FILE_URL)
            if (isPDFFromPath) {
                initPdfViewerWithPath(this.fileUrl)
            } else {

            }
        }
    }











    private fun initPdfViewerWithPath(filePath: String?) {

        if (TextUtils.isEmpty(filePath)) onPdfError()

        //Initiating PDf Viewer with URL
        try {

            val file = filePath
            findViewById<PdfRendererView>(R.id.pdfView).initWithPath(
                file.toString(),
                PdfQuality.NORMAL
            )

        } catch (e: Exception) {
            onPdfError()
        }

        //  enableDownload()
    }


    private fun checkPermissionOnInit() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) === PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted = true
        }
    }

    private fun onPdfError() {
        Toast.makeText(this, "Pdf has been corrupted", Toast.LENGTH_SHORT).show()
        true.showProgressBar()
        finish()
    }

    private fun Boolean.showProgressBar() {
       // findViewById<ProgressBar>(R.id.progressBar).visibility = if (this) View.VISIBLE else View.GONE
    }


    private fun checkPermission(requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                requestCode
            )
        } else {
            permissionGranted = true
            // downloadPdf()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted = true
            //  downloadPdf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        findViewById<PdfRendererView>(R.id.pdfView).closePdfRender()
    }




}