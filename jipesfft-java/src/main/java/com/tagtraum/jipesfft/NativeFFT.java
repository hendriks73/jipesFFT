/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipesfft;

/**
 * Native implementation of FFT.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class NativeFFT {

    static {
        NativeLibraryLoader.loadLibrary();
    }

    private JavaFFT javaFFT;
    private final float[] frequencies;
    private final int numberOfSamples;
    private final long pointer;

    public NativeFFT(final int numberOfSamples) {
        if (numberOfSamples < 0) throw new IllegalArgumentException("Number of samples=" + numberOfSamples);
        this.numberOfSamples = numberOfSamples;
        this.frequencies = createFrequencies(numberOfSamples);
        this.pointer = init(numberOfSamples);
    }

    private JavaFFT getJavaFFT() {
        if (javaFFT == null) {
            javaFFT = new JavaFFT(numberOfSamples);
        }
        return javaFFT;
    }

    public float[][] inverseTransform(final float[] real, final float[] imaginary) throws UnsupportedOperationException {
        if (numberOfSamples > 32768) {
            return getJavaFFT().inverseTransform(real, imaginary);
        }
        return NativeFFT.realFFT(pointer, false, real.length, real, imaginary);
    }

    public float[][] transform(final float[] real) throws UnsupportedOperationException {
        if (numberOfSamples > 32768) {
            return getJavaFFT().transform(real);
        }
        final float[][] result = new float[3][];
        final float[][] nativeResult = NativeFFT.realFFT(pointer, true, real.length, real, null);
        result[0] = nativeResult[0];
        result[1] = nativeResult[1];
        result[2] = frequencies.clone();
        return result;
    }

    public float[][] transform(final float[] real, final float[] imaginary) throws UnsupportedOperationException {
        // TODO: native version!
        return getJavaFFT().transform(real, imaginary);
    }

    /**
     * Perform real FFT.
     *
     * @param forward indicates whether this is a forward or inverse DFT
     * @param numberOfSamples number of samples
     * @param realIn input array of floats
     * @param imagIn imaginary input needed for inverse transform
     * @return real and imaginary part of the FFT's result
     * @throws IllegalArgumentException if the number of samples is less than 4 or not a power of 2
     */
    public static native float[][] realFFT(final long pointer, final boolean forward, final int numberOfSamples, final float[] realIn, final float[] imagIn) throws IllegalArgumentException;


    private static native long init(final int numberOfSamples);

    private static native void destroy(final long pointer);

    private static float[] createFrequencies(final int numberOfSamples) {
        final float[] frequencies = new float[numberOfSamples];
        for (int index=0; index< numberOfSamples; index++) {
            if (index <= numberOfSamples / 2) {
                frequencies[index] = index / (float) numberOfSamples;
            } else {
                frequencies[index] = -((numberOfSamples - index) / (float) numberOfSamples);
            }
        }
        return frequencies;
    }

    @Override
    protected void finalize() throws Throwable {
        if (pointer != 0) {
            destroy(pointer);
        }
        super.finalize();
    }
}
