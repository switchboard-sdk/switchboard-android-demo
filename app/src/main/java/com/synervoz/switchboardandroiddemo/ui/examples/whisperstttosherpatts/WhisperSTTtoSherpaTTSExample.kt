package com.synervoz.switchboardandroiddemo.ui.examples.whisperstttosherpatts

import android.content.Context
import com.synervoz.switchboardandroiddemo.AssetUtils

class WhisperSTTtoSherpaTTSExample(context: Context) {
    private external fun createEngine(dataDirectoryPath: String, json: String)
    private external fun startEngine(): Boolean
    private external fun stopEngine(): Boolean

    var isRunning: Boolean = false
        private set

    init {
        AssetUtils.copyAssetFileToInternal(context, "STTtoTTSExample.json", "STTtoTTSExample.json")
        createEngine(context.filesDir.absolutePath, "STTtoTTSExample.json")
    }

    fun close() {
        stopEngine()
    }

    fun start() {
        startEngine()
        isRunning = true
    }

    fun stop() {
        stopEngine()
        isRunning = false
    }
}
