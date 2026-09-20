package com.github.marschall.fixedlengthfile;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

final class MappedList<E, I> extends AbstractList<E> {
  // REVIEW superclass has modcount

  private final List<I> delegate;
  private final Function<I, E> mappingFunction;

  MappedList(List<I> delegate, Function<I, E> mappingFunction) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    this.mappingFunction = Objects.requireNonNull(mappingFunction, "mappingFunction");
  }

  @Override
  public int size() {
    return this.delegate.size();
  }
  @Override
  public E get(int index) {
    return this.mappingFunction.apply(this.delegate.get(index));
  }

}
