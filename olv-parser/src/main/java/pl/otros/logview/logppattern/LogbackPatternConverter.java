package pl.otros.logview.logppattern;

import java.util.*;
import java.util.regex.*;

public class LogbackPatternConverter {

    // https://logback.qos.ch/manual/layouts.html#ClassicPatternLayout
    public static Map<String, String> convert(String pattern) throws Exception {

        // 1. Parse format to abstract
        List<LoggerAst> parsed = LogPatternParser.apply(pattern);

        // 2. Convert to logback definition
        List<LoggerAst> logbackFormat = logbackFormat(parsed);

        // 2a. Convert "EXCEPTION MESSAGE" to "MESSAGE"
        logbackFormat = mergeSomeConversionWordBeforeMessage(logbackFormat);

        // 3. Validation
        validate(logbackFormat);

        // 4. Convert to Log4jPattern
        return toLog4jPatternProperties(logbackFormat);
    }

    // Placeholder for your actual `logbackFormat`, `mergeSomeConversionWordBeforeMessage`, `validate`, and `toLog4jPatternProperties`
    private static List<LoggerAst> logbackFormat(List<LoggerAst> logbackFormat) {
        return logbackFormat;
    }

    private static List<LoggerAst> mergeSomeConversionWordBeforeMessage(List<LoggerAst> logbackFormat) {
        return logbackFormat;
    }

    private static void validate(List<LoggerAst> logbackFormat) throws Exception {
        // Add validation logic here, if necessary.
    }

    private static Map<String, String> toLog4jPatternProperties(List<LoggerAst> logbackFormat) {
        return new HashMap<>();
    }

}

class LogPatternParser {

    // Apply method to parse pattern string
    public static List<LoggerAst> apply(String pattern) throws Exception {
        List<LoggerAst> result = new ArrayList<>();
        Pattern tokenPattern = Pattern.compile("%?(-?\\d+)?\\.?(\\d+)?[a-zA-Z]+|[^%]+");
        Matcher matcher = tokenPattern.matcher(pattern);

        while (matcher.find()) {
            String token = matcher.group();
            if (token.startsWith("%")) {
                // If it's a conversion word, process it accordingly
                result.add(new ConversionWord(token, ""));
            } else {
                // If it's a literal string, add it as a literal ast
                result.add(new LiteralAst(token));
            }
        }

        if (result.isEmpty()) {
            throw new Exception("Invalid pattern format: " + pattern);
        }

        return result;
    }
}

class LoggerAst {}

class ConversionWord extends LoggerAst {
    String word;
    String args;

    public ConversionWord(String word, String args) {
        this.word = word;
        this.args = args;
    }
}

class LiteralAst extends LoggerAst {
    String value;

    public LiteralAst(String value) {
        this.value = value;
    }
}

