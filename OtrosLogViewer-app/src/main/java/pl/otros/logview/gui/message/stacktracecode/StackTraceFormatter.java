package pl.otros.logview.gui.message.stacktracecode;

import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.gui.message.LocationInfo;
import pl.otros.logview.gui.message.MessageFormatter;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

public class StackTraceFormatter extends AbstractPluginableElement implements MessageFormatter {

  private static final Logger LOGGER = LoggerFactory.getLogger(StackTraceFormatter.class.getName());
  public static final String STACK_TRACE_REGEX = "(\\s*at\\s+([\\w\\d\\.]*)\\.([\\w\\d\\$]+)\\.([\\d\\w<>\\$]+)\\(([\\d\\w\\.\\u0020:]+)\\))";
  private final JumpToCodeService jumpToCodeService;

  public StackTraceFormatter(JumpToCodeService jumpToCodeService) {
    super("Stacktrace with message", "Add code fragment to stacktrace");
    this.jumpToCodeService = jumpToCodeService;
  }


  @Override
  public int getApiVersion() {
    return MESSAGE_FORMATTER_VERSION_1;
  }

  @Override
  public boolean formattingNeeded(String message) {
    return Pattern.compile(STACK_TRACE_REGEX).matcher(message).find();
  }

  @Override
  public String format(String message) {
    StringBuilder sb = new StringBuilder(message.length());
    final Iterable<String> split = Splitter.on("\n").split(message);
    for (String line : split) {
      sb.append(line);
      if (Pattern.compile(STACK_TRACE_REGEX).matcher(line).find()) {
        addCodeToLine(sb, line);
      }
      sb.append("\n");
    }
    return sb.toString().trim();
  }

  private void addCodeToLine(StringBuilder sb, String line) {
    final LocationInfo location = LocationInfo.parse(line);
    if (location != null) {
      try {
        final String finalContent = jumpToCodeService.getContent(location).replaceAll("\r", "");
        final Optional<String> oneLine = location.getLineNumber().flatMap(ln ->
          Splitter.on('\n').splitToList(finalContent)
            .stream()
            .filter(t -> t.startsWith(Integer.toString(ln)))
            .map(s -> "\t //" + s.replaceFirst("[\\d\\s]]+:", "").trim())
            .findFirst()

        );
        sb.append(oneLine);
      } catch (IOException e) {
        //ignore. if code fragment cant be loaded, just don't display it
      }
    }
  }

}
