package com.synervoz.switchboardandroiddemo.ui.examples.whisperstttosherpatts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.synervoz.switchboardandroiddemo.R

class WhisperSTTtoSherpaTTSFragment : Fragment() {
    private lateinit var example: WhisperSTTtoSherpaTTSExample
    private lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.stt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        example = WhisperSTTtoSherpaTTSExample(requireContext())

        textView = view.findViewById(R.id.textView)
        textView.text = "Tap the Start button and talk in the microphone to syntesize your speech"
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
