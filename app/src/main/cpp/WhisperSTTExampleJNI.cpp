//
// Created by Iván Nádor on 2025. 04. 18..
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

    std::string whisperModelPath = std::string(nativeDataDirectoryPath) +
            "/ggml-tiny.en.bin";
    auto result1 = SwitchboardV3::callAction("sttNode", "loadModel",
                                                     {{ "modelPath", whisperModelPath }, { "useGPU", true }});

    std::string sileroModelPath = std::string(nativeDataDirectoryPath) +
                                   "/silero_vad.onnx";
    auto result2 = SwitchboardV3::callAction("vadNode", "loadModel",
                                                     {{ "modelPath", sileroModelPath }});

    SwitchboardV3::addEventListener("vadNode", "start", [](const std::any& data) {
        __android_log_print(ANDROID_LOG_INFO, "WhisperSTTExample", "vadNode start");
    });
    SwitchboardV3::addEventListener("vadNode", "end", [](const std::any& data) {
        __android_log_print(ANDROID_LOG_INFO, "WhisperSTTExample", "vadNode end");
    });

    env->GetJavaVM(&jvm);
    javaObject = env->NewGlobalRef(instance);
    jclass javaClass = env->GetObjectClass(instance);
    onTranscriptionUpdateMethodId = env->GetMethodID(javaClass, "onTranscriptionUpdate", "(Ljava/lang/String;)V");

    SwitchboardV3::addEventListener("sttNode", "transcription", [](const std::any& data) {
        __android_log_print(ANDROID_LOG_INFO, "WhisperSTTExample", "transcription");
        const auto text = Config::toString(data);
        JNIEnv* env = getThreadLocalEnv(jvm);
        env->CallVoidMethod(javaObject, onTranscriptionUpdateMethodId, env->NewStringUTF(text.c_str()));
    });

    env->ReleaseStringUTFChars(dataDirectoryPath, nativeDataDirectoryPath);
    env->ReleaseStringUTFChars(json, nativeJson);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_synervoz_switchboardandroiddemo_ui_examples_whisperstt_WhisperSTTExample_startEngine(
        JNIEnv *env,
        jobject instance) {

    auto startEngineResult = SwitchboardV3::callAction(engineID, "start");
    if (startEngineResult.isError()) {
        return false;
    }
    return true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_synervoz_switchboardandroiddemo_ui_examples_whisperstt_WhisperSTTExample_stopEngine(
        JNIEnv *env,
        jobject instance) {
    auto stopEngineResult = SwitchboardV3::callAction(engineID, "stop");
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
