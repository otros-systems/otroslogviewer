package pl.otros.logview.api.theme;

import java.awt.*;

public class ThemeKey {

  public static final ThemeKey SEARCH_RESULT = new ThemeKey(
    "Search result",
    "theme.logDetails.search",
    Color.YELLOW,
    new Color(0x99000)
  );

  public static final ThemeKey LOG_DETAILS_PROPERTY = new ThemeKey(
    "Log details -> property",
    "theme.logDetails.property",
    new Color(0x006600),
    new Color(0xFFCC99));

  public static final ThemeKey LOG_DETAILS_VALUE = new ThemeKey(
    "Log details -> value",
    "theme.logDetails.value",
    new Color(0x660000),
    new Color(0x99FFFF));

  public static final ThemeKey LOG_DETAILS_DEFAULT = new ThemeKey(
    "Log details -> default",
    "theme.logDetails.default",
    Color.BLACK,
    Color.WHITE);

  public static final ThemeKey LOG_DETAILS_MESSAGE = new ThemeKey(
    "Log details -> message",
    "theme.logDetails.message",
    Color.BLACK,
    new Color(250, 250, 250));

  public static final ThemeKey LOG_DETAILS_PROPERTY_KEY = new ThemeKey(
    "Log details -> property key",
    "theme.logDetails.property.key",
    new Color(0x006600),
    new Color(0xC8782E));


  public static final ThemeKey LOG_DETAILS_PROPERTY_VALUE = new ThemeKey(
    "Log details -> property value",
    "theme.logDetails.property.value",
    Color.BLUE,
    new Color(0x69CE62));


  public static final ThemeKey LOG_DETAILS_STACKTRACE_BACKGROUND = new ThemeKey(
    "Stacktrace -> background",
    "theme.logDetails.stacktrace.background",
    new Color(255, 224, 193),
    Color.BLACK);

  public static final ThemeKey LOG_DETAILS_STACKTRACE_FOREGROUND = new ThemeKey(
    "Stacktrace -> foreground",
    "theme.logDetails.stacktrace.foreground",
    Color.BLACK,
    new Color(0xCCCCCC));

  public static final ThemeKey LOG_DETAILS_STACKTRACE_CLASS = new ThemeKey(
    "Stacktrace -> class",
    "theme.logDetails.stacktrace.class",
    new Color(11, 143, 61),
    new Color( 0xFF6666));

  public static final ThemeKey LOG_DETAILS_STACKTRACE_METHOD = new ThemeKey(
    "Stacktrace -> method",
    "theme.logDetails.stacktrace.method",
    new Color(83, 112, 223),
    Color.RED);

  public static final ThemeKey LOG_DETAILS_STACKTRACE_FLE = new ThemeKey(
    "Stacktrace -> file",
    "theme.logDetails.stacktrace.file",
    Color.BLACK,
    new Color(0x6666FF));

  public static final ThemeKey LOG_DETAILS_STACKTRACE_COMMENT = new ThemeKey(
    "Stacktrace -> comment",
    "theme.logDetails.stacktrace.comment",
    Color.DARK_GRAY,
    new Color(0x99FFFF));

  public static final ThemeKey LOG_DETAILS_SOAP_ELEMENT_NAME = new ThemeKey(
    "SOAP -> element name",
    "theme.logDetails.soap.elementName",
    new Color(128, 0, 0),
    new Color(0xCC7832)
  );

  public static final ThemeKey LOG_DETAILS_SOAP_ATTRIBUTE_NAME = new ThemeKey(
    "SOAP -> attribute name",
    "theme.logDetails.soap.attribute.name",
    Color.RED,
    new Color(0x4889FF)
  );

  public static final ThemeKey LOG_DETAILS_SOAP_ATTRIBUTE_VALUE = new ThemeKey(
    "SOAP -> attribute value",
    "theme.logDetails.soap.attribute.value",
    Color.BLACK,
    new Color(0x33CC00)
  );

  public static final ThemeKey LOG_DETAILS_SOAP_CONTENT_BACKGROUND = new ThemeKey(
    "SOAP -> content background",
    "theme.logDetails.soap.content.background",
    Color.WHITE,
    Color.BLACK
  );

  public static final ThemeKey LOG_DETAILS_SOAP_CONTENT_FOREGROUND = new ThemeKey(
    "SOAP -> content foreground",
    "theme.logDetails.soap.content.foreground",
    Color.BLACK,
    Color.WHITE
  );

  public static final ThemeKey LOG_DETAILS_SOAP_OPERATOR = new ThemeKey(
    "SOAP -> operator",
    "theme.logDetails.soap.operator",
    Color.BLUE,
    new Color(0xA9B7C6)
  );

  public static final ThemeKey LOG_DETAILS_SOAP_COMMENTS = new ThemeKey(
    "SOAP -> comments",
    "theme.logDetails.soap.comments",
    new Color(128, 128, 128),
    new Color(0x808080)
  );

  public static final ThemeKey LOG_DETAILS_SOAP_CDATA_BACKGROUND = new ThemeKey(
    "SOAP -> CDATA background",
    "theme.logDetails.soap.cdata.background",
    new Color(220, 220, 255),
    new Color(30, 30, 0)
  );

  public static final ThemeKey LOG_DETAILS_SOAP_CDATA_FOREGROUND = new ThemeKey(
    "SOAP -> CDATA foreground",
    "theme.logDetails.soap.cdata.foreground",
    new Color(30, 30, 0),
    new Color(220, 220, 255)
  );

  private String name;
  private String baseKey;
  private Color defaultLightColor;
  private Color defaultDarkColor;

  public ThemeKey(String name, String baseKey,
                  Color defaultLightColor,
                  Color defaultDarkColor) {
    this.name = name;
    this.baseKey = baseKey;
    this.defaultLightColor = defaultLightColor;
    this.defaultDarkColor = defaultDarkColor;
  }

  public String getName() {
    return name;
  }

  public String getBaseKey() {
    return baseKey;
  }

  public String getKey(Theme.Type type) {
    return baseKey + (type == Theme.Type.Light ? ".light" : ".dark");
  }

  public Color getDefaultLightColor() {
    return defaultLightColor;
  }

  public Color getDefaultDarkColor() {
    return defaultDarkColor;
  }
}
