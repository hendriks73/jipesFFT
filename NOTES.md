- 0.9.13
  - Explicit `maven-resources-plugin` version. 


- 0.9.12
  - Force signatures for macOS to be timestamped
  - Renamed ill-named linux binaries to `linux` (was `unix`).
  - Added support for arm64/aarch64 Linux.


- 0.9.11
  - Added signature for native macOS libs 
  - Updated Maven plugins


- 0.9.10
  - Fixed typo in readme  

 
- 0.9.9
  - Fixed missing native libs in *complete* package
  - Moved to actions/setup-java@v2 in GitHub build


- 0.9.5 - 0.9.8
  - Attempt to fix deploy error


- 0.9.4
  - Improved test coverage

 
- 0.9.3
  - Added pure Java fallback mechanism, in case native library loading fails 


- 0.9.2 
  - Renamed some classes
  - Improved javadocs
  - Introduced module build
  - Replaced `finalize()` with Cleaner logic


- 0.9.1
  - Attempt to properly include native libs in complete     


- 0.9.0
  - Initial release 