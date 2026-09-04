package com.github.marschall.fixedlengthfile.configuration.parser;

public record Field(String id, String name, int offset, int length, DataType dataType, String description) {

}
