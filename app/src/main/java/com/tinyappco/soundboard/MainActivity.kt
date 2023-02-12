package com.tinyappco.soundboard

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tinyappco.soundboard.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQ = 1

    private lateinit var audioManager: AudioManager

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        drawBoard(3,4)
        requestPermissions()

        audioManager = AudioManager(this)
    }

    private fun requestPermissions(){
        val hasRecordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!hasRecordPermission){
            val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
                    permitted: Boolean ->
                if (!permitted){
                    Toast.makeText(this,"You need to give permissions for the app to work",Toast.LENGTH_LONG).show()
                }
            }
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }


    private fun drawBoard(width: Int, height: Int){

        val gridLayout = GridLayout(this)
        gridLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        gridLayout.columnCount = width

        for (i in 0 until (width * height)){
            val button = Button(this)
            button.tag = i
            val buttonNumber = i + 1
            button.text = "$buttonNumber"
            button.setOnTouchListener(touchListener)
            button.gravity = Gravity.CENTER
            gridLayout.addView(button)

            val layoutParams = GridLayout.LayoutParams()
            layoutParams.columnSpec = GridLayout.spec(i % width,1f)
            layoutParams.rowSpec = GridLayout.spec(i / width,1f)
            button.layoutParams = layoutParams
        }
        binding.layout.addView(gridLayout)
    }

    private val touchListener = View.OnTouchListener { v: View?, event: MotionEvent? -> Boolean

            v?.performClick()

            val id = v?.tag as Int

            if (event?.action == MotionEvent.ACTION_DOWN) {

                if (binding.toggleButton.isChecked){ //recording
                    val isRecording = audioManager.startRecording(id)
                    if (isRecording) {
                        setBackgroundColour(Color.RED, v)
                    } else {
                        Toast.makeText(this,"Unable to start recording",Toast.LENGTH_LONG).show()
                    }

                } else {
                    if (audioManager.startPlayback(id)){
                       setBackgroundColour(Color.GREEN, v)
                    }
                }

                binding.toggleButton.isEnabled = false

                return@OnTouchListener true
            }
            if (event?.action == MotionEvent.ACTION_UP){

                if (binding.toggleButton.isChecked){
                    audioManager.stopRecording()
                } else {
                    audioManager.stopPlayback()
                }

                v.background.clearColorFilter()
                binding.toggleButton.isEnabled = true

                return@OnTouchListener true
            }

         false
    }

    private fun setBackgroundColour(colour: Int, v: View) {
        val filter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            BlendModeColorFilter(colour, BlendMode.DARKEN)
        } else {
            @Suppress("DEPRECATION")
            PorterDuffColorFilter(colour, PorterDuff.Mode.DARKEN)
            //PorterDuff is a class with list of blending + compositing modes, named after the authors of a paper on the subject
        }
        v.background.colorFilter = filter
    }


}
