#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <math.h>
#include "FFT.h"

int ** gFFTBitTable = NULL;
const int maxFastBits = 16;

static bool isPowerOfTwo(int x);
static int numberOfBitsNeeded(int powerOfTwo);
static int reverseBits(int index, int numBits);

bool isPowerOfTwo(int x) {
    if (x < 2) return false;
    if (x & (x - 1)) return false;
    return true;
}

int numberOfBitsNeeded(int powerOfTwo) {
    int i;
    if (powerOfTwo < 2) {
        fprintf(stderr, "Error: FFT called with size %d\n", powerOfTwo);
        exit(1);
    }
    for (i = 0;; i++) {
        if (powerOfTwo & (1 << i)) {
            return i;
        }
    }
}

int reverseBits(int index, int numBits) {
    int i, rev;
    for (i = rev = 0; i < numBits; i++) {
        rev = (rev << 1) | (index & 1);
        index >>= 1;
    }
    return rev;
}

void init() {
    gFFTBitTable = malloc(sizeof(int * ) * maxFastBits);

    int len = 2;
    int b = 1;
    int i = 1;
    for (b = 1; b <= maxFastBits; b++) {
        gFFTBitTable[b - 1] = malloc(sizeof(int) * len);
        for (i = 0; i < len; i++) {
            gFFTBitTable[b - 1][i] = reverseBits(i, b);
        }
        len <<= 1;
    }
}

inline int fastReverseBits(int i, int numBits) {
    if (numBits <= maxFastBits)
        return gFFTBitTable[numBits - 1][i];
    else
        return reverseBits(i, numBits);
}

/**
 * Complex Fast Fourier Transform.
 */
void fft(int numSamples,
        bool inverse,
        float * realIn, float * imagIn,
        float * realOut, float * imagOut) {

    double angle_numerator = 2.0 * M_PI;

    // Number of bits needed to store indices
    int numBits;
    int i, j, k, n;
    int blockSize, blockEnd;

    // temp real, temp imaginary
    double tr, ti;

    if (!isPowerOfTwo(numSamples)) {
        fprintf(stderr, "%d is not a power of two\n", numSamples);
        return;
    }

    if (inverse) {
        angle_numerator = -angle_numerator;
    }

    numBits = numberOfBitsNeeded(numSamples);

    // do simultaneous data copy and bit-reversal ordering into outputs.
    for (i = 0; i < numSamples; i++) {
        j = fastReverseBits(i, numBits);
        realOut[j] = realIn[i];
        imagOut[j] = (imagIn == NULL) ? 0.0 : imagIn[i];
    }

    // FFT itself
    blockEnd = 1;
    for (blockSize = 2; blockSize <= numSamples; blockSize <<= 1) {

        double delta_angle = angle_numerator / (double) blockSize;

        double sm2 = -sin(-2 * delta_angle);
        double sm1 = -sin(-delta_angle);
        double cm2 = cos(-2 * delta_angle);
        double cm1 = cos(-delta_angle);
        double w = 2 * cm1;
        double ar0, ar1, ar2, ai0, ai1, ai2;

        for (i = 0; i < numSamples; i += blockSize) {
            ar2 = cm2;
            ar1 = cm1;

            ai2 = sm2;
            ai1 = sm1;

            for (j = i, n = 0; n < blockEnd; j++, n++) {
                ar0 = w * ar1 - ar2;
                ar2 = ar1;
                ar1 = ar0;

                ai0 = w * ai1 - ai2;
                ai2 = ai1;
                ai1 = ai0;

                k = j + blockEnd;
                tr = ar0 * realOut[k] - ai0 * imagOut[k];
                ti = ar0 * imagOut[k] + ai0 * realOut[k];

                realOut[k] = realOut[j] - tr;
                imagOut[k] = imagOut[j] - ti;

                realOut[j] += tr;
                imagOut[j] += ti;
            }
        }

        blockEnd = blockSize;
    }

    /**
     * Normalize, if inverse transform.
     */
    if (inverse) {
        float denom = (float) numSamples;

        for (i = 0; i < numSamples; i++) {
            realOut[i] /= denom;
            imagOut[i] /= denom;
        }
    }
}