package com.synervoz.switchboardandroiddemo.ui.examples.whisperstt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.synervoz.switchboardandroiddemo.R
import com.synervoz.switchboardwhisper.TranscriptionInterface

class WhisperSTTFragment : Fragment() {
    private lateinit var example: WhisperSTTExample
    private lateinit var textView: TextView
    private var transcription = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.stt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        example = WhisperSTTExample(requireContext())

        example.onTranscriptionUpdate =
            TranscriptionInterface { transcription: String, _: Long ->
                this.transcription = "${this.transcription} $transcription"
                textView.text = this.transcription
        }

        textView = view.findViewById(R.id.textView)
        textView.text = "Tap the Start button to start transcribing"
        val button = view.findViewById<Button>(R.id.btnRun)
        button.text = "Start"
        button.setOnClickListener {
            if (!example.isRunning) {
                button.text = "Stop"
                example.start()
            } else {
                button.text = "Start"
                example.stop()
                return@setOnClickListener
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        example.close()
    }
}