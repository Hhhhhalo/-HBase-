package com.imooc.bigdata.hos.sdk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HosClientFactory {

  private static Map<String, HosClient> clientCache = new ConcurrentHashMap<>();

  /**
   * create mosclient by endpoints and token.
   *
   * @param endpoints seperated by comma,eg:http://127.0.0.1:80801,http://127.0.0.1:80802
   * @param token auth token
   * @return client
   */
  public static HosClient getOrClient(String endpoints, String token) {
    String key = endpoints + "_" + token;
    if (clientCache.containsKey(key)) {
      return clientCache.get(key);
    } else {
      HosClient client = new HosClientImpl(endpoints, token);
      clientCache.put(key, client);
      return client;
    }

  }
}
