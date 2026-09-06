package com.github.marschall.fixedlengthfile;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

import com.github.marschall.fixedlengthfile.FixedLengthFile.LineInformation;
import com.github.marschall.fixedlengthfile.StatefulFixedLengthFile.LineLocator;

final class LineLocatorListBuilder {

  private boolean firstLine;
  private RecordDefinition recordDefinition;
  private boolean uniform;
  private List<LineLocator> locators;
  private int initialRecordLength;
  private SegmentOffsets initialSegmentOffsets;
  private long initialStart;
  private long previousEnd;
  private int recordCount;
  private int newLineSize;

  LineLocatorListBuilder() {
    this.firstLine = true;
    this.uniform = true;
    this.locators = new ArrayList<>();
    this.recordCount = 0;
  }

  void add(long lineStart, LineInformation lineInformation) {
    if (this.firstLine) {
      this.recordDefinition = lineInformation.recordDefinition();
      this.initialStart = lineStart;
      this.initialSegmentOffsets = lineInformation.segmentOffsets();
      this.initialRecordLength = lineInformation.recordLength();
      this.firstLine = false;
    } else if (!this.uniform) {
      this.locators.add(new LineLocator(lineStart, lineInformation));
    } else {
      int currentNewLineSize = Math.toIntExact(lineStart - this.previousEnd);
      if (this.recordCount == 1) {
        this.newLineSize = currentNewLineSize;
      }
      boolean sameSegmentOffsets = this.initialSegmentOffsets.equals(lineInformation.segmentOffsets());
      boolean sameRecordLength = this.initialRecordLength == lineInformation.recordLength();
      boolean sameNewLineSize = currentNewLineSize == this.newLineSize;
      if (sameSegmentOffsets && sameRecordLength && sameNewLineSize) {
        // just increment record count
      } else {
        this.uniform = false;
        // de-optimize
        // reconstruct locators
        for (int i = 0; i < this.recordCount; i++) {
          long reconstructedStart = this.initialStart + ((long) (this.initialRecordLength + this.newLineSize) * i);
          this.locators.add(new LineLocator(reconstructedStart, new LineInformation(this.recordDefinition, this.initialRecordLength, this.initialSegmentOffsets)));
        }
        this.locators.add(new LineLocator(lineStart, lineInformation));
        this.recordCount += 1;
      }
    }
    this.recordCount += 1;
    this.previousEnd = lineStart + lineInformation.recordLength();
  }

  List<LineLocator> build() {
    if (this.uniform) {
      return new UniformList(this.initialStart, this.recordCount, this.initialRecordLength, this.initialSegmentOffsets, this.newLineSize, this.recordDefinition);
    } else {
      return this.locators;
    }
  }

  static final class UniformList extends AbstractList<LineLocator> implements RandomAccess {

    private final long baseOffset;
    private final int lineCount;
    private final int recordLength;
    private final SegmentOffsets segmentOffsets;
    private final int newLineSize;
    private final RecordDefinition recordDefinition;

    UniformList(long baseOffset, int lineCount, int recordLength, SegmentOffsets segmentOffsets, int newLineSize, RecordDefinition recordDefinition) {
      this.baseOffset = baseOffset;
      this.lineCount = lineCount;
      this.recordLength = recordLength;
      this.segmentOffsets = segmentOffsets;
      this.newLineSize = newLineSize;
      this.recordDefinition = recordDefinition;
    }

    @Override
    public int size() {
      return this.lineCount;
    }

    @Override
    public LineLocator get(int index) {
      Objects.checkIndex(index, this.lineCount);
      long lineStart = this.baseOffset + ((long) (this.recordLength + this.newLineSize) * index);
      return new LineLocator(lineStart, new LineInformation(this.recordDefinition, this.recordLength, this.segmentOffsets));
    }

  }

}
