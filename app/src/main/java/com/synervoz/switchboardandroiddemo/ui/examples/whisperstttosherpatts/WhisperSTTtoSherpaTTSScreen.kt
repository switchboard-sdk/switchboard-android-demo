package com.synervoz.switchboardandroiddemo.ui.examples.whisperstttosherpatts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun WhisperSTTtoSherpaTTSScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var example by remember { mutableStateOf<WhisperSTTtoSherpaTTSExample?>(null) }
    var isRunning by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val instance = WhisperSTTtoSherpaTTSExample(context)
        example = instance
        onDispose { instance.close() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Tap the Start button and talk into the microphone to synthesize your speech",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        Button(
            onClick = {
                val instance = example ?: return@Button
                if (isRunning) instance.stop() else instance.start()
                isRunning = !isRunning
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isRunning) "Stop" else "Start")
        }
    }
}
