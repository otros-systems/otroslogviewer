package pl.otros.logview.logppattern;

import pl.otros.logview.api.LayoutEncoderConverter;

import java.util.Properties;

public class LogbackLayoutEncoderConverter implements LayoutEncoderConverter {

    @Override
    public Properties convert(String layoutPattern) throws Exception {
        return new LogbackLayoutEncoderParser().convert(layoutPattern);
    }
}
