[![LGPL 2.1](https://img.shields.io/badge/License-LGPL_2.1-blue.svg)](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.tagtraum/jipesfft/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.tagtraum/jipesfft)
[![Build and Test](https://github.com/hendriks73/jipesFFT/workflows/Build%20and%20Test/badge.svg?branch=dev)](https://github.com/hendriks73/jipesFFT/actions?query=branch%3Adev++)
[![CodeCov](https://codecov.io/gh/hendriks73/jipesFFT/branch/dev/graph/badge.svg?token=DFf7RcXFkf)](https://codecov.io/gh/hendriks73/jipesFFT/tree/dev)


# jipesFFT

Native FFT (Fast Fourier Transformation) implementations for Java
(macOS, Windows, Linux).

The macOS version uses Apple's [Accelerate
framework](https://developer.apple.com/documentation/accelerate).
Windows and Linux versions use more ordinary implementations.


## Installation

jipesFFT is released via [Maven](https://maven.apache.org).
You can install it via the following dependency:

```xml
<dependencies>
    <dependency>
        <groupId>com.tagtraum</groupId>
        <artifactId>jipesfft-complete</artifactId>
    </dependency>
</dependencies>
```

## Usage

Here's a simple example that demonstrates how the `com.tagtraum.jipesfft.FFT`
class can be used:

```java
import com.tagtraum.jipesfft.FFT;

class Demo {
    public static void main(final String[] args) {
        // Create a new FFT instance suitable
        // for a window length of 8 samples.
        // Do this in a "try" statement to ensure native resources
        // are freed in a timely fashion.
        try (final FFT fft = new FFT(8)) {
            final float[] result = fft.transform(new float[]{0, 0, 0, 1, 0, 0, 0, 1});
            // real part of the complex result
            float[] real = result[FFT.REAL];
            // imaginary part of the complex result
            float[] imaginary = result[FFT.IMAGINARY];
            // frequencies for the result - must still be multiplied with sampling
            // frequency. Second half of frequency array is not necessarily useful.
            float[] frequencies = result[FFT.FREQUENCY];

            // go on and do something with it...
        }
    }
}
```

## Java Module

jipesFFT is shipped as a Java module
(see [JPMS](https://en.wikipedia.org/wiki/Java_Platform_Module_System))
with the name `tagtraum.jipesfft`.


## API

You can find the complete [API here](https://hendriks73.github.io/jipesFFT/).
                       

## Additional Resources

- [Discrete Fourier Transform (Wikipedia)](https://en.wikipedia.org/wiki/Discrete_Fourier_transform)
- [Intro to Discrete Fourier Transform by M. Müller and F. Zalkow](https://www.audiolabs-erlangen.de/resources/MIR/FMP/C2/C2_DFT-FFT.html)
- [Fast Fourier Transform (algorithm)](https://en.wikipedia.org/wiki/Fast_Fourier_transform)
- [Nyquist Frequency](https://en.wikipedia.org/wiki/Nyquist_frequency)
- [Fundamentals of Music Processing by Meinard Müller](https://www.springer.com/gp/book/9783030698072) 