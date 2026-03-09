//
// Created by Iván Nádor on 2025. 04. 17..
//

#include <jni.h>
#include <string>
#include <android/log.h>

#include <filesystem>
#include <fstream>
#include <iostream>
#include <switchboard/Switchboard.hpp>

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
Java_com_synervoz_switchboardandroiddemo_ui_examples_sherpatts_SherpaTTSExample_createEngine(
        JNIEnv *env,
        jobject instance,
        jstring dataDirectoryPath,
        jstring json) {

    const char* nativeDataDirectoryPath = env->GetStringUTFChars(dataDirectoryPath, nullptr);
    const char* nativeJson = env->GetStringUTFChars(json, nullptr);

    std::string jsonPath = std::string(nativeDataDirectoryPath) + "/" + std::string(nativeJson);

    auto engineJSON = readContentsOfTextFile(jsonPath);
    SBAnyMap sdkConfig({{"appID",     "demo"},
                        {"appSecret", "demo"}});
    Switchboard::initialize(sdkConfig);

    Result<Switchboard::ObjectID> result = Switchboard::createEngine(engineJSON.value());
    if (result.isError()) {
        env->ReleaseStringUTFChars(dataDirectoryPath, nativeDataDirectoryPath);
        env->ReleaseStringUTFChars(json, nativeJson);
        return;
    }
    engineID = result.value();

    std::string modelPath = std::string(nativeDataDirectoryPath) +
            "/en_GB/vits-piper-en_GB-southern_english_female-low/en_GB-southern_english_female-low.with_runtime_opt.ort";
    std::string tokensPath = std::string(nativeDataDirectoryPath) +
            "/en_GB/vits-piper-en_GB-southern_english_female-low/tokens.txt";
    std::string dataPath = std::string(nativeDataDirectoryPath) +
            "/en_GB/vits-piper-en_GB-southern_english_female-low/espeak-ng-data";
    auto loadModelResult = Switchboard::callAction("sherpaTTSNode", "loadModel",
                                                     {{ "modelPath", modelPath },
                                                      { "tokensPath", tokensPath }, { "dataPath", dataPath }});

    env->ReleaseStringUTFChars(dataDirectoryPath, nativeDataDirectoryPath);
    env->ReleaseStringUTFChars(json, nativeJson);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_synervoz_switchboardandroiddemo_ui_examples_sherpatts_SherpaTTSExample_startEngine(
        JNIEnv *env,
        jobject instance) {

    auto startEngineResult = Switchboard::callAction(engineID, "start");
    if (startEngineResult.isError()) {
        return false;
    }
    return true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_synervoz_switchboardandroiddemo_ui_examples_sherpatts_SherpaTTSExample_stopEngine(
        JNIEnv *env,
        jobject instance) {
    auto stopEngineResult = Switchboard::callAction(engineID, "stop");
    if (stopEngineResult.isError()) {
        return true;
    }
    return false;
}

extern "C" JNIEXPORT void JNICALL
Java_com_synervoz_switchboardandroiddemo_ui_examples_sherpatts_SherpaTTSExample_synthesizeText(
        JNIEnv *env,
        jobject instance,
        jstring text) {
    const char* nativeText = env->GetStringUTFChars(text, nullptr);
    auto synthesizeResult = Switchboard::callAction("sherpaTTSNode", "synthesize", { { "text", nativeText } });
}