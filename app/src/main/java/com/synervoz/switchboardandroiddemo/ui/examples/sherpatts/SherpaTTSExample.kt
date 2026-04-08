package com.synervoz.switchboardandroiddemo.ui.examples.sherpatts

import android.content.Context
import android.util.Log
import com.synervoz.switchboard.sdk.Switchboard
import com.synervoz.switchboardsherpa.SherpaExtension

class SherpaTTSExample(context: Context) {
    private var engineId: String? = null
    private val dataDirectoryPath: String = context.filesDir.absolutePath

    init {
        createEngine(context)
        start()
    }

    private fun createEngine(context: Context) {
        SherpaExtension.load()

        val initResult = Switchboard.initialize(
            context = context,
            appId = "demo",
            appSecret = "demo",
            extensions = mapOf(
                "Sherpa" to emptyMap<String, Any>()
            )
        )

        if (initResult.isError) {
            Log.e("SherpaTTSExample", "Failed to initialize Switchboard SDK")
            return
        }

        val configJson = context.assets.open("TTSExample.json").readBytes().decodeToString()

        val result = Switchboard.createEngine(configJson)
        if (result.isError) {
            Log.e("SherpaTTSExample", "Failed to create engine")
            return
        }

        engineId = result.value

        val modelPath = "$dataDirectoryPath/en_GB/vits-piper-en_GB-southern_english_female-low/en_GB-southern_english_female-low.with_runtime_opt.ort"
        val tokensPath = "$dataDirectoryPath/en_GB/vits-piper-en_GB-southern_english_female-low/tokens.txt"
        val dataPath = "$dataDirectoryPath/en_GB/vits-piper-en_GB-southern_english_female-low/espeak-ng-data"

        Switchboard.callAction(
            objectId = "sherpaTTSNode",
            actionName = "loadModel",
            params = mapOf(
                "modelPath" to modelPath,
                "tokensPath" to tokensPath,
                "dataPath" to dataPath
            )
        )
    }

    private fun start() {
        val engineId = this.engineId ?: return
        val startResult = Switchboard.callAction(engineId, "start")
        if (startResult.isError) {
            Log.e("SherpaTTSExample", "Failed to start engine")
        }
    }

    fun close() {
        val engineId = this.engineId ?: return
        Switchboard.callAction(engineId, "stop")
    }

    fun synthesize(text: String) {
        Switchboard.callAction(
            objectId = "sherpaTTSNode",
            actionName = "synthesize",
            params = mapOf("text" to text)
        )
    }
}