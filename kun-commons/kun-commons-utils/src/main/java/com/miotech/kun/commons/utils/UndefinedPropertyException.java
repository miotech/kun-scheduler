package com.miotech.kun.commons.utils;

/**
 * Indicates that a required property is missing from the Props
 */
public class UndefinedPropertyException extends RuntimeException {

  private static final long serialVersionUID = 1;

  public UndefinedPropertyException(final String message) {
    super(message);
  }

}
