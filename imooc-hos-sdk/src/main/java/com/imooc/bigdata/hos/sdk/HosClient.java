package com.imooc.bigdata.hos.sdk;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.imooc.bigdata.hos.common.BucketModel;
import com.imooc.bigdata.hos.common.HosObject;
import com.imooc.bigdata.hos.common.HosObjectSummary;
import com.imooc.bigdata.hos.common.ListObjectRequest;
import com.imooc.bigdata.hos.common.ObjectListResult;
import com.imooc.bigdata.hos.common.PutRequest;

public interface HosClient {

  public void createBucket(String bucketName) throws IOException;

  public void deleteBucket(String bucketName) throws IOException;

  public List<BucketModel> listBucket() throws IOException;

  public void putObject(PutRequest putRequest) throws IOException;

  public void putObject(String bucket, String key) throws IOException;

  public void putObject(String bucket, String key, byte[] content, String mediaType)
      throws IOException;

  public HosObjectSummary getObjectSummary(String bucket, String key) throws IOException;

  public void deleteObject(String bucket, String key) throws IOException;





  public void putObject(String bucket, String key, byte[] content, String mediaType,
      String contentEncoding)
      throws IOException;

  public void putObject(String bucket, String key, File content, String mediaType)
      throws IOException;

  public void putObject(String bucket, String key, File content, String mediaType,
      String contentEncoding)
      throws IOException;

  public void putObject(String bucket, String key, File content)
      throws IOException;




  public ObjectListResult listObject(String bucket, String startKey, String endKey)
      throws IOException;

  public ObjectListResult listObject(ListObjectRequest request) throws IOException;

  public ObjectListResult listObjectByPrefix(String bucket, String dir, String prefix,
      String startKey)
      throws IOException;

  public ObjectListResult listObjectByDir(String bucket, String dir, String startKey)
      throws IOException;

  public HosObject getObject(String bucket, String key) throws IOException;

  public void createBucket(String bucketName, String detail)
      throws IOException;


  public BucketModel getBucketInfo(String bucketName) throws IOException;

}
