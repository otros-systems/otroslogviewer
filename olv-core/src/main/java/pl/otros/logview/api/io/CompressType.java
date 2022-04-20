package pl.otros.logview.api.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public enum CompressType {
  NONE(false),
  GZIP(true),
  ZIP(true);

  private final boolean compressed;

  private CompressType(boolean compressed) {
    this.compressed = compressed;
  }

  public boolean isCompressed() {
    return compressed;
  }

  public InputStream createInputStream(InputStream inputStream) throws IOException {
    if (this == CompressType.GZIP) {
      return new GZIPInputStream(inputStream);
    } else if (this == CompressType.ZIP) {
      ZipInputStream zipInputStream = new ZipInputStream(inputStream);
      zipInputStream.getNextEntry();
      return zipInputStream;
    }
    return inputStream;
  }
}
