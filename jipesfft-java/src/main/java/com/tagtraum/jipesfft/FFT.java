/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipesfft;

import java.lang.ref.Cleaner;

/**
 * Native implementation of FFT for a specific number of samples.
 * Uses {@link PureJavaFFT} as a fallback for some cases.
 * The implementation may be re-used for efficiency, but is not necessarily thread-safe.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class FFT extends AbstractFFT {

    static {
        NativeLibraryLoader.loadLibrary();
    }

    private static final Cleaner cleaner = Cleaner.create();
    private final Cleaner.Cleanable cleanable;

    private PureJavaFFT javaFFT;
    private final long pointer;

    /**
     * Constructor for a given number of samples.
     *
     * @param numberOfSamples number of samples you intend to transform, must be a power of two
     */
    public FFT(final int numberOfSamples) {
        super(numberOfSamples);
        if (usePureJavaFFT()) {
            this.pointer = 0;
            this.cleanable = null;
        } else {
            this.pointer = init(numberOfSamples);
            this.cleanable = cleaner.register(this, new Destroyer(this.pointer));
        }
    }

    private boolean usePureJavaFFT() {
        // TODO: check power of 2?
        final int numberOfSamples = getNumberOfSamples();
        return numberOfSamples < 4 || numberOfSamples > 32768;
    }

    private PureJavaFFT getPureJavaFFT() {
        if (javaFFT == null) {
            // instantiate lazily to avoid initialization overhead
            javaFFT = new PureJavaFFT(getNumberOfSamples());
        }
        return javaFFT;
    }

    @Override
    public float[][] inverseTransform(final float[] real, final float[] imaginary) throws UnsupportedOperationException {
        if (usePureJavaFFT()) {
            return getPureJavaFFT().inverseTransform(real, imaginary);
        }
        return FFT.realFFT(pointer, false, real.length, real, imaginary);
    }

    @Override
    public float[][] transform(final float[] real) throws UnsupportedOperationException {
        if (usePureJavaFFT()) {
            return getPureJavaFFT().transform(real);
        }
        final float[][] result = new float[3][];
        final float[][] nativeResult = FFT.realFFT(pointer, true, real.length, real, null);
        result[0] = nativeResult[0];
        result[1] = nativeResult[1];
        result[2] = getFrequencies();
        return result;
    }

    @Override
    public float[][] transform(final float[] real, final float[] imaginary) throws UnsupportedOperationException {
        // TODO: native version!
        return getPureJavaFFT().transform(real, imaginary);
    }

    /**
     * Perform real FFT.
     *
     * @param pointer pointer to a native FFT object
     * @param forward indicates whether this is a forward or inverse DFT
     * @param numberOfSamples number of samples
     * @param realIn input array of floats
     * @param imagIn imaginary input needed for inverse transform
     * @return real and imaginary part of the FFT's result
     * @throws IllegalArgumentException if the number of samples is less than 4 or not a power of 2
     */
    private static native float[][] realFFT(final long pointer, final boolean forward, final int numberOfSamples, final float[] realIn, final float[] imagIn) throws IllegalArgumentException;

    private static native long init(final int numberOfSamples);

    private static native void destroy(final long pointer);

    @Override
    public void close() {
        if (cleanable != null) {
            this.cleanable.clean();
        }
    }

    /**
     * Hook that ensures native resources are eventually freed.
     * This is the Java 9 equivalent of the deprecated {@link #finalize()}.
     */
    private static class Destroyer implements Runnable {
        
        private final long pointer;

        public Destroyer(final long pointer) {
            this.pointer = pointer;
        }

        @Override
        public void run() {
            if (pointer != 0) {
                destroy(pointer);
            }
        }
    }
}
