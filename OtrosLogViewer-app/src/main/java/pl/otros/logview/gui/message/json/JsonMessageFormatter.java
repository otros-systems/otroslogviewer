package pl.otros.logview.gui.message.json;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.pluginable.MessageFormatter;
import pl.otros.logview.gui.message.SubText;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import java.util.ArrayList;

public class JsonMessageFormatter extends AbstractPluginableElement implements MessageFormatter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonMessageFormatter.class.getName());
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
      String toAppend = group;
      try {
        JSONObject o = new JSONObject(group);
        final String jsonFormatted = o.toString(2);
        toAppend = jsonFormatted;
      } catch (JSONException e) {
        LOGGER.debug("There is no need to format {}",group);
      }
      if (!sb.toString().endsWith("\n")) {
        sb.append("\n");
      }
      sb.append(toAppend).append("\n");
      lastEnd = jsonFragment.getEnd();
    }
    sb.append(message.substring(lastEnd));
    return sb.toString().trim();
  }
}


