/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipesfft;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * TestNativeFFT.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestFFT {

    @BeforeAll
    public static void removeOldNativeLibs() throws IOException {
        final Path path = Paths.get(System.getProperty("java.io.tmpdir"));
        try (final Stream<Path> walk = Files.walk(path, 1)) {
            final List<Path> dylibs = walk.filter(p -> p.getFileName().toString().endsWith(".dylib")).collect(Collectors.toList());
            for (final Path p : dylibs)
                Files.deleteIfExists(p);
        }
    }

    @Test
    public void testSimpleData() {
        final PureJavaFFT javaFFT = new PureJavaFFT(8);
        final FFT nativeFFT = new FFT(8);
        final float[][] nativeRes = nativeFFT.transform(new float[]{0, 0, 0, 1, 0, 0, 0, 1});
        final float[][] javaRes = javaFFT.transform(new float[]{0, 0, 0, 1, 0, 0, 0, 1});
        assertSame(3, nativeRes.length);
        assertArrayEquals(javaRes[0], nativeRes[0], 0.01f);
        assertArrayEquals(javaRes[1], nativeRes[1], 0.01f);
        assertArrayEquals(javaRes[2], nativeRes[2], 0.01f);
    }
}
