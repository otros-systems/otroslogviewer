/*
 * Copyright 2012 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.logview.gui.message.html;

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringEscapeUtils;
import pl.otros.logview.gui.message.MessageFragmentStyle;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.*;
import java.util.logging.Logger;

public class ExportToHtml {

    private static final Logger LOGGER = Logger.getLogger(ExportToHtml.class.getName());
    public static final String INLINE_HTML_HEADER = "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">";

    public enum HTML_MODE {
        FULL_HTML,
        INLINE_HTML
    }

    public String format(String text, Collection<MessageFragmentStyle> styles, String title, HTML_MODE html_mode) {
        HashMap<String, Map<String, String>> stylesMap = new HashMap<String, Map<String, String>>();
        for (MessageFragmentStyle mfs : styles) {
            Map<String, String> styleMapToCss = styleToCssMap(mfs);
            stylesMap.put(mfs.getStyle().getName(), styleMapToCss);

        }

        StringBuilder sb = new StringBuilder();
        appendHeaders(sb, stylesMap, title, html_mode);
        ArrayList<HtmlSpanFragment> htmlSpanTag = createHtmlSpanTag(styles, html_mode);
        String htmled = addSpanTagsToText(text, htmlSpanTag, html_mode);
        sb.append(htmled);


        appendFooter(sb, html_mode);

        return sb.toString();
    }

    private void appendFooter(StringBuilder sb, HTML_MODE html_mode) {
        if (HTML_MODE.FULL_HTML.equals(html_mode)) {
            sb.append("\n</BODY></HTML>");
        }
    }

    private String addSpanTagsToText(String text, ArrayList<HtmlSpanFragment> htmlSpanTag, HTML_MODE html_mode) {
        StringBuilder sb = new StringBuilder();
        int lastPosition = 0;
        for (HtmlSpanFragment htmlSpanFragment : htmlSpanTag) {
            String substring = text.substring(lastPosition, htmlSpanFragment.getPosition());
            substring = stringToHtml(substring);                    //
            sb.append(substring);
            sb.append(htmlSpanFragment.getText());
            lastPosition = htmlSpanFragment.getPosition();
        }
        sb.append(stringToHtml(text.substring(lastPosition)));
        return sb.toString();

    }

    private String stringToHtml(String substring) {
        return StringEscapeUtils.escapeHtml(substring)      //
                .replace("\r", "")                          //
                .replace("\n", String.format("<BR>%n%n"))    //
                .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")  //
                .replace("  ", "&nbsp;&nbsp;");
    }


    private ArrayList<HtmlSpanFragment> createHtmlSpanTag(Collection<MessageFragmentStyle> styles, HTML_MODE html_mode) {
        int id = 0;
        ArrayList<HtmlSpanFragment> htmlSpanFragments = new ArrayList<HtmlSpanFragment>();
        for (MessageFragmentStyle mfs : styles) {
            id++;
            LOGGER.finest(String.format("Creating span [id=%d] for style %s", id, mfs.getStyle().getName()));
            HtmlSpanFragment fragmentEnd = new HtmlSpanFragment(mfs.getOffset() + mfs.getLength() - 1, String.format("</span id=\"%d\">", id));
            HtmlSpanFragment fragmentStart = new HtmlSpanFragment(mfs.getOffset() - 1, generateSpanStart(html_mode,id, mfs));
            htmlSpanFragments.add(fragmentStart);
            htmlSpanFragments.add(fragmentEnd);
        }
        Collections.sort(htmlSpanFragments);
        return htmlSpanFragments;
    }

    private String generateSpanStart(HTML_MODE html_mode, int id, MessageFragmentStyle mfs) {
        if (HTML_MODE.FULL_HTML.equals(html_mode)) {
            return String.format("<span id=\"%d\" class=\"%s\">", id, mfs.getStyle().getName());
        }   else {
            String inlineStyle = Joiner.on(';').withKeyValueSeparator(":").join(styleToCssMap(mfs));
            return String.format("<span id=\"%d\" style=\"%s\">", id, inlineStyle);
        }

    }

    private void appendHeaders(StringBuilder sb, Map<String, Map<String, String>> stylesMap, String title, HTML_MODE html_mode) {
        if (HTML_MODE.FULL_HTML.equals(html_mode)) {
            sb.append("<HTML><HEAD>");
            sb.append("<style type=\"text/css\">\n");
            CssUtils cssUtils = new CssUtils();
            for (String styleName : stylesMap.keySet()) {
                Map<String, String> styleMap = stylesMap.get(styleName);
                sb.append(cssUtils.toString(styleName, styleMap));
            }
            sb.append("</style>\n");
            sb.append("<title>").append(title).append("</title>\n");
            sb.append("</HEAD><BODY>\n");
        } else {
            sb.append(INLINE_HTML_HEADER);
            sb.append("<title>").append(title).append("</title>\n");
        }
    }


    protected Map<String, String> styleToCssMap(MessageFragmentStyle mfs) {
        Style style = mfs.getStyle();
        HashMap<String, Object> attributes = new HashMap<String, Object>();
        Enumeration<?> attributeNames = style.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            Object nextElement = attributeNames.nextElement();
            attributes.put(nextElement.toString(),
                    style.getAttribute(nextElement));
        }

        Map<String, String> cssMap = new HashMap<String, String>();


        if (attributes.get("family") != null) {
            cssMap.put("font-family", String.format("'%s'",StyleConstants.getFontFamily(style)));
        }

        if (attributes.get("size") != null) {
            cssMap.put("font-size", Integer.toString(StyleConstants.getFontSize(style)));
        }

        if (attributes.get("foreground") != null) {
            cssMap.put("color", colorToHex(StyleConstants.getForeground(style)));
        }

        if (attributes.get("background") != null) {
            cssMap.put("background-color", colorToHex(StyleConstants.getBackground(style)));
        }

        if (attributes.get("bold") != null) {
            cssMap.put("font-weight", StyleConstants.isBold(style) ? "bold" : "normal");
        }

        if (attributes.get("italic") != null) {
            cssMap.put("font-style", StyleConstants.isItalic(style) ? "italic" : "normal");
        }

        if (attributes.get("underline") != null) {
            cssMap.put("text-decoration", StyleConstants.isItalic(style) ? "underline" : "none");
        }

        return cssMap;
    }


    protected String colorToHex(Color c) {
        return "#" + Integer.toHexString(c.getRGB()).substring(2);
    }

}
