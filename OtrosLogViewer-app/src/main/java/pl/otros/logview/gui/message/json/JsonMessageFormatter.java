package pl.otros.logview.gui.message.json;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import pl.otros.logview.gui.message.MessageFormatter;
import pl.otros.logview.gui.message.SubText;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import java.util.ArrayList;

public class JsonMessageFormatter extends AbstractPluginableElement implements MessageFormatter {


    public JsonMessageFormatter() {
        super("Json formatter", "Formats json in message");
    }

    public JsonFinder jsonFinder = new JsonFinder();

    @Override
    public int getApiVersion() {
        return MESSAGE_FORMATTER_VERSION_1;
    }

    @Override
    public boolean formattingNeeded(String message) {
        return jsonFinder.findJsonFragments(message).size() > 0;
    }

    @Override
    public String format(String message) {
        final ArrayList<SubText> jsonFragments = jsonFinder.findJsonFragments(message);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;
        for (SubText jsonFragment : jsonFragments) {

            sb.append(message.substring(lastEnd, jsonFragment.getStart()));
            final String group = jsonFragment.subString(message);
            try {
                JSONObject o = new JSONObject(group);
                if (!sb.toString().endsWith("\n")){
                    sb.append("\n");
                }
                sb.append(o.toString(2))
                        .append("\n");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            lastEnd = jsonFragment.getEnd();
        }
        sb.append(message.substring(lastEnd));
        return sb.toString().trim();
    }

}
