package com.synervoz.switchboardandroiddemo.ui.examples.whisperstttosherpatts

import android.content.Context
import com.synervoz.switchboard.sdk.Switchboard
import com.synervoz.switchboardandroiddemo.AssetUtils
import com.synervoz.switchboardsherpa.SherpaExtension
import com.synervoz.switchboardsilerovad.SileroVADExtension
import com.synervoz.switchboardwhisper.WhisperExtension

class WhisperSTTtoSherpaTTSExample(private val context: Context) {
    private var engineId: String? = null
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
            throw RuntimeException("Failed to initialize Switchboard SDK")
        }

        val configJson = context.filesDir.resolve("STTtoTTSExample.json").readText()

        val result = Switchboard.createEngine(configJson)
        if (result.isError) {
            throw RuntimeException("Failed to create engine")
        }

        engineId = result.value

        val dataDirectoryPath = context.filesDir.absolutePath
        val modelPath = "$dataDirectoryPath/en_GB/vits-piper-en_GB-southern_english_female-low/en_GB-southern_english_female-low.with_runtime_opt.ort"
        val tokensPath = "$dataDirectoryPath/en_GB/vits-piper-en_GB-southern_english_female-low/tokens.txt"
        val dataPath = "$dataDirectoryPath/en_GB/vits-piper-en_GB-southern_english_female-low/espeak-ng-data"

        val loadTTSModelResult = Switchboard.callAction(
            objectId = "ttsNode",
            actionName = "loadModel",
            params = mapOf(
                "modelPath" to modelPath,
                "tokensPath" to tokensPath,
                "dataPath" to dataPath
            )
        )

        if (loadTTSModelResult.isError) {
            throw RuntimeException("Failed to load TTS model: ${loadTTSModelResult.error}")
        }

        val whisperModelPath = "$dataDirectoryPath/ggml-tiny.en.bin"
        val loadSTTModelResult = Switchboard.callAction(
            objectId = "sttNode",
            actionName = "loadModel",
            params = mapOf(
                "modelPath" to whisperModelPath,
                "useGPU" to false
            )
        )

        if (loadSTTModelResult.isError) {
            throw RuntimeException("Failed to load STT model: ${loadSTTModelResult.error}")
        }

        val speechStartedListener = Switchboard.addEventListener(
            objectId = "vadNode",
            eventName = "speechStarted"
        ) { _, _ ->
            // Speech started
        }
        speechStartedListener.value?.let { vadEventListeners.add(it) }

        val speechEndedListener = Switchboard.addEventListener(
            objectId = "vadNode",
            eventName = "speechEnded"
        ) { _, _ ->
            // Speech ended
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
            throw RuntimeException("Failed to start engine")
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
