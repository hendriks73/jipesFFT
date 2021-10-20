[![LGPL 2.1](https://img.shields.io/badge/License-LGPL_2.1-blue.svg)](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.tagtraum/jipesfft/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.tagtraum/jipesfft)
[![Build and Test](https://github.com/hendriks73/jipesFFT/workflows/Build%20and%20Test/badge.svg)](https://github.com/hendriks73/jipesFFT/actions)
[![CodeCov](https://codecov.io/gh/hendriks73/jipesFFT/branch/main/graph/badge.svg?token=H98FM0SKQL)](https://codecov.io/gh/hendriks73/jipesFFT/branch/main)


# jipesFFT

Native FFT implementations for Java (macOS, Windows, Linux).

The macOS version uses Apple's [Accelerate
framework](https://developer.apple.com/documentation/accelerate).
Windows and Linux versions use a more ordinary implementation.

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


## API

You can find the complete [API here](https://hendriks73.github.io/jipesFFT/).
