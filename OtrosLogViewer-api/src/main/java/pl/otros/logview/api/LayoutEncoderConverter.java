package pl.otros.logview.api;

import java.util.Properties;

public interface LayoutEncoderConverter {

  Properties convert(String layoutPattern) throws Exception;

}
