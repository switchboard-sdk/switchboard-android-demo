# SwitchboardAndroidDemo

A simple speech-to-text and text-to-speech demo application for Android.

## Setup

If dependencies are not included, do the following:

1. Create directory named `libs` in the project root.
2. Obtain/build and copy the following archives in `libs`:
    - `SwitchboardSDK.aar`
    - `SwitchboardOnnx.aar`
    - `SwitchboardSherpa.aar`
    - `SwitchboardWhisper.aar`
    - `SwitchboardSileroVAD.aar`
    - SwitchboardSDK C++ include directory
3. Create a `model` directory under `app/src/main/assets`.
4. Copy all the model and asset files from SwitchboardSherpa assets into `model`.
5. Copy `ggml-tiny.en.bin` from the SwitchboardWhisper assets into `model`.
6. Copy `silero_vad.onnx` from the SwitchboardSileroVAD assets into `model`.

## Build & Run

Open the the project folder in Android Studio.
Run the application.
