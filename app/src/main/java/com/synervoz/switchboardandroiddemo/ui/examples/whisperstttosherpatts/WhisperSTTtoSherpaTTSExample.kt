package com.synervoz.switchboardandroiddemo.ui.examples.whisperstttosherpatts

import android.content.Context
import android.util.Log
import com.synervoz.switchboard.sdk.Switchboard
import com.synervoz.switchboardandroiddemo.AssetUtils
import com.synervoz.switchboardonnx.OnnxExtension
import com.synervoz.switchboardsherpa.SherpaExtension
import com.synervoz.switchboardsilerovad.SileroVADExtension
import com.synervoz.switchboardwhisper.WhisperExtension

class WhisperSTTtoSherpaTTSExample(context: Context) {
    private var engineId: String? = null
    private val dataDirectoryPath: String = context.filesDir.absolutePath
    private val vadEventListeners = mutableListOf<Int>()

    var isRunning: Boolean = false
        private set

    init {
        AssetUtils.copyAssetFileToInternal(context, "STTtoTTSExample.json", "STTtoTTSExample.json")
        createEngine(context)
    }

    private fun createEngine(context: Context) {
        WhisperExtension.load()
        SileroVADExtension.load()
        SherpaExtension.load()

        val initResult = Switchboard.initialize(
            context = context,
            appId = "demo",
            appSecret = "demo",
            extensions = mapOf(
                "Whisper" to emptyMap<String, Any>(),
                "SileroVAD" to emptyMap<String, Any>(),
                "Sherpa" to emptyMap<String, Any>()
            )
        )

        if (initResult.isError) {
            Log.e("WhisperSTTtoSherpaTTSExample", "Failed to initialize Switchboard SDK")
            return
        }

        val configJson = context.filesDir.resolve("STTtoTTSExample.json").readText()

        val result = Switchboard.createEngine(configJson)
        if (result.isError) {
            Log.e("WhisperSTTtoSherpaTTSExample", "Failed to create engine")
            return
        }

        engineId = result.value

        val modelPath = "$dataDirectoryPath/en_GB/vits-piper-en_GB-southern_english_female-low/en_GB-southern_english_female-low.with_runtime_opt.ort"
        val tokensPath = "$dataDirectoryPath/en_GB/vits-piper-en_GB-southern_english_female-low/tokens.txt"
        val dataPath = "$dataDirectoryPath/en_GB/vits-piper-en_GB-southern_english_female-low/espeak-ng-data"

        Switchboard.callAction(
            objectId = "ttsNode",
            actionName = "loadModel",
            params = mapOf(
                "modelPath" to modelPath,
                "tokensPath" to tokensPath,
                "dataPath" to dataPath
            )
        )

        val whisperModelPath = "$dataDirectoryPath/ggml-tiny.en.bin"
        Switchboard.callAction(
            objectId = "sttNode",
            actionName = "loadModel",
            params = mapOf(
                "modelPath" to whisperModelPath,
                "useGPU" to true
            )
        )

        val speechStartedListener = Switchboard.addEventListener(
            objectId = "vadNode",
            eventName = "speechStarted"
        ) { _, _ ->
            Log.i("WhisperSTTtoSherpaTTSExample", "vadNode start")
        }
        speechStartedListener.value?.let { vadEventListeners.add(it) }

        val speechEndedListener = Switchboard.addEventListener(
            objectId = "vadNode",
            eventName = "speechEnded"
        ) { _, _ ->
            Log.i("WhisperSTTtoSherpaTTSExample", "vadNode end")
        }
        speechEndedListener.value?.let { vadEventListeners.add(it) }
    }

    fun close() {
        stop()
        cleanup()
    }

    fun start() {
        val engineId = this.engineId ?: return
        val startResult = Switchboard.callAction(engineId, "start")
        if (startResult.isError) {
            Log.e("WhisperSTTtoSherpaTTSExample", "Failed to start engine")
            return
        }
        isRunning = true
    }

    fun stop() {
        val engineId = this.engineId ?: return
        Switchboard.callAction(engineId, "stop")
        isRunning = false
    }

    private fun cleanup() {
        vadEventListeners.forEach { listenerId ->
            Switchboard.removeEventListener("vadNode", listenerId)
        }
        vadEventListeners.clear()
    }
}
