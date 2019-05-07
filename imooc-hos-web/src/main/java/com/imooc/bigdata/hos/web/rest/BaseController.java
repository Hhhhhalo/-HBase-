package com.imooc.bigdata.hos.web.rest;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.bigdata.hos.core.ErrorCodes;
import com.imooc.bigdata.hos.core.HosException;
import com.imooc.bigdata.hos.web.security.IOperationAccessControl;

/**
 * Created by jixin on 17-3-6.
 */
@ControllerAdvice
public class BaseController {
  @Autowired
  @Qualifier("DefaultAccessControl")
  protected IOperationAccessControl operationAccessControl;

  /**
   * 异常处理.
   */
  @ExceptionHandler
  @ResponseBody
  public Map<String, Object> exceptionHandle(Exception ex, HttpServletResponse response) {
    ex.printStackTrace();
    if (HosException.class.isAssignableFrom(ex.getClass())) {
      HosException hosException = (HosException) ex;
      if (hosException.errorCode() == ErrorCodes.ERROR_PERMISSION_DENIED) {
        response.setStatus(403);
      } else {
        response.setStatus(500);
      }
      return getResultMap(hosException.errorCode(), null, hosException.errorMessage(), null);
    } else {
      response.setStatus(500);
      return getResultMap(500, null, ex.getMessage(), null);
    }
  }

  protected Map<String, Object> getResult(Object object) {
    return getResultMap(null, object, null, null);
  }


  protected Map<String, Object> getResult(Object object, Map<String, Object> extraMap) {
    return getResultMap(null, object, null, extraMap);
  }

  protected Map<String, Object> getError(int errCode, String errMsg) {
    return getResultMap(errCode, null, errMsg, null);
  }

  private Map<String, Object> getResultMap(Integer code, Object data, String msg,
      Map<String, Object> extraMap) {
    Map<String, Object> map = new HashMap<String, Object>();
    if (code == null || code.equals(200)) {
      map.put("code", 200);
      map.put("data", data);
    } else {
      map.put("code", code);
      map.put("msg", msg);
    }
    if (extraMap != null && !extraMap.isEmpty()) {
      map.putAll(extraMap);
    }
    return map;
  }

}
