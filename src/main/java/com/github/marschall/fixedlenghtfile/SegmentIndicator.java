package com.github.marschall.fixedlenghtfile;

public enum SegmentIndicator {

  // TODO PRESENT_WITH_VALUES?
  PRESENT {
    @Override
    char getValue() {
      return PRESENT_VALUE;
    }
  },
  ABSENT {
    @Override
    char getValue() {
      return ABSENT_VALUE;
    }
  },
  SPACES {
    @Override
    char getValue() {
      return SPACES_VALUE;
    }
  };

  static final char PRESENT_VALUE = 'Y';
  static final char ABSENT_VALUE = 'N';
  static final char SPACES_VALUE = 'S';

  abstract char getValue();

}
