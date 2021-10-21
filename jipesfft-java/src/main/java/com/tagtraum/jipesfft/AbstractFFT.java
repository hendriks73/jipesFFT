/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipesfft;

/**
 * Abstract superclass for FFTs.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public abstract class AbstractFFT {

    /** Index to be used on the result of {@link #transform(float[])} and the like. */
    public static final int REAL = 0;
    /** Index to be used on the result of {@link #transform(float[])} and the like. */
    public static final int IMAGINARY = 1;
    /** Index to be used on the result of {@link #transform(float[])} and the like. */
    public static final int FREQUENCY = 2;
    private final int numberOfSamples;
    private final float[] frequencies;

    /**
     * Constructor for a given number of samples.
     *
     * @param numberOfSamples number of samples you intend to transform
     * @throws IllegalArgumentException if the number of samples is negative or not a power of 2
     */
    public AbstractFFT(final int numberOfSamples) {
        if (numberOfSamples < 0) {
            throw new IllegalArgumentException("Number of samples must not be negative: " + numberOfSamples);
        }
        if (!isPowerOfTwo(numberOfSamples)) {
            throw new IllegalArgumentException("Number of samples must be a power of 2: " + numberOfSamples);
        }
        this.numberOfSamples = numberOfSamples;
        this.frequencies = createFrequencies(numberOfSamples);
    }

    /**
     * Number of samples.
     *
     * @return number of samples
     */
    public int getNumberOfSamples() {
        return numberOfSamples;
    }

    /**
     * Is power of two?
     *
     * @param number number
     * @return true or false
     */
    private static boolean isPowerOfTwo(final int number) {
        return (number & (number - 1)) == 0;
    }

    /**
     * Create frequencies array for a given number of samples
     *
     * @param numberOfSamples number of samples
     * @return frequencies
     */
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

    /**
     * Array describing the frequency for the different FFT bins.
     * Note that these still need to be multiplied with your sampling
     * frequency to get actual Hz values. Also note, that values
     * beyond {@code N/2} (<a href="https://en.wikipedia.org/wiki/Nyquist_frequency">Nyquist
     * frequency</a>) are typically not useful.
     * <p>
     * The returned array is a clone, you may do with it whatever you want.
     *
     * @return array with frequencies for the FFT (bins == index)
     */
    public float[] getFrequencies() {
        return this.frequencies.clone();
    }

    /**
     * Frequency for a given fourier transform bin.
     * Note that the returned frequencies need to be multiplied with your sampling
     * frequency to get actual Hz values. Also note, that values
     * beyond {@code N/2} (<a href="https://en.wikipedia.org/wiki/Nyquist_frequency">Nyquist
     * frequency</a>) are typically not useful.
     *
     * @param bin bin
     * @return frequency
     * @throws IllegalArgumentException if the given bin number is invalid for this FFT instance
     */
    public float getFrequencyForBin(final int bin) {
        if (bin < 0) throw new IllegalArgumentException("Frequency bin must not be negative: " + bin);
        if (bin >= this.frequencies.length) throw new IllegalArgumentException("Frequency bin must not be greater than "
            + (this.frequencies.length-1) + ": " + bin);
        return this.frequencies[bin];
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "N=" + getNumberOfSamples() +
            '}';
    }

    /**
     * Perform inverse transform.
     * For very large number of samples (&gt;32768) the Java version is used.
     *
     * @param real real part
     * @param imaginary imaginary part
     * @return two-dimensional array for the real and the imaginary parts
     */
    public abstract float[][] inverseTransform(float[] real, float[] imaginary) throws UnsupportedOperationException;

    /**
     * Transform for real numbers.
     * For very large number of samples (&gt;32768) the Java version is used.
     *
     * @param real samples
     * @return three-dimensional array, consisting of the real part, the imaginary, and the frequencies
     */
    public abstract float[][] transform(float[] real) throws UnsupportedOperationException;

    /**
     * Transform for complex numbers.
     * Note that the current implementation simply delegates to the pure Java version.
     *
     * @param real real part
     * @param imaginary imaginary part
     * @return three-dimensional array, consisting of the real part, the imaginary, and the frequencies
     */
    public abstract float[][] transform(float[] real, float[] imaginary) throws UnsupportedOperationException;
}
