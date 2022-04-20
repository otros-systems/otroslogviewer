package pl.otros.logview.api.io;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

public class LoadingInfoTest {

  private static final int PORT = 45501;
  private static final String HTTP_GZIPPED = "http://127.0.0.1:" + PORT + "/log.txt.gz";
  private static final String HTTP_ZIPPED = "http://127.0.0.1:" + PORT + "/log.txt.zip";
  private static final String HTTP_NOT_GZIPPED = "http://127.0.0.1:" + PORT + "/log.txt";
  private static FileSystemManager fsManager;
  private static WireMockServer wireMock;

  @BeforeClass
  public static void setUp() throws IOException {
    fsManager = VFS.getManager();
    wireMock = new WireMockServer(wireMockConfig().port(PORT));

    final byte[] gzipped = toByteArray(getSystemResourceAsStream("hierarchy/hierarchy.log.gz"));
    final byte[] zipped = toByteArray(getSystemResourceAsStream("hierarchy/hierarchy.log.zip"));
    final byte[] notGzipped = toByteArray(getSystemResourceAsStream("hierarchy/hierarchy.log"));

    wireMock.stubFor(
        get(urlEqualTo("/log.txt")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/plain").withBody(notGzipped)));

    wireMock.stubFor(
        get(urlEqualTo("/log.txt.gz")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/plain").withBody(gzipped)));

    wireMock.stubFor(
      get(urlEqualTo("/log.txt.zip")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/plain").withBody(zipped)));

    wireMock.start();
  }

  @AfterClass
  public static void tearDown() {
    wireMock.shutdown();
  }

  @Test
  public void testEmptyFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("empty.log"))) {
      assertFalse(logFileContent.getCompression().isCompressed());
      assertEquals(0, logFileContent.getInputStreamBufferedStart().length);
      assertEquals("", asString(logFileContent));
    }
  }

  @Test
  public void testEmptyGzippedFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("empty.log.gz"))) {
      assertEquals(CompressType.GZIP, logFileContent.getCompression());
      assertEquals(0, logFileContent.getInputStreamBufferedStart().length);
      assertEquals("", asString(logFileContent));
    }
  }

  @Test
  public void testEmptyZippedFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("empty.log.zip"))) {
      assertEquals(CompressType.ZIP, logFileContent.getCompression());
      assertEquals(0, logFileContent.getInputStreamBufferedStart().length);
      assertEquals("", asString(logFileContent));
    }
  }

  @Test
  public void testSmallFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("smallFile.txt"))) {
      assertEquals(CompressType.NONE, logFileContent.getCompression());
      assertEquals(10, logFileContent.getInputStreamBufferedStart().length);
      assertEquals("smallFile\n", asString(logFileContent));
    }
  }

  @Test
  public void testSmallGzippedFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("smallFile.txt.gz"))) {
      assertEquals(CompressType.GZIP, logFileContent.getCompression());
      assertEquals(10, logFileContent.getInputStreamBufferedStart().length);
      assertEquals("smallFile\n", asString(logFileContent));
    }
  }

  @Test
  public void testSmallZippedFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("smallFile.txt.zip"))) {
      assertEquals(CompressType.ZIP, logFileContent.getCompression());
      assertEquals(10, logFileContent.getInputStreamBufferedStart().length);
      assertEquals("smallFile\n", asString(logFileContent));
    }
  }

  @Test
  public void testAverageFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("jul_log.txt"))) {
      assertEquals(CompressType.NONE, logFileContent.getCompression());
      assertEquals(15_300, asString(logFileContent).length());
    }
  }

  @Test
  public void testAverageGzippedFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("jul_log.txt.gz"))) {
      assertEquals(CompressType.GZIP, logFileContent.getCompression());
      assertEquals(15_300, asString(logFileContent).length());
    }
  }

  @Test
  public void testAverageZippedFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("jul_log.txt.zip"))) {
      assertEquals(CompressType.ZIP, logFileContent.getCompression());
      assertEquals(15_300, asString(logFileContent).length());
    }
  }

  @Test
  public void testLargerFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("hierarchy/hierarchy.log"))) {
      assertEquals(CompressType.NONE, logFileContent.getCompression());
      assertEquals(69_058, asString(logFileContent).length());
    }
  }

  @Test
  public void testLargerGzippedFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("hierarchy/hierarchy.log.gz"))) {
      assertEquals(CompressType.GZIP, logFileContent.getCompression());
      assertEquals(69_058, asString(logFileContent).length());
    }
  }

  @Test
  public void testLargerZippedFile() throws IOException {
    try (LoadingInfo logFileContent = new LoadingInfo(asFileObject("hierarchy/hierarchy.log.zip"))) {
      assertEquals(CompressType.ZIP, logFileContent.getCompression());
      assertEquals(69_058, asString(logFileContent).length());
    }
  }

  @Test
  public void testLargerHttp() throws Exception {
    try (LoadingInfo logFileContent = new LoadingInfo(fsManager.resolveFile(HTTP_NOT_GZIPPED))) {
      assertEquals(CompressType.NONE, logFileContent.getCompression());
      assertEquals(69_058, asString(logFileContent).length());
    }
  }

  @Test
  public void testLargerGzippedHttp() throws Exception {
    try (LoadingInfo logFileContent = new LoadingInfo(fsManager.resolveFile(HTTP_GZIPPED))) {
      assertEquals(CompressType.GZIP, logFileContent.getCompression());
      assertEquals(69_058, asString(logFileContent).length());
    }
  }

  @Test
  public void testLargerZippedHttp() throws Exception {
    try (LoadingInfo logFileContent = new LoadingInfo(fsManager.resolveFile(HTTP_ZIPPED))) {
      assertEquals(CompressType.ZIP, logFileContent.getCompression());
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
