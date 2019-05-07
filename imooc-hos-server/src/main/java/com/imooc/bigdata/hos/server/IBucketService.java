package com.imooc.bigdata.hos.server;

import java.util.List;

import com.imooc.bigdata.hos.common.BucketModel;
import com.imooc.bigdata.hos.core.usermgr.model.UserInfo;

/**
 * Created by jixin on 18-3-13.
 */
public interface IBucketService {

  public boolean addBucket(UserInfo userInfo, String bucketName, String detail);

  public boolean deleteBucket(String bucketName);

  public boolean updateBucket(String bucketName, String detail);

  public BucketModel getBucketById(String bucketId);

  public BucketModel getBucketByName(String bucketName);

  public List<BucketModel> getUserBuckets(String token);
}
