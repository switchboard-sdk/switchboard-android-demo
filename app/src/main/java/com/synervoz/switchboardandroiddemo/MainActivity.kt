package com.synervoz.switchboardandroiddemo

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.synervoz.switchboardandroiddemo.ui.examples.sherpatts.SherpaTTSScreen
import com.synervoz.switchboardandroiddemo.ui.examples.whisperstt.WhisperSTTScreen
import com.synervoz.switchboardandroiddemo.ui.examples.whisperstttosherpatts.WhisperSTTtoSherpaTTSScreen
import com.synervoz.switchboardandroiddemo.ui.theme.SwitchboardAndroidDemoTheme

private enum class Screen(val title: String) {
    MENU("Switchboard Demo"),
    SHERPA_TTS("Sherpa TTS"),
    WHISPER_STT("Whisper STT"),
    STT_TO_TTS("Whisper STT to Sherpa TTS"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        AssetUtils.copyAssetDirectoryToInternal(this, "model", "")
        AssetUtils.copyAssetFileToInternal(this, "TTSExample.json", "TTSExample.json")
        AssetUtils.copyAssetFileToInternal(this, "STTExample.json", "STTExample.json")
        AssetUtils.copyAssetFileToInternal(this, "STTtoTTSExample.json", "STTtoTTSExample.json")

        requestPermission()

        setContent {
            SwitchboardAndroidDemoTheme {
                DemoApp()
            }
        }
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
}

@Composable
private fun DemoApp() {
    var screen by remember { mutableStateOf(Screen.MENU) }

    BackHandler(enabled = screen != Screen.MENU) {
        screen = Screen.MENU
    }

    Scaffold { padding ->
        val contentModifier = Modifier.padding(padding)
        when (screen) {
            Screen.MENU -> MenuScreen(
                modifier = contentModifier,
                onSelect = { screen = it },
            )
            Screen.SHERPA_TTS -> SherpaTTSScreen(modifier = contentModifier)
            Screen.WHISPER_STT -> WhisperSTTScreen(modifier = contentModifier)
            Screen.STT_TO_TTS -> WhisperSTTtoSherpaTTSScreen(modifier = contentModifier)
        }
    }
}

@Composable
private fun MenuScreen(
    onSelect: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        for (target in listOf(Screen.SHERPA_TTS, Screen.WHISPER_STT, Screen.STT_TO_TTS)) {
            Button(
                onClick = { onSelect(target) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(target.title)
            }
        }
    }
}
