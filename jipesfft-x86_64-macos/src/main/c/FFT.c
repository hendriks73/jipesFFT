/*
 * =================================================
 * Copyright 2008 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <CoreServices/CoreServices.h>
#include <Accelerate/Accelerate.h>

#include <sys/sysctl.h>

#include "com_tagtraum_jipesfft_FFT.h"


/*
 * Class:     com_tagtraum_jipesfft_FFT
 * Method:    init
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_com_tagtraum_jipesfft_FFT_init
(JNIEnv *env, jclass clazz, jint numberOfSamples) {

    if (numberOfSamples < 4) {
        jclass excCls = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
        (*env)->ThrowNew(env, excCls, "Number of samples is less than 4");
        return 0;
	}
    if (numberOfSamples & (numberOfSamples - 1)) {
        jclass excCls = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
        (*env)->ThrowNew(env, excCls, "Number of samples is not a power of 2");
        return 0;
    }

    FFTSetup setup = vDSP_create_fftsetup((int)log2(numberOfSamples), FFT_RADIX2);
    if (setup == NULL) {
        jclass excCls = ( * env) -> FindClass(env, "java/lang/OutOfMemoryError");
        ( * env) -> ThrowNew(env, excCls, "FFT_Setup failed to allocate enough memory for the real FFT");
        return 0;
    }
    return (jlong)setup;
}


/*
 * Class:     com_tagtraum_jipesfft_FFT
 * Method:    destroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_tagtraum_jipesfft_FFT_destroy
(JNIEnv *env, jclass clazz, jlong fftSetupPointer) {
    if (fftSetupPointer != 0) {
        vDSP_destroy_fftsetup((FFTSetup)fftSetupPointer);
    }
}


JNIEXPORT jobjectArray JNICALL Java_com_tagtraum_jipesfft_FFT_realFFT
(JNIEnv *env, jclass clazz, jlong fftSetupPointer, jboolean forward, jint numberOfSamples, jfloatArray jrealIn, jfloatArray jimagIn) {

    int direction;
	DSPSplitComplex A;
    FFTSetup fftSetup = NULL;
    UInt32        log2n = (int)log2(numberOfSamples);
    UInt32        nOver2 = numberOfSamples / 2; // half of n as real part and imag part.
    SInt32        stride = 1;
    float         realData[numberOfSamples] __attribute__ ((aligned (16)));
    float         real[numberOfSamples] __attribute__ ((aligned (16)));
    float         imag[numberOfSamples] __attribute__ ((aligned (16)));


    if ((*env)->GetArrayLength(env, jrealIn) < numberOfSamples) {
        jclass excCls = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
        (*env)->ThrowNew(env, excCls, "Number of samples must not be less than input array length");
        return NULL;
    }

    // clear/init in-place arrays
    vDSP_vclr(real, 1, numberOfSamples);
    vDSP_vclr(imag, 1, numberOfSamples);

	A.realp = real;
	A.imagp = imag;

    if (forward) {
        (*env)->GetFloatArrayRegion(env, jrealIn, 0, numberOfSamples, realData);
        direction = FFT_FORWARD;
        vDSP_ctoz((COMPLEX *) realData, 2, &A, 1, nOver2);
    } else {
        direction = FFT_INVERSE;
        (*env)->GetFloatArrayRegion(env, jimagIn, 0, numberOfSamples, imag);
        (*env)->GetFloatArrayRegion(env, jrealIn, 0, numberOfSamples, real);
        // place data in packed complex format (see http://developer.apple.com/hardwaredrivers/ve/downloads/vDSP_Library.pdf)
        imag[0] = real[nOver2];
    }

	if ((*env)->ExceptionOccurred(env)) {
	    return NULL;
	}

    fftSetup = (FFTSetup)fftSetupPointer;
    // perform FFT
	vDSP_fft_zrip(fftSetup, &A, stride, log2n, direction);    
    
    jobjectArray jtransformed = (*env)->NewObjectArray(env, 2, (*env)->FindClass(env, "[F"), NULL);
    if (jtransformed == NULL) {
        jclass excCls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
        (*env)->ThrowNew(env, excCls, "Failed to allocate jtransformed");
        return NULL;
    }

    if (forward) {
        // scale it..
        float scale = (float)1.0/2.0;
        vDSP_vsmul(A.realp, 1, &scale, A.realp, 1, nOver2);
        vDSP_vsmul(A.imagp, 1, &scale, A.imagp, 1, nOver2);

        // create real array
        {
            jfloatArray jreal = (*env)->NewFloatArray(env, numberOfSamples);
            if (jreal == NULL) {
                jclass excCls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
                (*env)->ThrowNew(env, excCls, "Failed to allocate jreal");
                return NULL;
            }
            A.realp[nOver2] = A.imagp[0];
            int from = nOver2-1;
            int to = nOver2+1;
            for (; to<numberOfSamples; to++, from--) {
                A.realp[to] = A.realp[from];
            }
            (*env)->SetFloatArrayRegion(env, jreal, 0, numberOfSamples, A.realp);
            (*env)->SetObjectArrayElement(env, jtransformed, 0, jreal);
        }

        // create imaginary array
        {
            jfloatArray jimag = (*env)->NewFloatArray(env, numberOfSamples);
            if (jimag == NULL) {
                jclass excCls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
                (*env)->ThrowNew(env, excCls, "Failed to allocate jimag");
                return NULL;
            }
            A.imagp[0] = 0;
            int from = nOver2-1;
            int to = nOver2+1;
            for (; to<numberOfSamples; to++, from--) {
                A.imagp[to] = -A.imagp[from];
            }
            (*env)->SetFloatArrayRegion(env, jimag, 0, numberOfSamples, A.imagp);
            (*env)->SetObjectArrayElement(env, jtransformed, 1, jimag);
        }
    } else {
        // inverse
        // scale it..
        float scale = (float)1.0/numberOfSamples;
        vDSP_vsmul(A.realp, 1, &scale, A.realp, 1, nOver2);
        vDSP_vsmul(A.imagp, 1, &scale, A.imagp, 1, nOver2);
        vDSP_ztoc(&A, 1, (COMPLEX *) realData, 2, nOver2);

        // create real array
        jfloatArray jreal = (*env)->NewFloatArray(env, numberOfSamples);
        if (jreal == NULL) {
            jclass excCls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
            (*env)->ThrowNew(env, excCls, "Failed to allocate jreal");
            return NULL;
        }
        (*env)->SetFloatArrayRegion(env, jreal, 0, numberOfSamples, realData);
        (*env)->SetObjectArrayElement(env, jtransformed, 0, jreal);

        // add empty imag array
        jfloatArray jimag = (*env)->NewFloatArray(env, numberOfSamples);
        if (jimag == NULL) {
            jclass excCls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
            (*env)->ThrowNew(env, excCls, "Failed to allocate jimag");
            return NULL;
        }
        (*env)->SetObjectArrayElement(env, jtransformed, 1, jimag);
    }

	return jtransformed;
}

