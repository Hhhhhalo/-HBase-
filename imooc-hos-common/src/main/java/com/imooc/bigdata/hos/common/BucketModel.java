package com.imooc.bigdata.hos.common;

import java.util.Date;

import com.imooc.bigdata.hos.core.usermgr.CoreUtil;

/**
 * Created by jixin on 18-3-8.
 */
public class BucketModel {

  private String bucketId;
  private String bucketName;
  private String creator;
  private String detail;
  private Date createTime;

  public BucketModel(String bucketName, String creator, String detail) {
    this.bucketId = CoreUtil.getUUID();
    this.bucketName = bucketName;
    this.createTime = new Date();
    this.creator = creator;
    this.detail = detail;
  }

  public BucketModel() {

  }

  public String getBucketId() {
    return bucketId;
  }

  public void setBucketId(String bucketId) {
    this.bucketId = bucketId;
  }

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }
}
