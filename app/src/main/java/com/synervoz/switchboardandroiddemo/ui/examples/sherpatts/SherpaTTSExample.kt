package com.synervoz.switchboardandroiddemo.ui.examples.sherpatts

import android.content.Context

class SherpaTTSExample(context: Context) {
    private external fun createEngine(dataDirectoryPath: String, json: String)
    private external fun startEngine(): Boolean
    private external fun stopEngine(): Boolean
    private external fun synthesizeText(text: String)

    init {
        createEngine(context.filesDir.absolutePath, "TTSExample.json")
        startEngine()
    }

    fun close() {
        stopEngine()
    }

    fun synthesize(text: String) {
        synthesizeText(text)
    }
}