//
// Created by DC\ttv16aleksim on 11/20/18.
//

#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_aleksi_ndk_1app_MainActivity_testFunc(JNIEnv *env, jobject instance) {
    const char* returnValue = "KAKKA";
    return env->NewStringUTF(returnValue);
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_example_aleksi_ndk_1app_MainActivity_returnInt(JNIEnv *env, jobject instance) {
    jint val = 2*2;
    return val;
}