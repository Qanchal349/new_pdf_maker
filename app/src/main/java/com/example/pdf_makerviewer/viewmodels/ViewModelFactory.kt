package com.example.pdf_makerviewer.viewmodels

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider



class ViewModelFactory(private  val context: Application)  :
    ViewModelProvider.NewInstanceFactory() {

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainActivityViewModel(context) as T
    }
}