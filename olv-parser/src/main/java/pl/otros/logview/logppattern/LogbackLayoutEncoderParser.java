package pl.otros.logview.logppattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

sealed interface LoggerAst permits LiteralAst, ConversionWord {
}

record LiteralAst(String value) implements LoggerAst {
}

record ConversionWord(String word, String args) implements LoggerAst {
}

sealed interface Token permits
    StringLiteral, DateToken, MDC, NotSupportedToken,
    LoggerClass, ContextName, FileToken, Caller,
    Line, Message, Method, NewLine, Level, Relative,
    ThreadToken, ExceptionToken, NoException, Marker,
    Replace, RootException {

    String pattern();
}

// ───── Records (case classes) ─────

record StringLiteral(String literal) implements Token {
    @Override
    public String pattern() {
        return literal;
    }
}

record DateToken(String datePattern) implements Token {
    @Override
    public String pattern() {
        return "TIMESTAMP";
    }

    public String olvDateFormat() {
        return datePattern.isEmpty() || datePattern.equals("ISO8601")
            ? "yyyy-MM-dd HH:mm:ss,SSS"
            : datePattern;
    }
}

record MDC(String mdc) implements Token {
    @Override
    public String pattern() {
        String[] elements = mdc.split(",");
        if (elements.length == 1) {
            return "PROP(" + elements[0].trim() + ")";
        } else {
            return Arrays.stream(elements)
                .map(String::trim)
                .map(e -> e + "=PROP(" + e + ")")
                .collect(Collectors.joining(" "));
        }
    }

    public boolean multiProperty() {
        return mdc.contains(",");
    }
}

record NotSupportedToken(String token) implements Token {
    @Override
    public String pattern() {
        return "";
    }
}

// ───── Enums (case objects) ─────

enum LoggerClass implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "CLASS";
    }
}

enum ContextName implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "PROP(contextName)";
    }
}

enum FileToken implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "FILE";
    }
}

enum Caller implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "";
    }
}

enum Line implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "LINE";
    }
}

enum Message implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "MESSAGE";
    }
}

enum Method implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "METHOD";
    }
}

enum NewLine implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "";
    }
}

enum Level implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "LEVEL";
    }
}

enum Relative implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "RELATIVE";
    }
}

enum ThreadToken implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "THREAD";
    }
}

enum ExceptionToken implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "";
    }
}

enum NoException implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "";
    }
}

enum Marker implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "PROP(MARKER)";
    }
}

enum Replace implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "";
    }
}

enum RootException implements Token {
    INSTANCE;

    @Override
    public String pattern() {
        return "";
    }
}


public class LogbackLayoutEncoderParser {

    private static final Pattern CONVERSION_WITH_ARGS = Pattern.compile("%(?:-?\\d+)?(?:\\.\\d+)?([a-zA-Z]+)\\{([^}]+)}");
    private static final Pattern CONVERSION_SIMPLE = Pattern.compile("%(?:-?\\d+)?(?:\\.\\d+)?([a-zA-Z]+)");
    private static final Pattern LITERAL = Pattern.compile("[^%]+");

    public static void main(String[] args) {
        new LogbackLayoutEncoderParser()
            .parseToLoggerAst("%level %d{HH:mm:ss.SSS} %thread %logger %msg%n")
            .forEach(System.out::println);
    }

    public Properties convert(String layoutPattern) throws Exception {
        List<LoggerAst> loggerAst = parseToLoggerAst(layoutPattern);
        List<Token> tokens = loggerAstToTokens(loggerAst);
        tokens = filterExceptionsBeforeMessage(tokens);
        new Validator().validate(tokens);
        Properties props = new Properties();
        var dateFormat = tokens
            .stream()
            .filter(token -> token instanceof DateToken)
            .map(token -> ((DateToken) token).olvDateFormat())
            .findFirst().orElse("");
        props.put("type", "log4j");
        props.put("pattern", tokens.stream().map(Token::pattern).collect(Collectors.joining("")).replaceAll("\\s+$", ""));
        props.put("dateFormat", dateFormat);
        props.put("name", "");
        props.put("charset", "UTF-8");
        return props;
    }

    List<Token> filterExceptionsBeforeMessage(List<Token> tokens) {
        List<Token> filtered = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token current = tokens.get(i);

            // Check for ExceptionToken or RootException
            if (isTokenToFilterOut(current)
                && i + 2 < tokens.size()
                && tokens.get(i + 1) instanceof StringLiteral
                && tokens.get(i + 2) instanceof Message) {

                // Skip current and the StringLiteral
                i += 1; // skip next too
                continue;
            }
            filtered.add(current);
        }

        return filtered;
    }

    private boolean isTokenToFilterOut(Token token) {
        return token instanceof ExceptionToken ||
            token instanceof RootException ||
            token instanceof NoException ||
            token instanceof Caller;
    }

    List<LoggerAst> parseToLoggerAst(String input) {
        List<LoggerAst> result = new ArrayList<>();
        int pos = 0;

        while (pos < input.length()) {
            String remaining = input.substring(pos);

            Matcher withArgs = CONVERSION_WITH_ARGS.matcher(remaining);
            Matcher simple = CONVERSION_SIMPLE.matcher(remaining);
            Matcher literal = LITERAL.matcher(remaining);

            if (withArgs.lookingAt()) {
                result.add(new ConversionWord(withArgs.group(1), withArgs.group(2)));
                pos += withArgs.end();
            } else if (simple.lookingAt()) {
                result.add(new ConversionWord(simple.group(1), ""));
                pos += simple.end();
            } else if (literal.lookingAt()) {
                result.add(new LiteralAst(literal.group()));
                pos += literal.end();
            } else {
                throw new IllegalArgumentException("Unexpected token at position " + pos);
            }
        }

        return result;
    }

    List<Token> loggerAstToTokens(List<LoggerAst> input) {
        return input.stream().map(this::toToken).collect(Collectors.toList());
    }

    private Token toToken(LoggerAst loggerAst) {
        return switch (loggerAst) {
            case LiteralAst(String str) -> new StringLiteral(str);
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("c") -> LoggerClass.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("class") -> LoggerClass.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("lo") -> LoggerClass.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("logger") -> LoggerClass.INSTANCE;

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("contextName") ->
                ContextName.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("cx") -> ContextName.INSTANCE;

            case ConversionWord(String w, String format) when w.equalsIgnoreCase("d") -> new DateToken(format);
            case ConversionWord(String w, String format) when w.equalsIgnoreCase("date") -> new DateToken(format);

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("F") -> FileToken.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("file") -> FileToken.INSTANCE;

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("caller") -> Caller.INSTANCE;

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("l") -> Line.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("line") -> Line.INSTANCE;

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("m") -> Message.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("msg") -> Message.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("message") -> Message.INSTANCE;

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("n") -> NewLine.INSTANCE;

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("p") -> Level.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("priority") -> Level.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("l") -> Level.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("level") -> Level.INSTANCE;

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("r") -> Relative.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("relative") -> Relative.INSTANCE;

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("t") -> ThreadToken.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("thread") -> ThreadToken.INSTANCE;

            case ConversionWord(String w, String key) when w.equalsIgnoreCase("X") -> new MDC(key);
            case ConversionWord(String w, String key) when w.equalsIgnoreCase("mdc") -> new MDC(key);

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("ex") -> ExceptionToken.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("ex") -> ExceptionToken.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("exception") ->
                ExceptionToken.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("throwable") ->
                ExceptionToken.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("xEx") -> ExceptionToken.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("xException") ->
                ExceptionToken.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("xThrowable") ->
                ExceptionToken.INSTANCE;

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("nopex") -> ThreadToken.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("nopexception") ->
                ThreadToken.INSTANCE;

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("marker") -> Marker.INSTANCE;

            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("rEx") -> RootException.INSTANCE;
            case ConversionWord(String w, String ignored) when w.equalsIgnoreCase("rootException") ->
                RootException.INSTANCE;

            case ConversionWord(String w, String ignored) -> new NotSupportedToken(w);
        };
    }

}
