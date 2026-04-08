package com.synervoz.switchboardandroiddemo.ui.examples.whisperstt

import android.content.Context
import android.util.Log
import com.synervoz.switchboard.sdk.Switchboard
import com.synervoz.switchboardonnx.OnnxExtension
import com.synervoz.switchboardsilerovad.SileroVADExtension
import com.synervoz.switchboardwhisper.TranscriptionInterface
import com.synervoz.switchboardwhisper.WhisperExtension

class WhisperSTTExample(private val context: Context) {
    var onTranscriptionUpdate: TranscriptionInterface? = null

    private var engineId: String? = null
    private val sttEventListeners = mutableListOf<Int>()
    private val vadEventListeners = mutableListOf<Int>()

    var isRunning: Boolean = false
        private set

    init {
        createEngine(context)
    }

    private fun createEngine(context: Context) {
        WhisperExtension.load()
        SileroVADExtension.load()

        val initResult = Switchboard.initialize(
            context = context,
            appId = "demo",
            appSecret = "demo",
            extensions = mapOf(
                "Whisper" to emptyMap<String, Any>(),
                "SileroVAD" to emptyMap<String, Any>()
            )
        )

        if (initResult.isError) {
            Log.e("WhisperSTTExample", "Failed to initialize Switchboard SDK")
            return
        }

        val configJson = context.assets.open("STTExample.json").readBytes().decodeToString()

        val result = Switchboard.createEngine(configJson)
        if (result.isError) {
            Log.e("WhisperSTTExample", "Failed to create engine")
            return
        }

        engineId = result.value

        val whisperModelPath = "${context.filesDir}/ggml-tiny.en.bin"
        Log.i("WhisperSTTExample", "Loading Whisper model from: $whisperModelPath (size: ${modelFile.length()} bytes)")
        val loadModelResult = Switchboard.callAction(
            objectId = "sttNode",
            actionName = "loadModel",
            params = mapOf(
                "modelPath" to whisperModelPath,
                "useGPU" to false
            )
        )

        if (loadModelResult.isError) {
            Log.e("WhisperSTTExample", "Failed to load model: ${loadModelResult.error}")
            return
        }

        val speechStartedListener = Switchboard.addEventListener(
            objectId = "vadNode",
            eventName = "speechStarted"
        ) { _, _ ->
            Log.i("WhisperSTTExample", "vadNode start")
        }
        speechStartedListener.value?.let { vadEventListeners.add(it) }

        val speechEndedListener = Switchboard.addEventListener(
            objectId = "vadNode",
            eventName = "speechEnded"
        ) { _, _ ->
            Log.i("WhisperSTTExample", "vadNode end")
        }
        speechEndedListener.value?.let { vadEventListeners.add(it) }

        val transcriptionListener = Switchboard.addEventListener(
            objectId = "sttNode",
            eventName = "transcribed"
        ) { _, eventData ->
            Log.i("WhisperSTTExample", "transcribed")
            handleTranscription(eventData)
        }
        transcriptionListener.value?.let { sttEventListeners.add(it) }
    }

    private fun handleTranscription(eventData: Any?) {
        val data = eventData as? Map<String, Any> ?: return
        val text = data["text"] as? String ?: ""
        val processingTime = (data["processingTime"] as? Number)?.toLong() ?: -1L

        onTranscriptionUpdate?.onTranscriptionUpdate(text, processingTime)
    }

    fun close() {
        stop()
        cleanup()
    }

    fun start() {
        val engineId = this.engineId ?: return

        val whisperModelPath = "${context.filesDir}/ggml-tiny.en.bin"

        val modelFile = java.io.File(whisperModelPath)
        if (!modelFile.exists()) {
            Log.e("WhisperSTTExample", "Model file does not exist at: $whisperModelPath")
            return
        }

        Log.i("WhisperSTTExample", "Loading Whisper model from: $whisperModelPath")
        val loadModelResult = Switchboard.callAction(
            objectId = "sttNode",
            actionName = "loadModel",
            params = mapOf(
                "modelPath" to whisperModelPath,
                "useGPU" to false
            )
        )

        if (loadModelResult.isError) {
            Log.e("WhisperSTTExample", "Failed to load model: ${loadModelResult.error}")
            return
        }

        val startResult = Switchboard.callAction(engineId, "start")
        if (startResult.isError) {
            Log.e("WhisperSTTExample", "Failed to start engine")
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

        sttEventListeners.forEach { listenerId ->
            Switchboard.removeEventListener("sttNode", listenerId)
        }
        sttEventListeners.clear()
    }
}
