package pl.otros.logview.api.io;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class UtilsTest {

  @Test
  public void getFileObjectShortNameIp() throws Exception {
    String scheme = "sftp";
    String url = "sftp://10.0.22.3/logs/out.log";
    String baseName = "out.log";
    String output = "sftp://10.0.22.3/out.log";

    testGetObjectShortName(scheme, url, baseName, output);
  }

  @Test
  public void getFileObjectShortNameLongHost() throws Exception {
    String scheme = "sftp";
    String url = "sftp://machine.a.b.com/logs/out.log";
    String baseName = "out.log";
    String output = "sftp://machine/out.log";

    testGetObjectShortName(scheme, url, baseName, output);
  }

  @Test
  public void getFileObjectShortNameShortHost() throws Exception {
    String scheme = "sftp";
    String url = "sftp://machine/logs/out.log";
    String baseName = "out.log";
    String output = "sftp://machine/out.log";

    testGetObjectShortName(scheme, url, baseName, output);
  }

  @Test
  public void getFileObjectShortNameLocalFile() throws Exception {
    String scheme = "file";
    String url = "file://opt/logs/out.log";
    String baseName = "out.log";
    String output = "file://out.log";

    testGetObjectShortName(scheme, url, baseName, output);
  }

  private void testGetObjectShortName(String scheme, String url, String baseName, String output) {
    // given
    FileObject fileObjectMock = mock(FileObject.class);
    FileName fileNameMock = mock(FileName.class);

    when(fileObjectMock.getName()).thenReturn(fileNameMock);
    when(fileNameMock.getScheme()).thenReturn(scheme);
    when(fileNameMock.getURI()).thenReturn(url);
    when(fileNameMock.getBaseName()).thenReturn(baseName);

    // when
    String fileObjectShortName = Utils.getFileObjectShortName(fileObjectMock);

    // then
    AssertJUnit.assertEquals(output, fileObjectShortName);
  }
}
