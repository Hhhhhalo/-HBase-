package com.imooc.bigdata.hos.sdk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.imooc.bigdata.hos.common.BucketModel;
import com.imooc.bigdata.hos.common.HosHeaders;
import com.imooc.bigdata.hos.common.HosObject;
import com.imooc.bigdata.hos.common.HosObjectSummary;
import com.imooc.bigdata.hos.common.ListObjectRequest;
import com.imooc.bigdata.hos.common.ObjectListResult;
import com.imooc.bigdata.hos.common.ObjectMetaData;
import com.imooc.bigdata.hos.common.PutRequest;
import com.imooc.bigdata.hos.common.util.JsonUtil;

import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class HosClientImpl implements HosClient {

  private static Logger logger = Logger.getLogger(HosClientImpl.class.getName());

  private String hosServer;
  private String schema;
  private String host;
  private int port = 80;
  private String token;
  private OkHttpClient client;


  public HosClientImpl(String mosServer, String token) {
    this.hosServer = mosServer;
    String[] ss = mosServer.split("://", 2);
    this.schema = ss[0];
    String[] ss1 = ss[1].split(":", 2);
    this.host = ss1[0];
    if (ss1.length == 1) {
      if (schema.equals("https")) {
        port = 443;
      } else {
        port = 80;
      }
    } else {
      port = Integer.parseInt(ss1[1]);
    }
    this.token = token;
    ConnectionPool pool = new ConnectionPool(10, 30, TimeUnit.SECONDS);
    OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120L, TimeUnit.SECONDS)
        .writeTimeout(120L, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .connectionPool(pool);
    Interceptor interceptor = new Interceptor() {
      @Override
      public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        boolean success = false;
        int tryCount = 0;
        int maxLimit = 5;
        while (!success && tryCount < maxLimit) {
          if (tryCount > 0) {
            logger.info("intercept:" + "retry request - " + tryCount);
          }
          response = chain.proceed(request);
          if (response.code() == 404) {
            break;
          }
          success = response.isSuccessful();
          tryCount++;
          if (success) {
            return response;
          }
          try {
            Thread.sleep(10000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        return response;
      }
    };
    client = httpClientBuilder.addInterceptor(interceptor).build();
    Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
  }

  @Override
  public List<BucketModel> listBucket() throws IOException {
    Headers headers = this.buildHeaders(null, this.token, null);
    Request request =
        new Request.Builder()
            .headers(headers)
            .url(new HttpUrl.Builder()
                .scheme(this.schema)
                .host(this.host)
                .port(this.port)
                .addPathSegment("/hos/v1/bucket/list")
                .build()).get().build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      String msg = response.body().string();
      response.close();
      throw new RuntimeException(msg);
    }
    String json = response.body().string();
    List<BucketModel> buckets = JsonUtil.fromJsonList(BucketModel.class, json);
    response.close();
    return buckets;
  }

  @Override
  public void putObject(PutRequest putRequest) throws IOException {
    if (!putRequest.getKey().startsWith("/")) {
      throw new RuntimeException("object key must start with /");
    }
    RequestBody contentBody = null;
    if (putRequest.getContent() != null) {
      if (putRequest.getMediaType() == null) {
        putRequest.setMediaType("application/octet-stream");
        //warn media type not set
      }
      contentBody = RequestBody
          .create(MediaType.parse(putRequest.getMediaType()), putRequest.getContent());
    }
    if (putRequest.getFile() != null) {
      String extMime = MimeUtil.getFileMimeType(putRequest.getFile());
      if (extMime != null && putRequest.getMediaType() == null) {
        putRequest.setMediaType(extMime);
      }
      contentBody = RequestBody
          .create(MediaType.parse(putRequest.getMediaType()), putRequest.getFile());
    }

    MultipartBody.Builder bodyBuilder =
        new MultipartBody.Builder().setType(MultipartBody.FORM);
    bodyBuilder.addFormDataPart("bucket", putRequest.getBucket());
    if (putRequest.getMediaType() != null) {
      bodyBuilder.addFormDataPart("mediaType", putRequest.getMediaType());
    }
    bodyBuilder.addFormDataPart("key", putRequest.getKey());
    RequestBody requestBody = null;
    if (contentBody != null) {
      bodyBuilder.addFormDataPart("content", "content", contentBody);
    }

    requestBody = bodyBuilder.build();
    Headers headers = this
        .buildHeaders(putRequest.getAttrs(), this.token, putRequest.getContentEncoding());
    Request.Builder reqBuilder =
        new Request.Builder()
            .headers(headers)
            .url(new HttpUrl.Builder()
                .scheme(this.schema)
                .host(this.host)
                .port(this.port)
                .addPathSegment("/hos/v1/object")
                .build())
            .post(requestBody);
    Request request = reqBuilder.build();
    Response response = null;
    try {
      response = client.newCall(request).execute();
      if (!response.isSuccessful()) {
        String error = "";
        if (response.body() != null) {
          error = response.body().string();
        }
        throw new IOException("put object failed:" + error);
      }
    } finally {
      response.close();
    }
  }

  @Override
  public void putObject(String bucket, String key) throws IOException {
    if (!key.endsWith("/")) {
      throw new IOException("plain object content is empty");
    }
    PutRequest putRequest = new PutRequest(bucket, key, null);
    this.putObject(putRequest);
  }

  @Override
  public void putObject(String bucket, String key,
      byte[] content, String mediaType) throws IOException {
    if (content == null || content.length == 0) {
      throw new IOException("plain object content is empty");
    }
    PutRequest putRequest = new PutRequest(bucket, key, content, mediaType);
    this.putObject(putRequest);
  }

  @Override
  public void putObject(String bucket, String key, byte[] content, String mediaType,
      String contentEncoding) throws IOException {
    PutRequest putRequest = new PutRequest(bucket, key, content, mediaType);
    putRequest.setContentEncoding(contentEncoding);
    this.putObject(putRequest);
  }

  @Override
  public void putObject(String bucket, String key, File content, String mediaType)
      throws IOException {
    if (!content.exists()) {
      throw new FileNotFoundException(content.getAbsolutePath());
    }
    if (content.length() == 0) {
      throw new IOException("plain object content is empty");
    }
    PutRequest putRequest = new PutRequest(bucket, key, content, mediaType);
    this.putObject(putRequest);

  }

  @Override
  public void putObject(String bucket, String key, File content, String mediaType,
      String contentEncoding) throws IOException {
    PutRequest putRequest = new PutRequest(bucket, key, content, mediaType);
    putRequest.setContentEncoding(contentEncoding);
    this.putObject(putRequest);
  }

  @Override
  public void putObject(String bucket, String key, File content) throws IOException {
    PutRequest putRequest = new PutRequest(bucket, key, content, MimeUtil.getFileMimeType(content));
    this.putObject(putRequest);
  }

  @Override
  public void deleteObject(String bucket, String key) throws IOException {
    Headers headers = this.buildHeaders(null, this.token, null);
    Request request =
        new Request.Builder()
            .headers(headers)
            .url(new HttpUrl.Builder()
                .scheme(this.schema)
                .host(this.host)
                .port(this.port)
                .addPathSegment("/hos/v1/object")
                .addQueryParameter("bucket", bucket)
                .addQueryParameter("key", key)
                .build()).delete().build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      String msg = response.body().string();
      response.close();
      throw new RuntimeException(msg);
    }
    response.close();

  }

  @Override
  public HosObjectSummary getObjectSummary(String bucket, String key) throws IOException {
    Headers headers = this.buildHeaders(null, this.token, null);
    Request request =
        new Request.Builder()
            .headers(headers)
            .url(new HttpUrl.Builder()
                .scheme(this.schema)
                .host(this.host)
                .port(this.port)
                .addPathSegment("/hos/v1/object/info")
                .addQueryParameter("bucket", bucket)
                .addQueryParameter("key", key)
                .build()).get().build();
    Response response = client.newCall(request).execute();
    try {
      if (!response.isSuccessful()) {
        if (response.code() == 404) {
          return null;
        }
        throw new RuntimeException(response.body().string());
      }
      String json = response.body().string();
      HosObjectSummary summary = JsonUtil.fromJson(HosObjectSummary.class, json);
      return summary;
    } finally {
      if (response != null) {
        response.close();
      }
    }

  }

  @Override
  public ObjectListResult listObject(String bucket, String startKey, String endKey)
      throws IOException {
    Headers headers = this.buildHeaders(null, this.token, null);
    Request request =
        new Request.Builder()
            .headers(headers)
            .url(new HttpUrl.Builder()
                .scheme(this.schema)
                .host(this.host)
                .port(this.port)
                .addPathSegment("/hos/v1/object/list")
                .addQueryParameter("bucket", bucket)
                .addQueryParameter("startKey", startKey)
                .addQueryParameter("endKey", endKey)
                .build()).build();
    Response response = client.newCall(request).execute();
    if (response.isSuccessful()) {
      String json = response.body().string();
      response.close();
      return JsonUtil.fromJson(ObjectListResult.class, json);
    }
    response.close();
    throw new IOException("list object error");
  }

  @Override
  public ObjectListResult listObject(ListObjectRequest request) throws IOException {
    return this.listObject(request.getBucket(), request.getStartKey(), request.getEndKey());
  }


  @Override
  public ObjectListResult listObjectByPrefix(String bucket, String dir, String prefix,
      String startKey)
      throws IOException {
    Headers headers = this.buildHeaders(null, this.token, null);
    Request request =
        new Request.Builder()
            .headers(headers)
            .url(new HttpUrl.Builder()
                .scheme(this.schema)
                .host(this.host)
                .port(this.port)
                .addPathSegment("/hos/v1/object/list/prefix")
                .addQueryParameter("bucket", bucket)
                .addQueryParameter("prefix", prefix)
                .addQueryParameter("dir", dir)
                .addQueryParameter("startKey", startKey)
                .build()).build();
    Response response = client.newCall(request).execute();
    if (response.isSuccessful()) {
      String json = response.body().string();
      response.close();
      return JsonUtil.fromJson(ObjectListResult.class, json);
    }
    response.close();
    throw new IOException("list object error");
  }

  @Override
  public ObjectListResult listObjectByDir(String bucket, String dir, String startKey)
      throws IOException {
    if (!(dir.startsWith("/") && dir.endsWith("/"))) {
      throw new RuntimeException("dir must start with / and end with /");
    }
    if (startKey == "" || startKey == null) {
      startKey = dir;
    }
    Headers headers = this.buildHeaders(null, this.token, null);
    Request request =
        new Request.Builder()
            .headers(headers)
            .url(new HttpUrl.Builder()
                .scheme(this.schema)
                .host(this.host)
                .port(this.port)
                .addPathSegment("/hos/v1/object/list/dir")
                .addQueryParameter("bucket", bucket)
                .addQueryParameter("dir", dir)
                .addQueryParameter("startKey", startKey)
                .build()).build();
    Response response = client.newCall(request).execute();
    if (response.isSuccessful()) {
      String json = response.body().string();
      response.close();
      return JsonUtil.fromJson(ObjectListResult.class, json);
    }
    response.close();
    throw new IOException("list object error");
  }

  /**
   * user must close object inputstream.
   *
   * @param bucket bucket
   * @param key key
   * @return object
   * @throws IOException ioe
   */
  @Override
  public HosObject getObject(String bucket, String key) throws IOException {
    Headers headers = this.buildHeaders(null, this.token, null);
    Request request =
        new Request.Builder()
            .headers(headers)
            .url(new HttpUrl.Builder()
                .scheme(this.schema)
                .host(this.host)
                .port(this.port)
                .addPathSegments("/hos/v1/object/content")
                .addQueryParameter("bucket", bucket)
                .addQueryParameter("key", key)
                .build()).get().build();
    Response response = client.newCall(request).execute();
    if (response.isSuccessful()) {
      HosObject object = new HosObject(response);
      object.setContent(response.body().byteStream());
      object.setMetaData(this.buildMetaData(response));
      return object;
    }
    response.close();
    return null;
  }

  @Override
  public void createBucket(String bucketName) throws IOException {
    this.createBucket(bucketName, "");
  }

  @Override
  public void createBucket(String bucketName, String detail)
      throws IOException {
    Headers headers = this.buildHeaders(null, this.token, null);
    RequestBody reqbody = RequestBody.create(null, new byte[0]);
    Request request =
        new Request.Builder()
            .headers(headers)
            .url(new HttpUrl.Builder()
                .scheme(this.schema)
                .host(this.host)
                .port(this.port)
                .addPathSegment("/hos/v1/bucket")
                .addQueryParameter("bucket", bucketName)
                .addQueryParameter("detail", detail)
                .build()).post(reqbody).build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      String message = response.body().string();
      response.close();
      throw new IOException("create bucket error:" + message);
    }
    response.close();
  }

  @Override
  public void deleteBucket(String bucketName) throws IOException {
    Headers headers = this.buildHeaders(null, this.token, null);
    Request request =
        new Request.Builder()
            .headers(headers)
            .url(new HttpUrl.Builder()
                .scheme(this.schema)
                .host(this.host)
                .port(this.port)
                .addPathSegment("/hos/v1/bucket")
                .addQueryParameter("bucket", bucketName)
                .build()).delete().build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      String message = response.body().string();
      response.close();
      throw new IOException("delete bucket error:" + message);
    }
    response.close();
  }

  @Override
  public BucketModel getBucketInfo(String bucketName) throws IOException {
    Headers headers = this.buildHeaders(null, this.token, null);
    Request request =
        new Request.Builder()
            .headers(headers)
            .url(new HttpUrl.Builder()
                .scheme(this.schema)
                .host(this.host)
                .port(this.port)
                .addPathSegment("/hos/v1/bucket")
                .addQueryParameter("bucket", bucketName)
                .build()).get().build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      String message = response.body().string();
      response.close();
      throw new IOException("bucket not found");
    }
    String json = response.body().string();
    return JsonUtil.fromJson(BucketModel.class, json);
  }

  private Headers buildHeaders(Map<String, String> attrs, String token, String contentEncoding) {
    Map<String, String> headerMap = new HashMap<>();
    if (contentEncoding != null) {
      headerMap.put("content-encoding", contentEncoding);
    }
    headerMap.put("X-Auth-Token", token);
    if (attrs != null && attrs.size() > 0) {
      attrs.forEach(new BiConsumer<String, String>() {
        @Override
        public void accept(String s, String s2) {
          headerMap.put(HosHeaders.COMMON_ATTR_PREFIX + s, s2);
        }
      });
    }
    Headers headers = Headers.of(headerMap);
    return headers;
  }

  private ObjectMetaData buildMetaData(Response response) {
    ObjectMetaData metaData = new ObjectMetaData();
    metaData.setBucket(response.header(HosHeaders.COMMON_OBJ_BUCKET));
    metaData.setKey(response.header(HosHeaders.COMMON_OBJ_KEY));
    metaData.setLastModifyTime(Long.parseLong(response.header("Last-Modified")));
    metaData.setMediaType(response.header("Content-Type"));
    metaData.setLength(Long.parseLong(response.header(HosHeaders.RESPONSE_OBJ_LENGTH)));
    return metaData;
  }

}
