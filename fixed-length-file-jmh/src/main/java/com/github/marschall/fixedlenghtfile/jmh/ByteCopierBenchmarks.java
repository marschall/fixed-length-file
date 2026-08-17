package com.github.marschall.fixedlenghtfile.jmh;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class ByteCopierBenchmarks {

  private Arena arena;
  private MemorySegment segment;
  private char[] dst;

  @Setup
  public void setUp() {
    this.arena = Arena.ofConfined();
    int size = 4525;
    this.segment = this.arena.allocate(size);
    this.dst = new char[size];
  }

  @TearDown
  public void tearDown() {
    this.arena.close();
  }

  @Benchmark
  public char[] copyScalar() {
    ByteCopier.copyScalar(this.segment, this.dst, 0, this.dst.length);
    return this.dst;
  }

  @Benchmark
  public char[] copySwar() {
    ByteCopier.copySwar(this.segment, this.dst, 0, this.dst.length);
    return this.dst;
  }

}
