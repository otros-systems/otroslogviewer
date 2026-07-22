package pl.otros.logview.dist;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.testng.AssertJUnit.assertFalse;

public class FileNewLineEncodingTest {
  @Test
  public void testShellScriptUnixNewLineEncoding() throws IOException {
    assertFalse("The file olv.sh contains windows new line ('\\r'). Remove it or you cannot use this script with linux!", readFile("olv.sh").contains("\r"));
    assertFalse("The file olv-batch.sh contains windows new line ('\\r'). Remove it or you cannot use this script with linux!", readFile("olv-batch.sh").contains("\r"));
  }

  private String readFile(String file) throws IOException {
    return java.nio.file.Files.readString(Path.of("src/main/dist/" + file));
  }
}
