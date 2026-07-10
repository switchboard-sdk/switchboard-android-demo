package com.synervoz.switchboardandroiddemo.ui.examples.whisperstt

import android.content.Context
import com.synervoz.switchboard.sdk.Switchboard
import com.synervoz.switchboardwhisper.TranscriptionInterface

class WhisperSTTExample(private val context: Context) {
    var onTranscriptionUpdate: TranscriptionInterface? = null

    private var engineId: String? = null
    private val sttEventListeners = mutableListOf<Int>()

    var isRunning: Boolean = false
        private set

    init {
        createEngine(context)
    }

    private fun createEngine(context: Context) {
        val configJson = context.assets.open("STTExample.json").readBytes().decodeToString()

        val result = Switchboard.createEngine(configJson)
        if (result.isError) {
            throw RuntimeException("Failed to create engine")
        }

        engineId = result.value

        val whisperModelPath = "${context.filesDir}/ggml-tiny.en.bin"

        val loadModelResult = Switchboard.callAction(
            objectId = "sttNode",
            actionName = "loadModel",
            params = mapOf(
                "modelPath" to whisperModelPath,
                "useGPU" to false
            )
        )

        if (loadModelResult.isError) {
            throw RuntimeException("Failed to load model: ${loadModelResult.error}")
        }

        val transcriptionListener = Switchboard.addEventListener(
            objectId = "sttNode",
            eventName = "transcribed"
        ) { _, eventData ->
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
        sttEventListeners.forEach { listenerId ->
            Switchboard.removeEventListener("sttNode", listenerId)
        }
        sttEventListeners.clear()
    }
}
