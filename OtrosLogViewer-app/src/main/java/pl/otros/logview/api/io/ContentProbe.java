package pl.otros.logview.api.io;

public class ContentProbe {
  private final byte[] bytes;

  public ContentProbe(byte[] bytes) {
    this.bytes = bytes;
  }

  public byte[] getBytes() {
    return bytes;
  }
}
