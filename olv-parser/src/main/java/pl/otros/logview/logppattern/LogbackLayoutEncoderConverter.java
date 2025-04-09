package pl.otros.logview.logppattern;

import pl.otros.logview.api.LayoutEncoderConverter;

import java.util.Map;
import java.util.Properties;

public class LogbackLayoutEncoderConverter extends LayoutEncoderConverter {
    @Override
    public Properties convert(String layoutPattern) throws Exception {
        Map<String, String> convert = LogbackPatternConverter.convert(layoutPattern);
        Properties p = new Properties();
        p.putAll(convert);
        return p;
    }
}
