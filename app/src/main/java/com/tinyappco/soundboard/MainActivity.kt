package com.tinyappco.soundboard

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat


import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button

import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.view.ViewGroup
import android.widget.GridLayout

import java.io.File


class MainActivity : AppCompatActivity() {

    val PERMISSIONS_REQ = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawBoard();

        requestPermissions()

        toggleButton.textOn = "Record"
        toggleButton.textOff = "Playback"
    }

    fun requestPermissions(){
        val permissionsRequired = mutableListOf<String>()

        val hasRecordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!hasRecordPermission){
            permissionsRequired.add(Manifest.permission.RECORD_AUDIO)
        }

        val hasStoragePermission = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (!hasStoragePermission){
            permissionsRequired.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsRequired.isNotEmpty()){
            ActivityCompat.requestPermissions(this, permissionsRequired.toTypedArray(),PERMISSIONS_REQ)
        }
    }


//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == AUDIO)
//    }


    fun drawBoard(){

        val gridLayout = GridLayout(this)

        //make the grid fill the available height
        gridLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        val gridSize = 12

        gridLayout.columnCount = 3
        //gridLayout.rowCount = gridSize / gridLayout.columnCount

        for (i in 0..gridSize-1){

            val button = Button(this)
            button.tag = i
            button.setText("${i+1}")
            button.setOnTouchListener(touchListener)
            gridLayout.addView(button)
            button.gravity = Gravity.CENTER

            val colSpec = GridLayout.spec(i % gridLayout.columnCount,1f)
            val rowSpec = GridLayout.spec(i / gridLayout.columnCount,1f)
            val layoutParams = GridLayout.LayoutParams()
            layoutParams.columnSpec = colSpec
            layoutParams.rowSpec = rowSpec
            layoutParams.height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
            layoutParams.width = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
            button.layoutParams = layoutParams
        }
        this.layout.addView(gridLayout)

    }

    val touchListener = View.OnTouchListener { v: View?, event: MotionEvent? -> Boolean

            val id = v?.tag as Int

            //recording
            if (event?.action == MotionEvent.ACTION_DOWN) {

                if (toggleButton.isChecked){
                    startRecording(id)
                    v.background.setColorFilter(Color.RED,PorterDuff.Mode.DARKEN)
                    //PorterDuff is a class with list of blending + compositing modes, named after the authors of a paper on the subject

                } else {
                    startPlayback(id)
                    v.background.setColorFilter(Color.GREEN,PorterDuff.Mode.DARKEN)
                }

                toggleButton.isEnabled = false

                true
            }
            if (event?.action == MotionEvent.ACTION_UP){

                if (toggleButton.isChecked){
                    stopRecording()

                } else {
                    stopPlayback()
                }

                v.background.clearColorFilter()
                toggleButton.isEnabled = true

                true
            }


         false
    }

    var mediaRecorder : MediaRecorder? = null
    var mediaPlayer : MediaPlayer? = null

    fun startPlayback(channel: Int){
        val path = fileLocationForChannel(channel)
        if (File(path).exists()) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(path)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        }
    }

    fun stopPlayback(){
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun fileLocationForChannel(channel: Int) : String {
        return Environment.getExternalStorageDirectory().absolutePath + "/$channel.aac"
    }

    fun startRecording(channel: Int) {
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setAudioSamplingRate(48000)
        mediaRecorder?.setAudioEncodingBitRate(128000)

        val fileLocation = fileLocationForChannel(channel)
        mediaRecorder?.setOutputFile(fileLocation)
        mediaRecorder?.prepare()

        if (this.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
            //record
            //add storage permission to manifest
            mediaRecorder?.start()

        }
        else {
            Toast.makeText(this,"No Microphone",Toast.LENGTH_LONG).show()
        }
    }

    fun stopRecording(){
        mediaRecorder?.stop()
        mediaRecorder?.release()
    }

}
