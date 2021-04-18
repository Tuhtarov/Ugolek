package com.example.myapplication.common.management.progressBar

import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar

class ProgressBarManage(private val progressBar: ProgressBar, private val progressBarContainer: LinearLayout) {
    fun progressOn(){
        progressBarContainer.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
    }
    fun progressOff(){
        progressBarContainer.visibility = View.INVISIBLE
        progressBar.visibility = View.INVISIBLE
    }
}