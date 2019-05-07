package com.imooc.bigdata.hos.common;

public class ListObjectRequest {

  private String bucket;
  private String startKey;
  private String endKey;
  private String prefix;
  private int maxKeyNumber;
  private String listId;

  public String getListId() {
    return listId;
  }

  public void setListId(String listId) {
    this.listId = listId;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getStartKey() {
    return startKey;
  }

  public void setStartKey(String startKey) {
    this.startKey = startKey;
  }

  public String getEndKey() {
    return endKey;
  }

  public void setEndKey(String endKey) {
    this.endKey = endKey;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public int getMaxKeyNumber() {
    return maxKeyNumber;
  }

  public void setMaxKeyNumber(int maxKeyNumber) {
    this.maxKeyNumber = maxKeyNumber;
  }
}
