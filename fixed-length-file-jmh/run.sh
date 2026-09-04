#!/bin/sh

# $JAVA_HOME/bin/java -jar target/spring-event-benchmarks-1.0.0-SNAPSHOT.jar -h
# $JAVA_HOME/bin/java -jar target/spring-event-benchmarks-1.0.0-SNAPSHOT.jar -lprof

# export DYLD_LIBRARY_PATH=${HOME}/Documents/dev/async-profiler/async-profiler-4.3-macos/lib

# -prof jfr \
# -prof async:libPath=${HOME}/Documents/dev/async-profiler/async-profiler-4.3-macos/lib/libasyncProfiler.dylib\;output=flamegraph \

$JAVA_HOME/bin/java \
  -Xmx128m \
  -XX:+UseSerialGC \
  -jar target/fixed-length-file-jmh-1.0.0-SNAPSHOT.jar \
     "com\\.github\\.marschall\\.fixedlengthfile\\.jmh\\.ByteCopierBenchmarks" \
     -jvmArgs "-XX:+UseParallelGC -Xmx8g  --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow" \
     -foe true \
     -rf text