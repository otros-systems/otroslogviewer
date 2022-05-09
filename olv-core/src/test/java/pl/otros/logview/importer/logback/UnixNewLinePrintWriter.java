package pl.otros.logview.importer.logback;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * The java PrintWriter prints a new line with the system line separator.
 * With java 17 you can not set line.separator before {@link System#lineSeparator()} is initialized.
 * So this implementation writes new lines every time in unix style "\n".
 */
public class UnixNewLinePrintWriter extends PrintWriter {
  public UnixNewLinePrintWriter(StringWriter stringWriter) {
    super(stringWriter);
  }

  @Override
  public void println() {
    write("\n");
  }
}
