//===-------- Classpath.h - Configuration for classpath -------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

// Historically has been included here, keep it for now
#include <jni.h>

#ifndef USE_OPENJDK

// GNU Classpath values
#define GNUClasspathLibs "@classpathlibs@"
#define GNUClasspathGlibj "@classpathglibj@"
#define GNUClasspathVersion "@classpathversion@"

#define ClasslibBootEnv GNUClasspathGlibj
#define ClasslibLibEnv GNUClasspathLibs

#else

// OpenJDK values
#define OpenJDKJRE "@openjdkjre@"
#define OpenJDKArch "@openjdkarchdir@"

// OpenJDK Bootstrap classpath
#define OpenJDKBootPath \
      OpenJDKJRE "/lib/rt.jar" \
  ":" OpenJDKJRE "/lib/resources.jar" \
  ":" OpenJDKJRE "/lib/jsse.jar" \
  ":" OpenJDKJRE "/lib/jce.jar" \
  ":" OpenJDKJRE "/lib/charsets.jar"

// Location of OpenJDK's libjava.so
#define OpenJDKLibJava OpenJDKArch "/libjava.so"

// Search path for native library files
// TODO: Use LD_LIBRARY_PATH to second part of this?
#define OpenJDKLibPaths \
      OpenJDKArch \
  ":" OpenJDKArch "/client" \
  ":" OpenJDKArch "/server" \
  ":" "/lib" \
  ":" "/lib64" \
  ":" "/usr/lib" \
  ":" "/usr/lib64"

#define ClasslibBootEnv OpenJDKBootPath
#define ClasslibLibEnv OpenJDKLibPaths

#endif // USE_OPENJDK
