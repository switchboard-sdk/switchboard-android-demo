package com.synervoz.switchboardandroiddemo.ui.examples.sherpatts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
fun SherpaTTSScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var example by remember { mutableStateOf<SherpaTTSExample?>(null) }
    var text by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val instance = SherpaTTSExample(context)
        example = instance
        onDispose { instance.close() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Text to synthesize") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                if (text.isNotBlank()) example?.synthesize(text)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Synthesize")
        }
    }
}
