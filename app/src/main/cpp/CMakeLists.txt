
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.19)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Declares and names the project.
set(MODULE_NAME "SwitchboardAndroidDemo")
project(${MODULE_NAME})

add_library(${MODULE_NAME} SHARED "")

target_sources(${MODULE_NAME} PRIVATE WhisperSTTExampleJNI.cpp SherpaTTSExampleJNI.cpp WhisperSTTtoSherpaTTSExampleJNI.cpp)

# Find the SwitchboardSDK AAR prefab package with built-in libraries
find_package(SwitchboardSDK REQUIRED CONFIG)
target_link_libraries(${MODULE_NAME} SwitchboardSDK::SwitchboardSDK)
target_include_directories(${MODULE_NAME} PRIVATE ${PROJECT_SOURCE_DIR}/../../../../libs/include)

find_package(SwitchboardSherpa REQUIRED CONFIG)
target_link_libraries(${MODULE_NAME} SwitchboardSherpa::SwitchboardSherpa)

find_package(SwitchboardOnnx REQUIRED CONFIG)
target_link_libraries(${MODULE_NAME} SwitchboardOnnx::SwitchboardOnnx)

find_package(SwitchboardSileroVAD REQUIRED CONFIG)
target_link_libraries(${MODULE_NAME} SwitchboardSileroVAD::SwitchboardSileroVAD)

find_package(SwitchboardWhisper REQUIRED CONFIG)
target_link_libraries(${MODULE_NAME} SwitchboardWhisper::SwitchboardWhisper)

find_library(log-lib log)
target_link_libraries(${MODULE_NAME} ${log-lib})
