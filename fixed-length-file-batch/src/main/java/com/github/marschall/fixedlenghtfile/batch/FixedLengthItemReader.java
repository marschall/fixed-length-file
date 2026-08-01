package com.github.marschall.fixedlenghtfile.batch;

import java.io.IOException;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;

import com.github.marschall.fixedlenghtfile.ReadingLine;

public class FixedLengthItemReader implements ResourceAwareItemReaderItemStream<ReadingLine> {

  private Resource resource;

  @Override
  public @Nullable ReadingLine read() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setResource(Resource resource) {
    this.resource = resource;
  }

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    if (!this.resource.exists()) {
      throw new IllegalStateException("Input resource must exist: " + resource);
    }
    if (!this.resource.isReadable()) {
      throw new IllegalStateException("Input resource must be readable: " + resource);
    }
    if (!this.resource.isFile()) {
      throw new IllegalStateException("Input resource must be a file: " + resource);
    }
    Path path;
    try {
      path = this.resource.getFilePath();
    } catch (IOException e) {
      throw new ItemStreamException("Failed to initialize the reader", e);
    }
  }


}
