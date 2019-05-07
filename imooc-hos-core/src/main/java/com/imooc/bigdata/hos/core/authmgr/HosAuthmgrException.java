package com.imooc.bigdata.hos.core.authmgr;

import com.imooc.bigdata.hos.core.HosException;

/**
 * Created by jixin on 18-3-8.
 */
public class HosAuthmgrException extends HosException {

  private int code;
  private String message;

  public HosAuthmgrException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.message = message;
  }

  public HosAuthmgrException(int code, String message) {
    super(message, null);
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public int errorCode() {
    return this.code;
  }
}