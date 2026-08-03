package com.github.marschall.fixedlenghtfile.batch;

import java.io.IOException;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.ItemStreamSupport;
import org.springframework.batch.infrastructure.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.github.marschall.fixedlenghtfile.ReadingLine;

public class FixedLengthItemReader extends ItemStreamSupport implements ResourceAwareItemReaderItemStream<ReadingLine> {

  private static final String READ_COUNT = "read.count";

  private int currentItemCount = 0;

  private Resource resource;

  @Override
  public @Nullable ReadingLine read() throws Exception {
    // TODO Auto-generated method stub
    currentItemCount++;
    return null;
  }

  @Override
  public void setResource(Resource resource) {
    this.resource = resource;
  }

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    Path path = this.getPath();

    int initialItemCount = getInitialItemCount(executionContext);

    if (initialItemCount > 0) {
      try {
        jumpToItem(initialItemCount);
      } catch (Exception e) {
        throw new ItemStreamException("Could not move to stored position on restart", e);
      }
    }

    this.currentItemCount = initialItemCount;
  }

  private void jumpToItem(int initialItemCount) {
    // TODO Auto-generated method stub
    
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

  private Path getPath() {
    Assert.notNull(this.resource, "Input resource must be set");
    if (!this.resource.exists()) {
      throw new IllegalStateException("Input resource must exist: " + resource);
    }
    if (!this.resource.isReadable()) {
      throw new IllegalStateException("Input resource must be readable: " + resource);
    }
    if (!this.resource.isFile()) {
      throw new IllegalStateException("Input resource must be a file: " + resource);
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
  }
  
  @Override
  public void close() throws ItemStreamException {
    // TODO Auto-generated method stub
    ResourceAwareItemReaderItemStream.super.close();
  }


}
