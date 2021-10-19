/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipesfft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * TestNativeFFT.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestNativeFFT {

    @Test
    public void testSimpleData() {
        final JavaFFT javaFFT = new JavaFFT(8);
        final NativeFFT nativeFFT = new NativeFFT(8);
        final float[][] nativeRes = nativeFFT.transform(new float[]{0, 0, 0, 1, 0, 0, 0, 1});
        final float[][] javaRes = javaFFT.transform(new float[]{0, 0, 0, 1, 0, 0, 0, 1});
        assertSame(3, nativeRes.length);
        assertArrayEquals(javaRes[0], nativeRes[0], 0.01f);
        assertArrayEquals(javaRes[1], nativeRes[1], 0.01f);
        assertArrayEquals(javaRes[2], nativeRes[2], 0.01f);
    }
}
