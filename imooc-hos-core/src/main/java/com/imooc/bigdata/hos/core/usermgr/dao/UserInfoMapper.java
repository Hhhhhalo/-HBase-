package com.imooc.bigdata.hos.core.usermgr.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;

import com.imooc.bigdata.hos.core.usermgr.model.UserInfo;

/**
 * Created by jixin on 17-3-9.
 */
@Mapper
public interface UserInfoMapper {

  void addUser(@Param("userInfo") UserInfo userInfo);

  int updateUserInfo(@Param("userId") String userId, @Param("password") String password,
      @Param("detail") String detail);

  int deleteUser(@Param("userId") String userId);

  @ResultMap("UserInfoResultMap")
  UserInfo getUserInfo(@Param("userId") String userId);

  UserInfo checkPassword(@Param("userName") String userName,
      @Param("password") String password);

  @ResultMap("UserInfoResultMap")
  UserInfo getUserInfoByName(@Param("userName") String userName);
}
