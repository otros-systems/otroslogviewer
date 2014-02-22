package pl.otros.logview.gui.services.jumptocdoe;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.message.LocationInfo;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the interface with the JumpToCode plugin for IntelliJ IDEA
 */
public class JumpToCodeClient {
  private static final Logger LOGGER = Logger.getLogger(JumpToCodeClient.class.getName());
  private HttpClient client;
  private Configuration configuration;
  private Cache<LocationInfo, Boolean> locationInfoCache;
  private Cache<LocationInfo, String> locationSourceCache;
  private Cache<String, JumpToCodeService.IDE> ideCache;

  public String getContent(final LocationInfo locationInfo) throws IOException {
    try {
      return locationSourceCache.get(locationInfo,new Callable<String>() {
        @Override
        public String call() throws Exception {
          HttpMethod httpMethod = buildMethod(locationInfo, HttpOperation.GET_SOURCE);
          String content = executeAndGetContent(httpMethod);
          return content;
        }
      }) ;
    } catch (ExecutionException e) {
      return "";
    }
  }

  private enum HttpOperation {
    JUMP("jump"),TEST("test"),GET_SOURCE("content");
    String opeartion;

    HttpOperation(String opeartion) {
      this.opeartion = opeartion;
    }

    public String getOpeartion() {
      return opeartion;
    }

  }

  public JumpToCodeClient(Configuration configuration) {
    this.configuration = configuration;
    this.client = new HttpClient();
    //
    client.getHttpConnectionManager().getParams().setConnectionTimeout(150);
    locationInfoCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterAccess(15, TimeUnit.MINUTES).build();
    locationSourceCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(15, TimeUnit.MINUTES).build();
    ideCache = CacheBuilder.newBuilder().maximumSize(1).expireAfterWrite(10, TimeUnit.SECONDS).build();
  }

  /**
   * get the URL for jumping to the given location
   *
   * @param locationInfo the location
   * @return the JumpToCode url or null when location is unknown
   */
  public String getUrl(LocationInfo locationInfo) {
    if (configuration.getBoolean(ConfKeys.JUMP_TO_CODE_ENABLED, true) && isJumpable(locationInfo)) {
      HttpMethod method = buildMethod(locationInfo, HttpOperation.JUMP);
      return String.format("http://%s:%d/?%s",
          configuration.getString(ConfKeys.JUMP_TO_CODE_HOST, "localhost"),
          configuration.getInt(ConfKeys.JUMP_TO_CODE_PORT, 5986),
          method.getQueryString());
    } else {
      return null;
    }
  }

  /**
   * jump to location represented by given URL
   *
   * @param url a JumpToCode url that represents a source location
   */
  protected void jumpTo(String url) throws IOException {
    LOGGER.finest("Jumping to URL " + url);
    HttpMethod method = new GetMethod(url);
    int execute = execute(method);
    LOGGER.finest("Result status: " + execute);
  }

  private HttpMethod buildMethod(LocationInfo locationInfo, HttpOperation httpOperation) {
    String url = String.format("http://%s:%d/",
        configuration.getString(ConfKeys.JUMP_TO_CODE_HOST, "localhost"),
        configuration.getInt(ConfKeys.JUMP_TO_CODE_PORT, 5986));
    HttpMethod method = new GetMethod(url);
    NameValuePair[] pair =  new NameValuePair[4];
    String lineNumber = String.valueOf(locationInfo.getLineNumber());
    pair[0] = new NameValuePair("p", locationInfo.getPackageName());
    pair[1] = new NameValuePair("f", locationInfo.getFileName());
    pair[2] = new NameValuePair("l", lineNumber);
    pair[3] = new NameValuePair("o", httpOperation.getOpeartion());

    method.setQueryString(pair);
    return method;
  }

  public boolean isJumpable(final LocationInfo locationInfo) {
    if (locationInfo == null) {
      return false;
    }
    try {
      return locationInfoCache.get(locationInfo, new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          LOGGER.finest("Checking if " + locationInfo + " is jumpable.");
          HttpMethod method = buildMethod(locationInfo, HttpOperation.TEST);
          try {
            int execute = execute(method);
            LOGGER.finest("HTTP status line is " + execute);
            return (execute == HttpStatus.SC_OK);
          } catch (IOException e) {
            LOGGER.log(Level.FINEST, "IOException when checking if location if jumpable", e);
            return false;
          }
        }
      });
    } catch (ExecutionException e) {
      return false;
    }
  }

  private int execute(HttpMethod method) throws IOException {
    try {
      // we don't even read the response body
      // the statusCode is enough information
      return client.executeMethod(method);
    } catch (IOException e) {
      // probably either configuration error or IDEA is not running
      throw new IOException("Failed to communicate with JumpToCode", e);
    } finally {
      method.releaseConnection();
    }
  }

  private String executeAndGetContent(HttpMethod method) throws  IOException {
    try {
      // we don't even read the response body
      // the statusCode is enough information
      int status = client.executeMethod(method);
      if (status != HttpStatus.SC_OK){
        return "";
      }
      return method.getResponseBodyAsString();
    } catch (IOException e) {
      // probably either configuration error or IDEA is not running
      throw new IOException("Failed to communicate with JumpToCode", e);
    } finally {
      method.releaseConnection();
    }
  }


  public JumpToCodeService.IDE getIde() {
    try {
      return ideCache.get("", new GetIdeCallable());
    } catch (ExecutionException e) {
      LOGGER.finest("Can't get ide: " + e.getMessage());
    }
    return JumpToCodeService.IDE.DISONECTED;
  }

  public void clearLocationCaches() {
    locationInfoCache.invalidateAll();
  }

  private class GetIdeCallable implements Callable<JumpToCodeService.IDE> {
    @Override
    public JumpToCodeService.IDE call() throws Exception {
      HttpMethod method = buildMethod(new LocationInfo("", ""), HttpOperation.TEST);
      JumpToCodeService.IDE ide = JumpToCodeService.IDE.DISONECTED;
      try {
        // we don't even read the response body
        // the statusCode is enough information
        client.executeMethod(method);
        Header responseHeader = method.getResponseHeader("IDE");
        if (responseHeader != null) {
          String value = responseHeader.getValue();
          if (StringUtils.equalsIgnoreCase(value, "eclipse")) {
            ide = JumpToCodeService.IDE.Eclipse;
          } else {
            ide = JumpToCodeService.IDE.IDEA;
          }
        }
        return ide;
      } catch (IOException e) {
        // IDE is not available;
        LOGGER.finest("IDE is not available: " + e.getMessage());
        return JumpToCodeService.IDE.DISONECTED;
      } finally {
        method.releaseConnection();
      }
    }
  }


}
