//
// Created by Iván Nádor on 2025. 04. 18..
//

#include <jni.h>
#include <string>
#include <android/log.h>

#include <filesystem>
#include <fstream>
#include <iostream>
#include <switchboard/Switchboard.hpp>
#include <switchboard_core/ExtensionManager.hpp>
#include <switchboard_core/Logger.hpp>
#include <WhisperExtension.hpp>
#include <OnnxExtension.hpp>
#include <SileroVADExtension.hpp>

using namespace switchboard;

static std::string engineID;
static jobject javaObject;
static JavaVM* jvm;
static jmethodID onTranscriptionUpdateMethodId;

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

static JNIEnv* getThreadLocalEnv(JavaVM* jvm) {
    JNIEnv* environment;
    int getEnvStat = jvm->GetEnv((void**)&environment, JNI_VERSION_1_6);
    if (getEnvStat == JNI_EDETACHED) {
        jvm->AttachCurrentThread(&environment, NULL);
    }
    return environment;
}

extern "C" JNIEXPORT void JNICALL
Java_com_synervoz_switchboardandroiddemo_ui_examples_whisperstt_WhisperSTTExample_createEngine(
        JNIEnv *env,
        jobject instance,
        jstring dataDirectoryPath,
        jstring json) {

    const char* nativeDataDirectoryPath = env->GetStringUTFChars(dataDirectoryPath, nullptr);
    const char* nativeJson = env->GetStringUTFChars(json, nullptr);

    std::string jsonPath = std::string(nativeDataDirectoryPath) + "/" + std::string(nativeJson);

    auto engineJSON = readContentsOfTextFile(jsonPath);

    extensions::whisper::WhisperExtension::load();
    extensions::onnx::OnnxExtension::load();
    extensions::silerovad::SileroVADExtension::load();

    SBAnyMap extensionsConfig;
    extensionsConfig["Whisper"] = SBAnyMap();
    extensionsConfig["Onnx"] = SBAnyMap();
    extensionsConfig["SileroVAD"] = SBAnyMap();

    SBAnyMap sdkConfig = {{"appID",     "demo"},
                        {"appSecret", "demo"},
                        {"extensions", extensionsConfig}};

    Switchboard::initialize(sdkConfig);

    Result<Switchboard::ObjectID> result = Switchboard::createEngine(engineJSON.value());
    if (result.isError()) {
        env->ReleaseStringUTFChars(dataDirectoryPath, nativeDataDirectoryPath);
        env->ReleaseStringUTFChars(json, nativeJson);
        return;
    }
    engineID = result.value();

    std::string whisperModelPath = std::string(nativeDataDirectoryPath) +
            "/ggml-tiny.en.bin";
    auto result1 = Switchboard::callAction("sttNode", "loadModel",
                                                     {{ "modelPath", whisperModelPath }, { "useGPU", true }});

    std::string sileroModelPath = std::string(nativeDataDirectoryPath) +
                                   "/silero_vad.onnx";
    auto result2 = Switchboard::callAction("vadNode", "loadModel",
                                                     {{ "modelPath", sileroModelPath }});

    Switchboard::addEventListener("vadNode", "speechStarted", [](const Event& event) {
        __android_log_print(ANDROID_LOG_INFO, "WhisperSTTExample", "vadNode start");
    });
    Switchboard::addEventListener("vadNode", "speechEnded", [](const Event& event) {
        __android_log_print(ANDROID_LOG_INFO, "WhisperSTTExample", "vadNode end");
    });

    env->GetJavaVM(&jvm);
    javaObject = env->NewGlobalRef(instance);
    jclass javaClass = env->GetObjectClass(instance);
    onTranscriptionUpdateMethodId = env->GetMethodID(javaClass, "onTranscriptionUpdate", "(Ljava/lang/String;J)V");

    Switchboard::addEventListener("sttNode", "transcribed", [](const Event& event) {
        __android_log_print(ANDROID_LOG_INFO, "WhisperSTTExample", "transcribed");
        const auto params = SBAny::convert<SBAnyMap>(event.data);
        const auto text = SBAny::convert<std::string>(params.at("text"));
        const auto processingTime = SBAny::convert<int>(params.at("processingTime"));
        JNIEnv* env = getThreadLocalEnv(jvm);
        env->CallVoidMethod(javaObject, onTranscriptionUpdateMethodId, env->NewStringUTF(text.c_str()), static_cast<long>(processingTime));
    });

    env->ReleaseStringUTFChars(dataDirectoryPath, nativeDataDirectoryPath);
    env->ReleaseStringUTFChars(json, nativeJson);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_synervoz_switchboardandroiddemo_ui_examples_whisperstt_WhisperSTTExample_startEngine(
        JNIEnv *env,
        jobject instance) {

    auto startEngineResult = Switchboard::callAction(engineID, "start");
    if (startEngineResult.isError()) {
        return false;
    }
    return true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_synervoz_switchboardandroiddemo_ui_examples_whisperstt_WhisperSTTExample_stopEngine(
        JNIEnv *env,
        jobject instance) {
    auto stopEngineResult = Switchboard::callAction(engineID, "stop");
    if (stopEngineResult.isError()) {
        return true;
    }
    return false;
}

extern "C" JNIEXPORT void JNICALL
Java_com_synervoz_switchboardandroiddemo_ui_examples_whisperstt_WhisperSTTExample_closeEngine(
        JNIEnv *env,
        jobject instance) {
    if (javaObject && jvm) {
        JNIEnv* env = getThreadLocalEnv(jvm);
        env->DeleteGlobalRef(javaObject);
        javaObject = nullptr;
    }
}
