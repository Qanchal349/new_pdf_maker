package com.example.pdf_makerviewer

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.AppComponentFactory
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import com.example.pdf_makerviewer.databinding.ActivityMainBinding
import com.example.pdf_makerviewer.databinding.DialogLayoutBinding
import com.example.pdf_makerviewer.model.PDFData
import com.example.pdf_makerviewer.utils.Utils
import com.example.pdf_makerviewer.viewmodels.MainActivityViewModel
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import androidx.lifecycle.ViewModelProvider
import com.example.pdf_makerviewer.model.Image
import com.example.pdf_makerviewer.viewmodels.ViewModelFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity(),PDFListAdapter.OnItemClickListener,PDFListAdapter.OnItemLongClickListener{

     lateinit var imageUri: Uri
     lateinit var uri: Uri
     private var binding: ActivityMainBinding? = null
     lateinit var viewModel: MainActivityViewModel
     var pdfList= mutableListOf<PDFData>()
     lateinit var obj_adapter: PDFListAdapter
      var doc:PdfDocument?=null



    companion object{
        var imageList:ArrayList<Image> = ArrayList()
        var imageUri1:ArrayList<String> = ArrayList()
    }

    var photoFile: File? = null
    private val CAPTURE_IMAGE_REQUEST = 1
    var mCurrentPhotoPath: String? = null



    @RequiresApi(Build.VERSION_CODES.Q)
      var getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
             if (data?.clipData != null) {
                val count = data.clipData?.itemCount ?: 0
                 for (i in 0 until count) {
                     val imageUri: Uri? = data.clipData?.getItemAt(i)?.uri
                     imageUri1.add(imageUri.toString())
                     getBitmapFromUri(imageUri)
                  }

                  if(imageUri1.size > 0){
                     //  doc = PdfOperations.createPdf(imageList)
                     val intent = Intent(this@MainActivity,DetailActivity::class.java)
                     intent.putStringArrayListExtra("IMAGE", imageUri1)
                     startActivity(intent)
                  }

                }


            //If single image selected
            else if (data?.data != null) {
                val imageUri: Uri? = data.data
                val file = getBitmapFromUri(imageUri)
                file.let {
                   // selectedPaths.add(it.absolutePath)
                }

            }


        }

    }

     private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
         if(it){
            // binding?.imageView?.setImageURI(uri)
             val intent = Intent(this,BasicActivity::class.java)
             intent.putExtra("JL",uri.toString())
             startForResult.launch(intent)
             finish()
         }
     }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            // get the cropped image and save into imageList

        }
    }



    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getBitmapFromUri(imageUri: Uri?) {
        val stream = imageUri?.let { contentResolver.openInputStream(it) }
        val bitmap = BitmapFactory.decodeStream(stream)
        imageList.add(Image(bitmap,240000))

    }


      fun showAllImages(){

          startActivity(Intent(this,SelectImagesActivity::class.java))

      }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_PDFMakerViewer)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbar)
        supportActionBar?.setTitle(" All Pdf Files ")

        binding?.open?.setOnClickListener {
                      //  launchPdf()
          showAllImages()
        }

        viewModel = ViewModelProvider(this, ViewModelFactory(this@MainActivity.application)).get(MainActivityViewModel::class.java)
        viewModel.showAllPdfs(this).observe(this, androidx.lifecycle.Observer {
             pdfList= it as MutableList<PDFData>
            obj_adapter = PDFListAdapter(this@MainActivity, pdfList as ArrayList<PDFData>, this@MainActivity, this@MainActivity)
            binding?.pdfList?.adapter=obj_adapter
        })



        showPdfList()

        imageList= ArrayList()
        val intent = intent
        val text = intent.getParcelableExtra<Uri>("RESULT")
        val pdfDone = intent.getBooleanExtra("DONE",false);
        if(text!=null && !pdfDone ){
            imageUri1.add(text.toString())
            takeImagesFromCamera()
        } else{
            if(pdfDone){
                imageUri1.add(text.toString())
                if(imageUri1.size > 0){
                     val intent = Intent(this@MainActivity,DetailActivity::class.java)
                     intent.putStringArrayListExtra("IMAGE", imageUri1)
                     startActivity(intent)
                 }


            }
        }


       // showPdfList()

        if(imageList.size>0){
            binding?.create?.visibility=View.VISIBLE
        }

        binding?.apply {
            addPdf.setOnClickListener {
                showDialogBox()
            }
            create.setOnClickListener {
                if(imageList.size>0){
                    create.visibility=View.GONE
                    viewModel.createPdfFromCamera(this@MainActivity, imageList)
                    showPdfList()
                  //  obj_adapter.updateList(pdfList as ArrayList<PDFData>)

                }
            }
        }

     }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        showPdfList()

    }


    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putStringArrayList("VALUE", imageUri1)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.search -> {

            }
            R.id.sort -> {

            }
            R.id.grid -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun showDialogBox() {
            val dialog = Dialog(this)
            val customLayoutBinding : DialogLayoutBinding = DialogLayoutBinding.inflate(layoutInflater)
            dialog.setContentView(customLayoutBinding.root)
            customLayoutBinding.apply {
                gallery.setOnClickListener {
                    if(Utils.hasPermissionForReadExternalStorage(applicationContext)){
                        fetchImagesFromGallery()
                    }else{
                        Utils.requestPermissions(this@MainActivity)
                    }
                    dialog.dismiss()
                }
                camera.setOnClickListener {
                    if(Utils.hasPermissionForCamera(applicationContext)){
                        takeImagesFromCamera()
                    }else{
                        Utils.requestPermissions(this@MainActivity)
                    }
                    dialog.dismiss()
                }
            }
           dialog.show()
        }



    private fun takeImagesFromCamera() {
        val name=UUID.randomUUID().toString()
         val filename = "$name.jpg"

        // Get the correct media Uri for your Android build version
         imageUri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val imageDetails = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, filename)
        }

        // Try to create a Uri for your image file
        this.contentResolver.insert(imageUri, imageDetails).let {
            // Save the generated Uri using our placeholder from before
            if (it != null) {
                uri = it
            }

            // Capture your image
            contract.launch(uri)

        }


    }

    private fun fetchImagesFromGallery() {
        val intent = Intent(ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        getContent.launch(intent)

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showPdfList() {
        viewModel.showAllPdfs(this).observe(this, androidx.lifecycle.Observer {
            pdfList= it as MutableList<PDFData>
            if(pdfList.size==0){
                binding?.apply {
                    noPdf.visibility=View.VISIBLE
                    nofile.visibility=View.VISIBLE
                }
            }

            else{
                binding?.apply {
                    noPdf.visibility=View.INVISIBLE
                    nofile.visibility=View.INVISIBLE
                }
                obj_adapter.updateList(pdfList as ArrayList<PDFData>)
                obj_adapter = PDFListAdapter(this@MainActivity, pdfList as ArrayList<PDFData>,this@MainActivity,this@MainActivity)
                binding?.pdfList?.adapter=obj_adapter

            }

        })
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==Constants.REQUEST_CODE && grantResults.isNotEmpty()){
            for(i in grantResults.indices){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    Log.d("MainActivity","${permissions[i]} granted ")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(binding!=null){
            binding=null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onItemClick(position: Int) {
        CoroutineScope(Main).launch{
          //  pdfList=  PdfOperations.queryForPDF(applicationContext)
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfList[position].uri,"application/pdf")
        }
        startActivity(intent)
    }


    override fun OnLongItemClickListener(position: Int) {
        Toast.makeText(this, "long", Toast.LENGTH_SHORT).show()
    }




    private fun captureImage() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                // Create the File where the photo should go
                try {
                    photoFile = createImageFile()
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        val photoURI = FileProvider.getUriForFile(
                            this,
                            "com.example.pdf_makerviewer",
                            photoFile!!
                        )
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
                    }
                } catch (ex: Exception) {
                    // Error occurred while creating the File
                    displayMessage(baseContext, ex.message.toString())
                }

            } else {
                displayMessage(baseContext, "Null")
            }
        }

    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        val intent = Intent(this,BasicActivity::class.java)
            intent.putExtra("ALL",image)
            startForResult.launch(intent)
         return image
    }

    private fun displayMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }



    @RequiresApi(Build.VERSION_CODES.Q)
    private fun launchPdf() {
        startActivity(
            // Use 'launchPdfFromPath' if you want to use assets file (enable "fromAssets" flag) / internal directory
            PdfViewerActivity.launchPdfFromPath(           //PdfViewerActivity.Companion.launchPdfFromUrl(..   :: incase of JAVA
                this,
                "content://media/external/downloads/20082",                                // PDF URL in String format
                "Pdf title/name ",                        // PDF Name/Title in String format
                "",                  // I:f nothing specific, Put "" it will save to Downloads
                enableDownload = false                    // This param is true by defualt.
            )
        )
    }



}
