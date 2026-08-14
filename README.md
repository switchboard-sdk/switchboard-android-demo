# SwitchboardAndroidDemo

A simple speech-to-text and text-to-speech demo application for Android.

The app targets **SwitchboardSDK 3.2.5**, uses the **v3 JSON audio-graph API**, and is
built with **Jetpack Compose** for the UI. It includes three examples:

- **Sherpa TTS** — synthesize typed text to speech.
- **Whisper STT** — transcribe microphone input to text.
- **Whisper STT to Sherpa TTS** — transcribe speech and synthesize it back.

## Setup

1. Create a `model` directory under `app/src/main/assets`.
2. Copy all the model and asset files from SwitchboardSherpa assets into `model`.
3. Copy `ggml-tiny.en.bin` from the SwitchboardWhisper assets into `model`.

The SwitchboardSDK and its extensions are declared as Gradle dependencies (see
`app/build.gradle`) and resolved automatically from Maven when you build.

## Build & Run

Open the project folder in Android Studio and run the application, or build from the
command line:

```
./gradlew assembleDebug
```
