package com.synervoz.switchboardandroiddemo.ui.examples.sherpatts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.synervoz.switchboardandroiddemo.R

class SherpaTTSFragment : Fragment() {
    private lateinit var example: SherpaTTSExample
    private lateinit var editText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.tts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        example = SherpaTTSExample(requireContext())

        editText = view.findViewById(R.id.editText)
        val button = view.findViewById<Button>(R.id.btnRun)
        button.text = "Synthesize"
        button.setOnClickListener {
            if (editText.text.toString().isNotBlank()) {
                example.synthesize(editText.text.toString())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        example.close()
    }
}