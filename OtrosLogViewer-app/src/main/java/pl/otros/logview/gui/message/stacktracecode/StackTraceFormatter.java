package pl.otros.logview.gui.message.stacktracecode;

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.gui.message.LocationInfo;
import pl.otros.logview.gui.message.MessageFormatter;
import pl.otros.logview.gui.message.StackTraceFinder;
import pl.otros.logview.gui.message.SubText;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import java.io.IOException;
import java.util.SortedSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackTraceFormatter extends AbstractPluginableElement implements MessageFormatter {

    private static final Logger LOGGER = Logger.getLogger(StackTraceFormatter.class.getName());
    protected static final Pattern exceptionLine = Pattern.compile("(\\s*at\\s+([\\w\\d\\.]*)\\.([\\w\\d\\$]+)\\.([\\d\\w<>]+)\\(([\\d\\w\\.\\u0020:]+)\\))");
    private JumpToCodeService jumpToCodeService;

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
        return exceptionLine.matcher(message).find();
    }

    @Override
    public String format(String message) {
        return colorize(message);
    }


    public String colorize(String message) {

        StackTraceFinder stackTraceFinder = new StackTraceFinder();
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;
        SortedSet<SubText> foundStackTraces = stackTraceFinder.findStackTraces(message);
        sb.append(message.substring(0, foundStackTraces.size() > 0 ? foundStackTraces.iterator().next().getStart() : 0));

        for (SubText subText : foundStackTraces) {

            String subTextFragment = message.substring(subText.getStart(), subText.getEnd());
            Matcher matcher = exceptionLine.matcher(subTextFragment);

            while (matcher.find()) {
                final String group = matcher.group(1);
                sb.append(group);
                final LocationInfo location = LocationInfo.parse(group);
                if (location != null) {
                    try {
                        String content = jumpToCodeService.getContent(location).replaceAll("\r", "");
                        if (StringUtils.isNotBlank(content)){
                            final String begin = "\n" + location.getLineNumber() + ":";
                            content = content.substring(content.indexOf(begin)+begin.length());
                            content = content.split("\n", 2)[0];
                            sb.append("\t //").append(content.trim());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            lastEnd = subText.getEnd();
        }
        sb.append(message.substring(lastEnd));
        return sb.toString();
    }

}
