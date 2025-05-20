//
// Created by Iván Nádor on 2025. 04. 17..
//

#include <jni.h>
#include <string>
#include <android/log.h>

#include <filesystem>
#include <fstream>
#include <iostream>
#include <switchboard/SwitchboardV3.hpp>

using namespace switchboard;

static std::string engineID;

static std::optional<std::string> readContentsOfTextFile(const std::string &filePath) {
    std::filesystem::path fileSystemPath(filePath);
    if (!std::filesystem::exists(filePath)) {
        return std::nullopt;
    }
    std::ifstream fileStream(filePath);
    std::string fileContent((std::istreambuf_iterator<char>(fileStream)),
                            std::istreambuf_iterator<char>());
    return fileContent;
}

extern "C" JNIEXPORT void JNICALL
Java_com_synervoz_switchboardandroiddemo_ui_examples_whisperstttosherpatts_WhisperSTTtoSherpaTTSExample_createEngine(
        JNIEnv *env,
        jobject instance,
        jstring dataDirectoryPath,
        jstring json) {

    const char* nativeDataDirectoryPath = env->GetStringUTFChars(dataDirectoryPath, nullptr);
    const char* nativeJson = env->GetStringUTFChars(json, nullptr);

    std::string jsonPath = std::string(nativeDataDirectoryPath) + "/" + std::string(nativeJson);

    auto engineJSON = readContentsOfTextFile(jsonPath);
    Config sdkConfig({{"appID",     "demo"},
                      {"appSecret", "demo"}});
    SwitchboardV3::initialize(sdkConfig);

    Result<SwitchboardV3::ObjectID> result = SwitchboardV3::createEngine(engineJSON.value());
    if (result.isError()) {
        env->ReleaseStringUTFChars(dataDirectoryPath, nativeDataDirectoryPath);
        env->ReleaseStringUTFChars(json, nativeJson);
        return;
    }
    engineID = result.value().value();
    std::string modelPath = std::string(nativeDataDirectoryPath) +
            "/en_GB/vits-piper-en_GB-southern_english_female-low/en_GB-southern_english_female-low.with_runtime_opt.ort";
    std::string tokensPath = std::string(nativeDataDirectoryPath) +
            "/en_GB/vits-piper-en_GB-southern_english_female-low/tokens.txt";
    std::string dataPath = std::string(nativeDataDirectoryPath) +
            "/en_GB/vits-piper-en_GB-southern_english_female-low/espeak-ng-data";
    auto loadModelResult = SwitchboardV3::callAction("ttsNode", "loadModel",
                                                     {{ "modelPath", modelPath },
                                                      { "tokensPath", tokensPath }, { "dataPath", dataPath }});

    std::string whisperModelPath = std::string(nativeDataDirectoryPath) +
                                   "/ggml-tiny.en.bin";
    auto result1 = SwitchboardV3::callAction("sttNode", "loadModel",
                                             {{ "modelPath", whisperModelPath }, { "useGPU", true }});

    std::string sileroModelPath = std::string(nativeDataDirectoryPath) +
                                  "/silero_vad.onnx";
    auto result2 = SwitchboardV3::callAction("vadNode", "loadModel",
                                             {{ "modelPath", sileroModelPath }});

    SwitchboardV3::addEventListener("vadNode", "start", [](const std::any& data) {
        __android_log_print(ANDROID_LOG_INFO, "WhisperSTTtoSherpaTTSExample", "vadNode start");
    });
    SwitchboardV3::addEventListener("vadNode", "end", [](const std::any& data) {
        __android_log_print(ANDROID_LOG_INFO, "WhisperSTTtoSherpaTTSExample", "vadNode end");
    });

    env->ReleaseStringUTFChars(dataDirectoryPath, nativeDataDirectoryPath);
    env->ReleaseStringUTFChars(json, nativeJson);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_synervoz_switchboardandroiddemo_ui_examples_whisperstttosherpatts_WhisperSTTtoSherpaTTSExample_startEngine(
        JNIEnv *env,
        jobject instance) {

    auto startEngineResult = SwitchboardV3::callAction(engineID, "start");
    if (startEngineResult.isError()) {
        return false;
    }
    return true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_synervoz_switchboardandroiddemo_ui_examples_whisperstttosherpatts_WhisperSTTtoSherpaTTSExample_stopEngine(
        JNIEnv *env,
        jobject instance) {
    auto stopEngineResult = SwitchboardV3::callAction(engineID, "stop");
    if (stopEngineResult.isError()) {
        return true;
    }
    return false;
}
