package com.github.marschall.fixedlengthfile.jmh;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import com.github.marschall.fixedlengthfile.ByteArrayWritingLine;
import com.github.marschall.fixedlengthfile.CharArrayWritingLine;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.SignedFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlengthfile.WritingLine;
import com.github.marschall.fixedlengthfile.configuration.parser.InterfaceDefinition267;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class WritingLineBenchmarks {

  private WritingLine charArrayWritingLine;
  private WritingLine byteArrayWritingLine;
  private FixedLengthRecordDefinition recordDefinition;

  @Setup
  public void setUp() {
    this.recordDefinition = (FixedLengthRecordDefinition) InterfaceDefinition267.HD.definition();
    this.charArrayWritingLine = new CharArrayWritingLine(this.recordDefinition.getMaximumLength());
    this.byteArrayWritingLine = new ByteArrayWritingLine(this.recordDefinition.getMaximumLength());
  }
  
  @Benchmark
  public WritingLine charArrayWritingLine() {
    List<? extends OffsetFieldDefinition> fields = this.recordDefinition.getFields();
    this.charArrayWritingLine.writeString(InterfaceDefinition267.HD.H01, "HD");
    for (OffsetFieldDefinition field : fields.subList(1, fields.size())) {
      switch (field) {
        case StringFieldDefinition stringField -> this.charArrayWritingLine.writeNoValue(stringField);
        case UnsignedFieldDefinition unsignedField -> this.charArrayWritingLine.writeNoValue(unsignedField);
        case SignedFieldDefinition _ -> throw new UnsupportedOperationException("Unsigned not yet supported");
      };
    }
    return this.charArrayWritingLine;
  }
  
  @Benchmark
  public WritingLine byteArrayWritingLine() {
    List<? extends OffsetFieldDefinition> fields = this.recordDefinition.getFields();
    this.byteArrayWritingLine.writeString(InterfaceDefinition267.HD.H01, "HD");
    for (OffsetFieldDefinition field : fields.subList(1, fields.size())) {
      switch (field) {
      case StringFieldDefinition stringField -> this.byteArrayWritingLine.writeNoValue(stringField);
      case UnsignedFieldDefinition unsignedField -> this.byteArrayWritingLine.writeNoValue(unsignedField);
      case SignedFieldDefinition _ -> throw new UnsupportedOperationException("Unsigned not yet supported");
      };
    }
    return this.byteArrayWritingLine;
  }

}
