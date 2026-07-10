package com.synervoz.switchboardandroiddemo.ui.examples.whisperstt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.synervoz.switchboardwhisper.TranscriptionInterface

@Composable
fun WhisperSTTScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var example by remember { mutableStateOf<WhisperSTTExample?>(null) }
    var transcript by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {
        val instance = WhisperSTTExample(context)
        instance.onTranscriptionUpdate = TranscriptionInterface { text, _ ->
            transcript = if (transcript.isEmpty()) text else "$transcript $text"
        }
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
            text = transcript.ifEmpty { "Tap the Start button to start transcribing" },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
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
