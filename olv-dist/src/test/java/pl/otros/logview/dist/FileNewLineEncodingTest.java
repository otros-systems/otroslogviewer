package pl.otros.logview.dist;

import org.testng.annotations.Test;
import org.testng.reporters.Files;

import java.io.File;
import java.io.IOException;

import static org.testng.AssertJUnit.assertFalse;

public class FileNewLineEncodingTest {
  @Test
  public void testShellScriptUnixNewLineEncoding() throws IOException {
    assertFalse("The file olv.sh contains widows new line ('\\r'). Remove it or you cannot use this script with linux!", readFile("olv.sh").contains("\r"));
    assertFalse("The file olv-batch.sh contains widows new line ('\\r'). Remove it or you cannot use this script with linux!", readFile("olv-batch.sh").contains("\r"));
  }

  private String readFile(String file) throws IOException {
    return Files.readFile(new File("src/main/dist/" + file));
  }
}
