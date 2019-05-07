package com.imooc.bigdata.hos.web.security;

import java.util.List;

import com.imooc.bigdata.hos.core.usermgr.model.SystemRole;
import com.imooc.bigdata.hos.core.usermgr.model.UserInfo;

public interface IOperationAccessControl {

  public UserInfo checkLogin(String userName, String password);

  public boolean checkSystemRole(SystemRole systemRole1, SystemRole systemRole2);
  public boolean checkSystemRole(SystemRole systemRole1, String userId);

  public boolean checkTokenOwner(String userName, String token);

  public boolean checkBucketOwner(String userName, String bucketName);

  public boolean checkPermission(String token, String bucket);

}
