package com.synervoz.switchboardandroiddemo.ui.examples.whisperstt

import android.content.Context
import com.synervoz.switchboardwhisper.TranscriptionInterface

class WhisperSTTExample(context: Context) {
    var onTranscriptionUpdate: TranscriptionInterface? = null

    private external fun createEngine(dataDirectoryPath: String, json: String)
    private external fun startEngine(): Boolean
    private external fun stopEngine(): Boolean
    private external fun closeEngine()

    var isRunning: Boolean = false
        private set

    init {
        createEngine(context.filesDir.absolutePath, "STTExample.json")
    }

    fun close() {
        stopEngine()
        closeEngine()
    }

    fun start() {
        startEngine()
        isRunning = true
    }

    fun stop() {
        stopEngine()
        isRunning = false
    }

    fun onTranscriptionUpdate(text: String, processingTime: Long) {
        onTranscriptionUpdate?.onTranscriptionUpdate(text, processingTime)
    }
}
