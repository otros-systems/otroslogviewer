package pl.otros.logview.gui.message.json;

import pl.otros.logview.gui.message.SubText;

import java.util.ArrayList;
import java.util.List;

public class JsonFinder {

    public ArrayList<SubText> findJsonFragments(String string) {
        final List<Integer> ints = countParenthesis(string);
        return getSubTexts(ints);
    }

    protected ArrayList<SubText> getSubTexts(List<Integer> ints) {
        final ArrayList<SubText> subTexts = new ArrayList<SubText>();
        int startPosition = 0;
        int lastValue = 0;
        for (int i = 0; i < ints.size(); i++) {
            final int currentValue = ints.get(i);
            if (currentValue == 0 && lastValue > 0) {
                subTexts.add(new SubText(startPosition, i+1));
            } else if (currentValue > 0 && lastValue == 0) {
                startPosition = i;
            }
            lastValue = currentValue;
        }
        return subTexts;
    }

    protected List<Integer> countParenthesis(String string) {
        final List<Integer> openParenthesis = new ArrayList<Integer>(string.length());
        int currentValue = 0;
        boolean escaped = false;
        boolean inString = false;
        for (int i = 0; i < string.length(); i++) {
            //watch for '{}' in strings value
            final char c = string.charAt(i);
            if (c == '{' && !inString) {
                currentValue++;
            } else if (c == '}' && !inString) {
                currentValue--;
            } else if (!escaped && c == '"') {
                inString = !inString;
            } else {
                escaped = c == '\\' && !escaped;
            }
            openParenthesis.add(currentValue);
        }
        return openParenthesis;
    }
}
