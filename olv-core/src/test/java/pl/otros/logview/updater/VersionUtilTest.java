package pl.otros.logview.updater;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.Proxy;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.testng.Assert.assertEquals;

public class VersionUtilTest {

  @Test
  public void testGetCurrentVersion() throws Exception {
    //given
    String versionJson = "{\n" +
      "    \"currentVersion\": {\n" +
      "        \"major\":1,\n" +
      "        \"minor\":2,\n" +
      "        \"patch\":3\n" +
      "    }\n" +
      "}";
    final WireMockServer wireMockServer = new WireMockServer();
    wireMockServer
      .stubFor(
        get(urlMatching("/currentVersion.*"))
          .willReturn(aResponse()
            .withStatus(200)
            .withBody(versionJson.getBytes()))
      );
    wireMockServer.start();

    final int port = wireMockServer.port();
    final VersionUtil versionUtil = new VersionUtil("http://localhost:" + port+"/currentVersion");

    //when
    final Optional<String> currentVersion = versionUtil.getCurrentVersion(Proxy.NO_PROXY);
    wireMockServer.stop();

    //then
    assertEquals(currentVersion,Optional.of("1.2.3"));

  }


  @DataProvider(name = "testValidateResponse")
  public Object[][] testValidateResponseData() {
    return new Object[][]{
      {"1", Optional.of("1")},
      {"11", Optional.of("11")},
      {"1.", Optional.empty()},
      {"1.23.", Optional.empty()},
      {"1.1", Optional.of("1.1")},
      {"1.2.3.4", Optional.of("1.2.3.4")},
      {"", Optional.empty()},
      {null, Optional.empty()},
      {"?", Optional.empty()}
    };
  }

  @Test(dataProvider = "testValidateResponse")
  public void testValidateResponse(String response, Optional<String> expected) {
    final VersionUtil versionUtil = new VersionUtil();
    final Optional<String> actual = versionUtil.validateResponse(response);
    assertEquals(actual, expected);
  }
}