package com.imooc.bigdata.hos.server.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;

import com.imooc.bigdata.hos.common.BucketModel;

/**
 * Created by jixin on 18-3-8.
 */
@Mapper
public interface BucketMapper {

  void addBucket(@Param("bucket") BucketModel bucketModel);

  int updateBucket(@Param("bucketName") String bucketName, @Param("detail") String detail);

  int deleteBucket(@Param("bucketName") String bucketName);

  @ResultMap("BucketResultMap")
  BucketModel getBucket(@Param("bucketId") String bucketId);

  @ResultMap("BucketResultMap")
  BucketModel getBucketByName(@Param("bucketName") String bucketName);

  @ResultMap("BucketResultMap")
  List<BucketModel> getBucketByCreator(@Param("creator") String creator);

  @ResultMap("BucketResultMap")
  List<BucketModel> getUserAuthorizedBuckets(@Param("token") String token);
}
