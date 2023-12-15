package com.example.pdf_makerviewer.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.os.BuildCompat
import com.example.pdf_makerviewer.Constants

object Utils {

    // for permissions

     fun hasPermissionForWriteExternalStorage(mContext: Context):Boolean{
        return  ActivityCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
    }

     fun hasPermissionForReadExternalStorage(mContext: Context):Boolean{
        return  ActivityCompat.checkSelfPermission(mContext,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
    }

     fun hasPermissionForCamera(mContext: Context):Boolean{
        return  ActivityCompat.checkSelfPermission(mContext,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED
    }

     fun requestPermissions(mContext: Activity){
      var permissionToRequest = mutableListOf<String>()
      if(!hasPermissionForWriteExternalStorage(mContext)){
         permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      }
        if(!hasPermissionForReadExternalStorage(mContext)){
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(!hasPermissionForCamera(mContext)){
            permissionToRequest.add(Manifest.permission.CAMERA)
        }

        if(permissionToRequest.isNotEmpty()){
            ActivityCompat.requestPermissions(mContext,permissionToRequest.toTypedArray(),Constants.REQUEST_CODE)
        }
    }


    fun hasSdkHigherThan(sdk: Int): Boolean {
        //Early previous of R will return Build.VERSION.SDK_INT as 29
        if (Build.VERSION_CODES.R == sdk) {
            return BuildCompat.isAtLeastR()
        }
        return Build.VERSION.SDK_INT > sdk
    }


}