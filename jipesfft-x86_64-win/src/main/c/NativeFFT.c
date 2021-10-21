/**
 * NativeFFT.c
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>
#include "FFT.h"
#include "com_tagtraum_jipesfft_FFT.h"


/*
 * Class:     com_tagtraum_jipesfft_FFT
 * Method:    init
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_com_tagtraum_jipesfft_FFT_init
    (JNIEnv * env, jclass clazz, jint numberOfSamples) {
    // do nothing
    return 1L;
}

/*
 * Class:     com_tagtraum_jipesfft_FFT
 * Method:    destroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_tagtraum_jipesfft_FFT_destroy
    (JNIEnv * env, jclass clazz, jlong fftSetupPointer) {
    // do nothing
}

JNIEXPORT jobjectArray JNICALL Java_com_tagtraum_jipesfft_FFT_realFFT
    (JNIEnv * env, jclass clazz, jlong setupPointer, jboolean forward, jint numberOfSamples, jfloatArray jrealIn, jfloatArray jimagIn) {

    float * realIn = NULL;
    float * imagIn = NULL;
    float * realOut = NULL;
    float * imagOut = NULL;

    if (numberOfSamples < 4) {
        jclass excCls = ( * env) -> FindClass(env, "java/lang/IllegalArgumentException");
        ( * env) -> ThrowNew(env, excCls, "Number of samples is less than 4.");
        return NULL;
    }
    if (numberOfSamples & (numberOfSamples - 1)) {
        jclass excCls = ( * env) -> FindClass(env, "java/lang/IllegalArgumentException");
        ( * env) -> ThrowNew(env, excCls, "Number of samples is not a power of 2.");
        return NULL;
    }
    if (( * env) -> GetArrayLength(env, jrealIn) < numberOfSamples) {
        jclass excCls = ( * env) -> FindClass(env, "java/lang/IllegalArgumentException");
        ( * env) -> ThrowNew(env, excCls, "Number of samples must not be less than input array length.");
        return NULL;
    }

    realIn = malloc(sizeof(float) * numberOfSamples);
    if (!realIn) {
        jclass excCls = ( * env) -> FindClass(env, "java/lang/OutOfMemoryError");
        ( * env) -> ThrowNew(env, excCls, "Failed to allocate realIn");
        goto BAIL;
    }
    imagIn = malloc(sizeof(float) * numberOfSamples);
    if (!imagIn) {
        jclass excCls = ( * env) -> FindClass(env, "java/lang/OutOfMemoryError");
        ( * env) -> ThrowNew(env, excCls, "Failed to allocate imagIn");
        goto BAIL;
    }
    realOut = malloc(sizeof(float) * numberOfSamples);
    if (!realOut) {
        jclass excCls = ( * env) -> FindClass(env, "java/lang/OutOfMemoryError");
        ( * env) -> ThrowNew(env, excCls, "Failed to allocate realOut");
        goto BAIL;
    }
    imagOut = malloc(sizeof(float) * numberOfSamples);
    if (!imagOut) {
        jclass excCls = ( * env) -> FindClass(env, "java/lang/OutOfMemoryError");
        ( * env) -> ThrowNew(env, excCls, "Failed to allocate imagOut");
        goto BAIL;
    }

    ( * env) -> GetFloatArrayRegion(env, jrealIn, 0, numberOfSamples, realIn);
    if (forward) {
        fft(numberOfSamples, false, realIn, NULL, realOut, imagOut);
    } else {
        ( * env) -> GetFloatArrayRegion(env, jimagIn, 0, numberOfSamples, imagIn);
        fft(numberOfSamples, true, realIn, imagIn, realOut, imagOut);
    }

    jfloatArray jreal = NULL;
    jfloatArray jimag = NULL;
    jobjectArray jtransformed = NULL;

    jtransformed = ( * env) -> NewObjectArray(env, 2, ( * env) -> FindClass(env, "[F"), NULL);

    if (jtransformed == NULL) {
        jclass excCls = ( * env) -> FindClass(env, "java/lang/OutOfMemoryError");
        ( * env) -> ThrowNew(env, excCls, "Failed to allocate jtransformed");
        goto BAIL;
    }

    jreal = ( * env) -> NewFloatArray(env, numberOfSamples);

    if (jreal == NULL) {
        jclass excCls = ( * env) -> FindClass(env, "java/lang/OutOfMemoryError");
        ( * env) -> ThrowNew(env, excCls, "Failed to allocate jreal");
        goto BAIL;
    }

    ( * env) -> SetFloatArrayRegion(env, jreal, 0, numberOfSamples, realOut);
    ( * env) -> SetObjectArrayElement(env, jtransformed, 0, jreal);
    jimag = ( * env) -> NewFloatArray(env, numberOfSamples);

    if (jimag == NULL) {
        jclass excCls = ( * env) -> FindClass(env, "java/lang/OutOfMemoryError");
        ( * env) -> ThrowNew(env, excCls, "Failed to allocate jimag");
        goto BAIL;
    }

    if (forward) {
        ( * env) -> SetFloatArrayRegion(env, jimag, 0, numberOfSamples, imagOut);
    }
    ( * env) -> SetObjectArrayElement(env, jtransformed, 1, jimag);

    BAIL:

    if (realOut) free(realOut);
    if (imagOut) free(imagOut);
    if (realIn) free(realIn);
    if (imagIn) free(imagIn);

    return jtransformed;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM * vm, void * reserved) {
    #ifdef DEBUG
    printf("JNI_OnLoad\n");
    #endif
    init();
    // Return the JNI version
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM * vm, void * reserved) {
    #ifdef DEBUG
    printf("JNI_OnUnload\n");
    #endif
}