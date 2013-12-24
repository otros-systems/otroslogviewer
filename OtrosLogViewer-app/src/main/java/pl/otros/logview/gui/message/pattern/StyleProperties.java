/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.gui.message.pattern;

import org.apache.commons.configuration.DataConfiguration;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class StyleProperties {

  public static final String PROP_FONT_FAMILY = "font.family";
  public static final String PROP_FONT_SIZE = "font.size";
  public static final String PROP_FONT_BOLD = "font.bold";
  public static final String PROP_FONT_ITALIC = "font.italic";
  public static final String PROP_FONT_UNDERLINE = "font.underline";
  public static final String PROP_BACKGROUND = "background";
  public static final String PROP_FOREGROUND = "foreground";

  public static boolean isStyleForGroupDeclared(int group, DataConfiguration styleConfig) {
    String groupSuffix = "." + group;
    if (group <= 0) {
      groupSuffix = "";
    }
    StringBuilder sb = new StringBuilder();
    sb.append(styleConfig.getString(PROP_FONT_FAMILY + groupSuffix, ""));
    sb.append(styleConfig.getString(PROP_FONT_SIZE + groupSuffix, ""));
    sb.append(styleConfig.getString(PROP_FONT_BOLD + groupSuffix, ""));
    sb.append(styleConfig.getString(PROP_FONT_ITALIC + groupSuffix, ""));
    sb.append(styleConfig.getString(PROP_FONT_UNDERLINE + groupSuffix, ""));
    sb.append(styleConfig.getString(PROP_BACKGROUND + groupSuffix, ""));
    sb.append(styleConfig.getString(PROP_FOREGROUND + groupSuffix, ""));

    return sb.toString().trim().length() > 0;

  }

  public static Style getStyle(StyleContext styleContext, DataConfiguration styleConfig, String styleName) {
    return getStyle(styleContext, styleConfig, styleName, -1);
  }

  public static Style getStyle(StyleContext styleContext, DataConfiguration styleConfig, String styleName, int group) {
    Style style = styleContext.addStyle(styleName, styleContext.getStyle(StyleContext.DEFAULT_STYLE));

    String groupSuffix = "." + group;
    if (group <= 0) {
      groupSuffix = "";
    }

    String fontFamily = styleConfig.getString(PROP_FONT_FAMILY + groupSuffix, "");
    if (fontFamily.trim().length() > 0) {
      StyleConstants.setFontFamily(style, styleConfig.getString(PROP_FONT_FAMILY + groupSuffix));
    }

    if (styleConfig.getString(PROP_FONT_SIZE + groupSuffix, "").trim().length() > 0) {
      StyleConstants.setFontSize(style, styleConfig.getInt(PROP_FONT_SIZE + groupSuffix));
    }

    if (styleConfig.getString(PROP_FONT_BOLD + groupSuffix, "").trim().length() > 0) {
      StyleConstants.setBold(style, styleConfig.getBoolean(PROP_FONT_BOLD + groupSuffix));
    }

    if (styleConfig.getString(PROP_FONT_ITALIC + groupSuffix, "").trim().length() > 0) {
      StyleConstants.setItalic(style, styleConfig.getBoolean(PROP_FONT_ITALIC + groupSuffix));
    }

    if (styleConfig.getString(PROP_FONT_UNDERLINE + groupSuffix, "").trim().length() > 0) {
      StyleConstants.setUnderline(style, styleConfig.getBoolean(PROP_FONT_UNDERLINE + groupSuffix));
    }

    if (styleConfig.getString(PROP_BACKGROUND + groupSuffix, "").trim().length() > 0) {
      StyleConstants.setBackground(style, styleConfig.getColor(PROP_BACKGROUND + groupSuffix));
    }

    if (styleConfig.getString(PROP_FOREGROUND + groupSuffix, "").trim().length() > 0) {
      StyleConstants.setForeground(style, styleConfig.getColor(PROP_FOREGROUND + groupSuffix));
    }
    return style;
  }

}
