package com.github.marschall.fixedlenghtfile.configuration.parser;

public record Field(String id, String name, int offset, int length, DataType dataType, String description) {

}
