package pl.otros.logview.stats;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.services.StatsReporterService;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpStatsReporterService implements StatsReporterService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpStatsReporterService.class.getName());
  private static final String SEND_URL = "http://otroslogviewer.appspot.com/services/statsReporter";

  @Override
  public void sendStats(Map<String, Long> stats, String uuid, String olvVersion, String javaVersion) {
    String r = stats
      .entrySet()
      .stream()
      .sorted(Comparator.comparing(Map.Entry::getKey))
      .map(kv -> kv.getKey() + "=" + kv.getValue()).collect(Collectors.joining("\n"));

    HttpClient httpClient = new HttpClient();
    PostMethod method = new PostMethod(SEND_URL);
    try {
      method.setRequestEntity(new StringRequestEntity(r, "text/plain", "UTF-8"));
      method.addRequestHeader("uuid", uuid);
      method.addRequestHeader("olvVersion", olvVersion);
      method.addRequestHeader("javaVersion", javaVersion);
      httpClient.executeMethod(method);
    } catch (Exception e) {
      //User is not interested in issues with sending report
      LOGGER.warn("Can't send stats to server", e);
    }
  }

}
