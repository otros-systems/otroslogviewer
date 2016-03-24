package pl.otros.logview.gui.message.stacktracecode;

import com.google.common.base.Splitter;
import pl.otros.logview.api.LocationInfo;
import pl.otros.logview.api.MessageFormatter;
import pl.otros.logview.api.services.JumpToCodeService;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import java.util.Optional;
import java.util.regex.Pattern;

public class StackTraceFormatter extends AbstractPluginableElement implements MessageFormatter {

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
      sb.append(line.replaceFirst("\\s+at","  at"));
      if (Pattern.compile(STACK_TRACE_REGEX).matcher(line).find()) {
        addCodeToLine(sb, line);
      }
      sb.append("\n");
    }
    return sb.toString().trim();
  }

  private void addCodeToLine(StringBuilder sb, String line) {
    final Optional<LocationInfo> location = Optional
      .ofNullable(line)
      .map(LocationInfo::parse);

    final Optional<String> content = location
      .flatMap(jumpToCodeService::getContentOptional)
      .map(s -> s.replaceAll("\r", ""));

    if (content.isPresent()) {
      final String finalContent = content.get();
      location
        .flatMap(LocationInfo::getLineNumber)
        .flatMap(ln ->
          Splitter.on('\n').splitToList(finalContent)
            .stream()
            .filter(t -> t.startsWith(Integer.toString(ln)))
            .map(s -> "\t //" + s.replaceFirst("[\\d\\s]+:\\s*", " ").trim())
            .findFirst()
        ).ifPresent(sb::append);

    }

  }

}
