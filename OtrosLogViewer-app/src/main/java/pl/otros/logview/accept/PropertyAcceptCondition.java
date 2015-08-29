package pl.otros.logview.accept;

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.LogData;

/**
 * Accept LogData if contains specific property/value
 */
public class PropertyAcceptCondition extends AbstractAcceptContidion {

    private final String propertyKey;
    private final String propertyValue;

    public PropertyAcceptCondition(String propertyKey, String propertyValue) {
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
        name = String.format("Property %s=%s",propertyKey,propertyValue);
        description = String.format("Contains property %s with value %s",propertyKey,propertyValue);
    }

    @Override
    public boolean accept(LogData ld) {
        return ld.getProperties().containsKey(propertyKey) && StringUtils.equals(propertyValue,ld.getProperties().get(propertyKey));
    }
}
