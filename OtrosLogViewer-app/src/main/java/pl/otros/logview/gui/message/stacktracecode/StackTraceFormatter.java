package pl.otros.logview.gui.message.stacktracecode;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.message.LocationInfo;
import pl.otros.logview.gui.message.MessageFormatter;
import pl.otros.logview.gui.message.StackTraceFinder;
import pl.otros.logview.gui.message.SubText;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeServiceImpl;
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
                System.out.println("Group: " + group);
                sb.append(group);
                final LocationInfo location = LocationInfo.parse(group);
                if (location != null) {
                    try {
                        System.out.println("LI:" + location);
                        String content = jumpToCodeService.getContent(location).replaceAll("\r", "");
                        System.out.println("Content is " + content);
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

    public static void main(String[] args) {

        BaseConfiguration b = new BaseConfiguration();
        b.setProperty(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED, true);
        final JumpToCodeService jumpToCodeService = new JumpToCodeServiceImpl(b);

        String message = "Error occurred when using message colorizer Java stack trace: null\n" +
                "\n" +
                "java.lang.NullPointerException\n" +
                "\tat java.util.Hashtable.put(Hashtable.java:514)\n" +
                "\tat javax.swing.text.SimpleAttributeSet.addAttribute(SimpleAttributeSet.java:193)\n" +
                "\tat javax.swing.text.StyleContext.addAttribute(StyleContext.java:310)\n" +
                "\tat javax.swing.text.StyleContext$NamedStyle.addAttribute(StyleContext.java:1501)\n" +
                "\tat javax.swing.text.StyleConstants.setBackground(StyleConstants.java:585)\n" +
                "\tat pl.otros.logview.gui.message.StackTraceColorizer.initStyles(StackTraceColorizer.java:74)\n" +
                "\tat pl.otros.logview.gui.message.StackTraceColorizer.colorize(StackTraceColorizer.java:85)\n" +
                "\tat pl.otros.logview.gui.message.update.MessageUpdateUtils$2.call(MessageUpdateUtils.java:100)\n" +
                "\tat pl.otros.logview.gui.message.update.MessageUpdateUtils$2.call(MessageUpdateUtils.java:92)\n" +
                "\tat java.util.concurrent.FutureTask.run(FutureTask.java:262)\n" +
                "\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)\n" +
                "\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)\n" +
                "\tat java.lang.Thread.run(Thread.java:745)\n" +
                "\n" +
                "java.lang.NullPointerException\n" +
                "\tat java.util.Hashtable.put(Hashtable.java:514)\n" +
                "\tat javax.swing.text.SimpleAttributeSet.addAttribute(SimpleAttributeSet.java:193)\n" +
                "\tat javax.swing.text.StyleContext.addAttribute(StyleContext.java:310)\n" +
                "\tat javax.swing.text.StyleContext$NamedStyle.addAttribute(StyleContext.java:1501)\n" +
                "\tat javax.swing.text.StyleConstants.setBackground(StyleConstants.java:585)\n" +
                "\tat pl.otros.logview.gui.message.StackTraceColorizer.initStyles(StackTraceColorizer.java:74)\n" +
                "\tat pl.otros.logview.gui.message.StackTraceColorizer.colorize(StackTraceColorizer.java:85)\n" +
                "\tat pl.otros.logview.gui.message.update.MessageUpdateUtils$2.call(MessageUpdateUtils.java:100)\n" +
                "\tat pl.otros.logview.gui.message.update.MessageUpdateUtils$2.call(MessageUpdateUtils.java:92)\n" +
                "\tat java.util.concurrent.FutureTask.run(FutureTask.java:262)\n" +
                "\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)\n" +
                "\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)\n" +
                "\tat java.lang.Thread.run(Thread.java:745)\n";

        final long start = System.currentTimeMillis();
        final StackTraceFormatter formtterPlugin = new StackTraceFormatter(jumpToCodeService);
        formtterPlugin.format(message);
        System.out.println("\nFormatted: " + formtterPlugin.format(message));
        System.out.println("Formatting took "+ (System.currentTimeMillis()-start + "ms"));

    }
}
