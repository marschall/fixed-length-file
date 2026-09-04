package com.github.marschall.fixedlengthfile.batch;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.ItemStreamSupport;
import org.springframework.batch.infrastructure.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.github.marschall.fixedlengthfile.FileDefinition;
import com.github.marschall.fixedlengthfile.FixedLengthFileParser;
import com.github.marschall.fixedlengthfile.ReadingLine;
import com.github.marschall.fixedlengthfile.StatefulFixedLengthFile;

public class FixedLengthItemReader extends ItemStreamSupport implements ResourceAwareItemReaderItemStream<ReadingLine> {

  private static final String READ_COUNT = "read.count";
  private static final String READ_POSITION = "read.position";

  private int currentItemCount = 0;

  private Resource resource;

  private Arena arena;

  private StatefulFixedLengthFile file;

  @Override
  public @Nullable ReadingLine read() throws Exception {
    ReadingLine line = this.file.nextLineOrNull();
    if (line != null) {
      this.currentItemCount++;
    }
    return line;
  }

  @Override
  public void setResource(Resource resource) {
    this.resource = resource;
  }

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    Path path = this.getPath();
    this.arena = Arena.ofShared();
    // FIXME
    FileDefinition fileDefinition = null;
    try {
      this.file = FixedLengthFileParser.parseFile(fileDefinition, path, this.arena);
    } catch (IOException e) {
      throw new ItemStreamException("could not open file: " + path, e);
    }
    long position = this.getInitialPosition(executionContext);
    this.file.setCurrentPosition(position);

    int initialItemCount = getInitialItemCount(executionContext);
    this.currentItemCount = initialItemCount;
  }

  private int getInitialItemCount(ExecutionContext executionContext) {
    int itemCount;
    if (executionContext.containsKey(getExecutionContextKey(READ_COUNT))) {
      itemCount = executionContext.getInt(getExecutionContextKey(READ_COUNT));
    } else if (this.currentItemCount > 0) {
      itemCount = this.currentItemCount;
    } else {
      itemCount = 0;
    }
    if (itemCount < 0) {
      throw new IllegalStateException("itemCount must be positive: " + itemCount);
    }
    return itemCount;
  }
  
  private long getInitialPosition(ExecutionContext executionContext) {
    long position;
    if (executionContext.containsKey(getExecutionContextKey(READ_POSITION))) {
      position = executionContext.getLong(getExecutionContextKey(READ_POSITION));
    } else {
      position = 0L;
    }
    if (position < 0) {
      throw new IllegalStateException("itemCount must be positive: " + position);
    }
    return position;
  }

  private Path getPath() {
    Assert.notNull(this.resource, "Input resource must be set");
    if (!this.resource.exists()) {
      throw new IllegalStateException("Input resource must exist: " + this.resource);
    }
    if (!this.resource.isReadable()) {
      throw new IllegalStateException("Input resource must be readable: " + this.resource);
    }
    if (!this.resource.isFile()) {
      throw new IllegalStateException("Input resource must be a file: " + this.resource);
    }
    try {
      return this.resource.getFilePath();
    } catch (IOException e) {
      throw new ItemStreamException("Failed to initialize the reader", e);
    }
  }
  
  @Override
  public void update(ExecutionContext executionContext) {
    executionContext.putInt(getExecutionContextKey(READ_COUNT), this.currentItemCount);
    executionContext.putLong(getExecutionContextKey(READ_POSITION), this.file.getCurrentPosition());
  }
  
  @Override
  public void close() throws ItemStreamException {
    try {
      if (this.file != null) {
        try {
          this.file.close();
        } catch (IOException e) {
          throw new ItemStreamException("Could not clos file", e);
        }
      }
    } finally {
      this.arena.close();
    }
  }

}
