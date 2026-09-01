cmake_minimum_required(VERSION 3.18.1)
project(stockfishjni)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

file(GLOB STOCKFISH_SOURCES CONFIGURE_DEPENDS
        "${CMAKE_CURRENT_SOURCE_DIR}/stockfish/*.cpp"
        "${CMAKE_CURRENT_SOURCE_DIR}/stockfish/nnue/*.cpp"
        "${CMAKE_CURRENT_SOURCE_DIR}/stockfish/nnue/features/*.cpp"
        "${CMAKE_CURRENT_SOURCE_DIR}/stockfish/syzygy/*.cpp"
)
list(FILTER STOCKFISH_SOURCES EXCLUDE REGEX ".*/main\\.cpp$")

add_library(stockfishjni SHARED
        bridge.cpp
        ${STOCKFISH_SOURCES}
)

target_include_directories(stockfishjni PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/stockfish
)

target_compile_definitions(stockfishjni PRIVATE
        NDEBUG
        NNUE_EMBEDDING_OFF
        NO_PREFETCH
)

if(ANDROID_ABI STREQUAL "arm64-v8a" OR ANDROID_ABI STREQUAL "x86_64")
    target_compile_definitions(stockfishjni PRIVATE IS_64BIT)
endif()

target_compile_options(stockfishjni PRIVATE
        -O3
        -fno-exceptions
        -fno-rtti
        -Wno-unused-function
        -Wno-unused-variable
)

target_link_libraries(stockfishjni
        android
        log
        atomic
)
