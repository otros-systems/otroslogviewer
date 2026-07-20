package pl.otros.logview.logppattern;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Validator {
    void validate(List<Token> tokens) throws Exception {
        if (messageTokenCount(tokens) > 1) {
            throw new Exception("More than one MESSAGE found");
        } else if (messageTokenCount(tokens) == 0) {
            throw new Exception("No MESSAGE found");
        } else if (tokensNotSeparatedByStringLiteral(tokens)) {
            throw new Exception("Tokens not separated by StringLiteral");
        } else if (!allMergedWithWhiteCharacter(tokens)) {
            throw new Exception("Not all tokens are separated by white space");
        } else if (mdcWithoutProperty(tokens)) {
            throw new Exception("MDC without property");
        } else if (!mdcSurroundedByWhiteSpace(tokens).isEmpty()) {
            throw new Exception("MDC not surrounded by white space");
        } else if (!forbiddenAfterMessage(tokens).isEmpty()) {
            var forbidden = forbiddenAfterMessage(tokens).stream().map(Token::toString).collect(Collectors.joining(", "));
            throw new Exception("Forbidden tokens after MESSAGE: " + forbidden);
        } else if (!notSupportedTokens(tokens).isEmpty()) {
            String notSupported = notSupportedTokens(tokens).stream()
                .map(NotSupportedToken::token)
                .collect(Collectors.joining(", "));
            throw new Exception("Not supported tokens found: " + notSupported) ;
                
        }
    }

    boolean mdcWithoutProperty(List<Token> tokens) {
        var f = tokens.stream()
            .filter(t -> t instanceof MDC)
            .map(t -> (MDC) t)
            .anyMatch(mdc -> mdc.mdc() == null || mdc.mdc().isEmpty());
        return f;
    }

    List<Token> forbiddenAfterMessage(List<Token> tokens) {
        List<Token> afterMessage = tokens.stream()
            .dropWhile(t -> !(t instanceof Message)) // drop until first Message (inclusive)
            .skip(1)                                 // skip the Message itself
            .filter(t -> !(t instanceof NewLine
                || t instanceof StringLiteral
                || t instanceof ExceptionToken
                || t instanceof RootException
                || t instanceof NoException
                || t instanceof Caller))
            .collect(Collectors.toList());
        return afterMessage;
    }

    boolean allMergedWithWhiteCharacter(List<Token> tokens) {
        return tokens.stream()
            .map(t -> t instanceof NewLine ? new StringLiteral("\n") : t) // replace NewLine
            .filter(t -> t instanceof StringLiteral)                      // keep only StringLiteral
            .map(t -> ((StringLiteral) t).literal())                      // extract literal
            .allMatch(s -> s.matches(".*\\s.*"));
    }

    boolean tokensNotSeparatedByStringLiteral(List<Token> tokens) {
        // Step 1: replace NewLine with StringLiteral("\n")
        //List<Token> replaced = tokens.stream()
        //    .map(t -> t instanceof NewLine ? new StringLiteral("\n") : t)
        //    .toList();

        // Step 2: check for adjacent StringLiteral instances
        boolean hasAdjacent = IntStream.range(0, tokens.size() - 1)
            .anyMatch(i ->
                tokens.get(i) instanceof StringLiteral &&
                    tokens.get(i + 1) instanceof StringLiteral
            );

        // If you want the inverse condition (no adjacent StringLiterals):
        return hasAdjacent;
        //return false;
    }

    List<String> mdcSurroundedByWhiteSpace(List<Token> tokens) {
        return IntStream.range(0, tokens.size() - 2)
            .mapToObj(i -> {
                Token t1 = tokens.get(i);
                Token t2 = tokens.get(i + 1);
                Token t3 = tokens.get(i + 2);

                if (t1 instanceof StringLiteral(String literal) &&
                    t2 instanceof MDC mdc &&
                    t3 instanceof StringLiteral &&
                    !mdc.multiProperty() &&
                    !mdc.pattern().trim().isEmpty()) {

                    if (!literal.isEmpty() && Character.isWhitespace(literal.charAt(literal.length() - 1))) {
                        return mdc.pattern();
                    }
                }
                return null;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    int messageTokenCount(List<Token> tokens) {
        return (int) tokens.stream()
            .filter(t -> t instanceof Message)
            .count();
    }

    List<NotSupportedToken> notSupportedTokens(List<Token> tokens) {
        return tokens.stream()
            .filter(t -> t instanceof NotSupportedToken)
            .map(t -> (NotSupportedToken) t)
            .collect(Collectors.toList());
    }
}
