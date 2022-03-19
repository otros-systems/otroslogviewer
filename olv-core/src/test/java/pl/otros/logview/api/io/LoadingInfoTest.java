package pl.otros.logview.api.io;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.github.tomakehurst.wiremock.WireMockServer;

public class LoadingInfoTest {

  private static final int PORT = 45501;
  private static final String HTTP_GZIPPED = "http://127.0.0.1:" + PORT + "/log.txt.gz";
  private static final String HTTP_NOT_GZIPPED = "http://127.0.0.1:" + PORT + "/log.txt";
  private static FileSystemManager fsManager;
  private static WireMockServer wireMock;

  @BeforeClass
  public static void setUp() throws IOException {
    fsManager = VFS.getManager();
    wireMock = new WireMockServer(wireMockConfig().port(PORT));

    final byte[] gzipped = toByteArray(getSystemResourceAsStream("hierarchy/hierarchy.log.gz"));
    final byte[] notGzipped = toByteArray(getSystemResourceAsStream("hierarchy/hierarchy.log"));

    wireMock.stubFor(
        get(urlEqualTo("/log.txt")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/plain").withBody(notGzipped)));

    wireMock.stubFor(
        get(urlEqualTo("/log.txt.gz")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/plain").withBody(gzipped)));

    wireMock.start();
  }

  @AfterClass
  public static void tearDown() {
    wireMock.shutdown();
  }

  @Test
  public void testEmptyFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("empty.log"))) {
      assertFalse(logFileContent.isGzipped());
      assertEquals(0, logFileContent.getInputStreamBufferedStart().length);
      assertEquals("", asString(logFileContent));
    }
  }

  @Test
  public void testEmptyGzippedFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("empty.log.gz"))) {
      assertTrue(logFileContent.isGzipped());
      assertEquals(0, logFileContent.getInputStreamBufferedStart().length);
      assertEquals("", asString(logFileContent));
    }
  }

  @Test
  public void testSmallFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("smallFile.txt"))) {
      assertFalse(logFileContent.isGzipped());
      assertEquals(10, logFileContent.getInputStreamBufferedStart().length);
      assertEquals("smallFile\n", asString(logFileContent));
    }
  }

  @Test
  public void testSmallGzippedFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("smallFile.txt.gz"))) {
      assertTrue(logFileContent.isGzipped());
      assertEquals(10, logFileContent.getInputStreamBufferedStart().length);
      assertEquals("smallFile\n", asString(logFileContent));
    }
  }

  @Test
  public void testAverageFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("jul_log.txt"))) {
      assertFalse(logFileContent.isGzipped());
      assertEquals(15_300, asString(logFileContent).length());
    }
  }

  @Test
  public void testAverageGzippedFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("jul_log.txt.gz"))) {
      assertTrue(logFileContent.isGzipped());
      assertEquals(15_300, asString(logFileContent).length());
    }
  }

  @Test
  public void testLargerFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("hierarchy/hierarchy.log"))) {
      assertFalse(logFileContent.isGzipped());
      assertEquals(69_058, asString(logFileContent).length());
    }
  }

  @Test
  public void testLargerGzippedFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("hierarchy/hierarchy.log.gz"))) {
      assertTrue(logFileContent.isGzipped());
      assertEquals(69_058, asString(logFileContent).length());
    }
  }

  @Test
  public void testLargerHttp() throws Exception {
    try (LoadingInfo logFileContent = new LoadingInfo(fsManager.resolveFile(HTTP_NOT_GZIPPED))) {
      assertFalse(logFileContent.isGzipped());
      assertEquals(69_058, asString(logFileContent).length());
    }
  }

  @Test
  public void testLargerGzippedHttp() throws Exception {
    try (LoadingInfo logFileContent = new LoadingInfo(fsManager.resolveFile(HTTP_GZIPPED))) {
      assertTrue(logFileContent.isGzipped());
      assertEquals(69_058, asString(logFileContent).length());
    }
  }

  private FileObject asFileObject(String name) throws FileSystemException {
    return fsManager.resolveFile(getSystemResource(name).toExternalForm());
  }

  private String asString(LoadingInfo logFileContent) throws IOException {
    return IOUtils.toString(logFileContent.getContentInputStream(), UTF_8).replace("\r", "");
  }
}
