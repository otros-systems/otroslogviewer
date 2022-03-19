package pl.otros.logview.gui.services.jumptocode;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.configuration.BaseConfiguration;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.Ide;
import pl.otros.logview.api.services.JumpToCodeService;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class JumpToCodeClientTest {

  private WireMockServer wireMock;
  private JumpToCodeClient jumpToCodeClient;

  private static final String LOGGER_PATTERNS_RESPONSE = "[\n" +
    "  {\n" +
    "    \"fileName\": \"logback.xml\",\n" +
    "    \"patterns\": [\n" +
    "      \"%level %date %logger %msg%n\",\n" +
    "      \"%level %date %logger %t %msg%n\"\n" +
    "    ],\n" +
    "    \"type\": \"Logback\"\n" +
    "  }\n" +
    "]";

  @BeforeMethod
  public void setUp() {
    wireMock = new WireMockServer(wireMockConfig());

    wireMock.stubFor(get(urlPathEqualTo("/"))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withHeader("plugin-features", "jumpByLine,jumpByMessage,contentByLine,contentByMessage,allFile,loggersConfig")
        .withHeader("ide", "idea")
        .withBody("")));

    wireMock.stubFor(get(urlPathEqualTo("/"))
      .withQueryParam("o", equalTo("test"))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withHeader("plugin-features", "jumpByLine,jumpByMessage,contentByLine,contentByMessage,allFile,loggersConfig")
        .withHeader("ide", "idea")
        .withBody("")));

    wireMock.stubFor(get(urlPathEqualTo("/"))
      .withQueryParam("o", equalTo("loggersConfig"))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withHeader("plugin-features", "jumpByLine,jumpByMessage,contentByLine,contentByMessage,allFile,loggersConfig")
        .withHeader("ide", "idea")
        .withBody(LOGGER_PATTERNS_RESPONSE)));

    wireMock.start();
    final BaseConfiguration configuration = new BaseConfiguration();
    configuration.setProperty(ConfKeys.JUMP_TO_CODE_HOST, "localhost");
    configuration.setProperty(ConfKeys.JUMP_TO_CODE_PORT, wireMock.port());
    jumpToCodeClient = new JumpToCodeClient(configuration);

  }

  @AfterMethod
  public void tearDown() {
    wireMock.stop();
  }

  @Test
  public void testCapabilities() throws Exception {
    Assertions.assertThat(jumpToCodeClient.capabilities())
      .containsOnly(
        JumpToCodeService.Capabilities.AllFile,
        JumpToCodeService.Capabilities.ContentByLine,
        JumpToCodeService.Capabilities.ContentByMessage,
        JumpToCodeService.Capabilities.JumpByLine,
        JumpToCodeService.Capabilities.JumpByMessage,
        JumpToCodeService.Capabilities.LoggersConfig
      );
  }

  @Test
  public void testGetIde() {
    Assertions.assertThat(jumpToCodeClient.getIde())
      .isEqualTo(Ide.IDEA);
  }

  @Test
  public void testLoggerPatterns() throws Exception {
    //given
    //when
    //then
    Assertions.assertThat(jumpToCodeClient.loggerPatterns())
      .containsOnly(
        "%level %date %logger %t %msg%n",
        "%level %date %logger %msg%n"
      );
  }
}