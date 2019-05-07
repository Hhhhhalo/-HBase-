package com.imooc.bigdata.hos.core;

/**
 * Created by jixin on 18-3-8.
 * base exception,all exception should extend it.
 */
public abstract class HosException extends RuntimeException {

  protected String errorMessage;

  public HosException(String message, Throwable cause) {
    super(cause);
    this.errorMessage = message;
  }

  public abstract int errorCode();

  public String errorMessage() {
    return this.errorMessage;
  }
}
