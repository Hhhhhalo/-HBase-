package com.imooc.bigdata.hos.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imooc.bigdata.hos.common.BucketModel;
import com.imooc.bigdata.hos.core.authmgr.IAuthService;
import com.imooc.bigdata.hos.core.authmgr.model.ServiceAuth;
import com.imooc.bigdata.hos.core.usermgr.model.UserInfo;
import com.imooc.bigdata.hos.server.dao.BucketMapper;

/**
 * Created by jixin on 18-3-13.
 */
@Service("bucketServiceImpl")
@Transactional
public class BucketServiceImpl implements IBucketService {

  @Autowired
  BucketMapper bucketMapper;
  @Autowired
  @Qualifier("authServiceImpl")
  IAuthService authService;

  @Override
  public boolean addBucket(UserInfo userInfo, String bucketName, String detail) {
    BucketModel bucketModel = new BucketModel(bucketName, userInfo.getUserName(), detail);
    bucketMapper.addBucket(bucketModel);
    ServiceAuth serviceAuth = new ServiceAuth();
    serviceAuth.setBucketName(bucketName);
    serviceAuth.setTargetToken(userInfo.getUserId());
    authService.addAuth(serviceAuth);
    return true;
  }

  @Override
  public boolean deleteBucket(String bucketName) {
    bucketMapper.deleteBucket(bucketName);
    authService.deleteAuthByBucket(bucketName);
    return true;
  }

  @Override
  public boolean updateBucket(String bucketName, String detail) {
    bucketMapper.updateBucket(bucketName, detail);
    return true;
  }

  @Override
  public BucketModel getBucketById(String bucketId) {
    return bucketMapper.getBucket(bucketId);
  }

  @Override
  public BucketModel getBucketByName(String bucketName) {
    return bucketMapper.getBucketByName(bucketName);
  }

  @Override
  public List<BucketModel> getUserBuckets(String token) {
    return bucketMapper.getUserAuthorizedBuckets(token);
  }
}
