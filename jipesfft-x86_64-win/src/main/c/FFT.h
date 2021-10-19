#ifndef M_PI
#define	M_PI		3.14159265358979323846  /* pi */
#endif

/**
 * Initializes the FFT code.
 */
void init();

/**
 * Computes a FFT of complex input and returns complex output.
 * Currently this is the only function here that supports the
 * inverse transform as well.
 */
void fft(int numSamples, bool inverseTransform, float *realIn, float *imagIn, float *realOut, float *imagOut);

