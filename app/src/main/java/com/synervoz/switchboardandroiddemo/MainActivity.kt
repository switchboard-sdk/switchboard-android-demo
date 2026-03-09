package com.synervoz.switchboardandroiddemo

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.synervoz.switchboard.sdk.SwitchboardSDK
import com.synervoz.switchboardsherpa.SherpaExtension
import com.synervoz.switchboardandroiddemo.ui.examples.sherpatts.SherpaTTSFragment
import com.synervoz.switchboardandroiddemo.ui.examples.whisperstt.WhisperSTTFragment
import com.synervoz.switchboardandroiddemo.ui.examples.whisperstttosherpatts.WhisperSTTtoSherpaTTSFragment
import com.synervoz.switchboardsilerovad.SileroVADExtension
import com.synervoz.switchboardwhisper.WhisperExtension
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView(R.layout.activity_main)

        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)
        val button3: Button = findViewById(R.id.button3)

        button1.setOnClickListener {
            openFragment(SherpaTTSFragment())
        }

        button2.setOnClickListener {
            openFragment(WhisperSTTFragment())
        }

        button3.setOnClickListener {
            openFragment(WhisperSTTtoSherpaTTSFragment())
        }

        System.loadLibrary("SwitchboardAndroidDemo")

        // Please get your own  appID and appSecret from https://console.switchboard.audio/register
        SwitchboardSDK.initialize(this, "demo", "demo")
        SileroVADExtension.load()
        WhisperExtension.load()
        SherpaExtension.load()

        AssetUtils.copyAssetDirectoryToInternal(this, "model", "")
        AssetUtils.copyAssetFileToInternal(this, "TTSExample.json", "TTSExample.json")
        AssetUtils.copyAssetFileToInternal(this, "STTExample.json", "STTExample.json")
        AssetUtils.copyAssetFileToInternal(this, "STTtoTTSExample.json", "STTtoTTSExample.json")

        requestPermission()
    }

    private fun openFragment(fragment: Fragment) {
        val fragmentContainer = findViewById<FrameLayout>(R.id.fragment_container)
        fragmentContainer.visibility = View.VISIBLE
        fragmentContainer.isClickable = true
        fragmentContainer.isFocusable = true

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun requestPermission(): Boolean {
        val permissions: MutableList<String> = mutableListOf(
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 0)
                return false
            }
        }
        return true
    }

    override fun onBackPressed() {
        val fragmentContainer = findViewById<FrameLayout>(R.id.fragment_container)

        // Check if the fragment container is visible
        if (fragmentContainer.isVisible) {
            // Hide the fragment container and remove the fragment
            fragmentContainer.visibility = View.GONE

            // Optionally, you can remove the fragment itself from the FragmentManager:
            // (this will prevent it from being restored when navigating back)
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (fragment != null) {
                supportFragmentManager.beginTransaction()
                    .remove(fragment)
                    .commit()
            }
        } else {
            // Call the default behavior (this will exit the activity or go back to the previous activity)
            super.onBackPressed()
        }
    }
}