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
 * Test FFT.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestFFT {

    @BeforeAll
    public static void removeOldNativeLibs() throws IOException {
        final Path path = Paths.get(System.getProperty("java.io.tmpdir"));
        try (final Stream<Path> walk = Files.walk(path, 1)) {
            final Predicate<Path> pred = p -> p.getFileName().toString().endsWith(".dylib")
                || p.getFileName().toString().endsWith(".so")
                || p.getFileName().toString().endsWith(".dll");
            final List<Path> dylibs = walk.filter(pred).collect(Collectors.toList());
            for (final Path p : dylibs)
                Files.deleteIfExists(p);
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
        final FFT fft = new FFT(8);
        assertEquals(8, fft.getNumberOfSamples());
    }

    @Test
    public void testNegativeNumberOfSamples() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new FFT(-1));
    }

    @Test
    public void testGetFrequencyForBin() {
        final FFT fft = new FFT(8);
        assertEquals(0f, fft.getFrequencyForBin(0));
        assertEquals(-0.125f, fft.getFrequencyForBin(7));
    }

    @Test
    public void testGetFrequencyForNegativeBin() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new FFT(8).getFrequencyForBin(-1));
    }

    @Test
    public void testGetFrequencyForTooLargeBin() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new FFT(8).getFrequencyForBin(8));
    }

    @Test
    public void testGetFrequencies() {
        final FFT fft = new FFT(8);
        assertArrayEquals(new float[]{0.0f, 0.125f, 0.25f, 0.375f, 0.5f, -0.375f, -0.25f, -0.125f}, fft.getFrequencies());
    }

    @Test
    public void testToString() {
        final FFT fft = new FFT(8);
        assertEquals("FFT{N=8}", fft.toString());
    }
}
