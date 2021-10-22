/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipesfft;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tagtraum.jipesfft.AbstractFFT.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test PureJavaFFT.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestPureJavaFFT {

    @BeforeAll
    public static void removeOldNativeLibs() throws IOException {
        final Path path = Paths.get(System.getProperty("java.io.tmpdir"));
        try (final Stream<Path> walk = Files.walk(path, 1)) {
            final Predicate<Path> pred = p -> p.getFileName().toString().endsWith(".dylib")
                || p.getFileName().toString().endsWith(".so")
                || p.getFileName().toString().endsWith(".dll");
            final List<Path> dylibs = walk.filter(pred).collect(Collectors.toList());
            for (final Path p : dylibs)
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    @Test
    public void testTransformByComparison() {
        final PureJavaFFT javaFFT = new PureJavaFFT(8);
        final FFT nativeFFT = new FFT(8);
        
        final float[][] nativeRes = nativeFFT.transform(new float[]{0, 0, 0, 1, 0, 0, 0, 1});
        final float[][] javaRes = javaFFT.transform(new float[]{0, 0, 0, 1, 0, 0, 0, 1});

        assertEquals(3, nativeRes.length);
        assertArrayEquals(javaRes[REAL], nativeRes[REAL], 0.01f);
        assertArrayEquals(javaRes[IMAGINARY], nativeRes[IMAGINARY], 0.01f);
        assertArrayEquals(javaRes[FREQUENCY], nativeRes[FREQUENCY], 0.01f);

        final float[][] nativeInverseRes = nativeFFT.inverseTransform(nativeRes[REAL], nativeRes[IMAGINARY]);
        final float[][] javaInverseRes = javaFFT.inverseTransform(javaRes[REAL], javaRes[IMAGINARY]);

        assertEquals(2, nativeInverseRes.length);
        assertArrayEquals(javaInverseRes[REAL], nativeInverseRes[REAL], 0.01f);
        assertArrayEquals(javaInverseRes[IMAGINARY], nativeInverseRes[IMAGINARY], 0.01f);
    }

    @Test
    public void testNumberOfSamples() {
        final PureJavaFFT fft = new PureJavaFFT(8);
        assertEquals(8, fft.getNumberOfSamples());
        assertEquals(fft.hashCode(), fft.getNumberOfSamples());
    }

    @Test
    public void testNegativeNumberOfSamples() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PureJavaFFT(-1));
    }

    @Test
    public void testGetFrequencyForBin() {
        final PureJavaFFT fft = new PureJavaFFT(8);
        assertEquals(0f, fft.getFrequencyForBin(0));
        assertEquals(-0.125f, fft.getFrequencyForBin(7));
    }

    @Test
    public void testGetFrequencyForNegativeBin() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PureJavaFFT(8).getFrequencyForBin(-1));
    }

    @Test
    public void testGetFrequencyForTooLargeBin() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PureJavaFFT(8).getFrequencyForBin(8));
    }

    @Test
    public void testGetFrequencies() {
        final PureJavaFFT fft = new PureJavaFFT(8);
        assertArrayEquals(new float[]{0.0f, 0.125f, 0.25f, 0.375f, 0.5f, -0.375f, -0.25f, -0.125f}, fft.getFrequencies());
    }

    @Test
    public void testToString() {
        final PureJavaFFT fft = new PureJavaFFT(8);
        assertEquals("PureJavaFFT{N=8}", fft.toString());
    }

    @Test
    public void testTwoSampleFFT() {
        final PureJavaFFT fft = new PureJavaFFT(2);
        final float[][] result = fft.transform(new float[]{0, 1});
        assertArrayEquals(new float[]{1f, -1f}, result[REAL], 0.01f);
        assertArrayEquals(new float[]{0f, 0f}, result[IMAGINARY], 0.01f);
        assertArrayEquals(new float[]{0f, 0.5f}, result[FREQUENCY], 0.01f);
        assertEquals("PureJavaFFT{N=2}", fft.toString());
    }

    @Test
    public void testNumberOfSamplesPowerOfTwo() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PureJavaFFT(5));
    }

    @Test
    public void testManySamplesFFT() {
        try (final PureJavaFFT fft = new PureJavaFFT(65536)) {
            final float[] emptyArray = new float[65536];
            final float[][] result = fft.transform(emptyArray);
            assertArrayEquals(emptyArray, result[REAL], 0.01f);
            assertArrayEquals(emptyArray, result[IMAGINARY], 0.01f);
            // transform again!
            fft.transform(emptyArray);
        }
    }
}
