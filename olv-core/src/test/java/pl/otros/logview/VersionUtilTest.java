package pl.otros.logview;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.DataConfiguration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.OtrosApplication;

import java.net.Proxy;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.testng.Assert.assertEquals;

public class VersionUtilTest {

  @Test
  public void testGetCurrentVersion() throws Exception {
    //given
    final WireMockServer wireMockServer = new WireMockServer();
    wireMockServer
      .stubFor(
        get(urlMatching("/currentVersion.*"))
        .willReturn(aResponse()
        .withStatus(200)
        .withBody("currentVersion=1.2.3\n".getBytes()))
      );
    wireMockServer.start();

    final int port = wireMockServer.port();
    final VersionUtil versionUtil = new VersionUtil("http://localhost:" + port+"/currentVersion");
    final OtrosApplication otrosApplication = new OtrosApplication();
    otrosApplication.setConfiguration(new DataConfiguration(new BaseConfiguration()));
    otrosApplication.getConfiguration().setProperty(ConfKeys.UUID,"C9457787-AF59-4B9F-B4E8-FB75334EBEF8");

    //when
    final Optional<String> currentVersion = versionUtil.getCurrentVersion("1.2.3", Proxy.NO_PROXY, otrosApplication);
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
  public void testValidateResponse(String response, Optional<String> expected) throws Exception {
    final VersionUtil versionUtil = new VersionUtil();
    final Optional<String> actual = versionUtil.validateResponse(response);
    assertEquals(actual, expected);
  }
}