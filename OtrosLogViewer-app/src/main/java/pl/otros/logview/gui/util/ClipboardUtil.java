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

package pl.otros.logview.gui.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

/**
 */
public class ClipboardUtil {


    public static void copyToClipboard(PlainTextAndHtml plainTextAndHtml){
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new HtmlTransferable(plainTextAndHtml),null);
    }

    public static void copyToClipboard(String plainText){
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(plainText),null);
    }


    private static class HtmlTransferable implements Transferable {
        private static final Logger LOGGER = LoggerFactory.getLogger(HtmlTransferable.class.getName());

        private static final ArrayList flavors = new ArrayList();


        public static final String TEXT_HTML_CLASS_JAVA_IO_READER = "text/html;class=java.io.Reader";
        public static final String TEXT_HTML_CHARSET_UNICODE_CLASS_JAVA_IO_INPUT_STREAM = "text/html;charset=unicode;class=java.io.InputStream";
        public static final String TEXT_HTML_CLASS_JAVA_LANG_STRING = "text/html;class=java.lang.String";
        public static final String TEXT_PLAIN_CLASS_JAVA_IO_READER = "text/plain;class=java.io.Reader";
        public static final String TEXT_PLAIN_CHARSET_UNICODE_CLASS_JAVA_IO_INPUT_STREAM = "text/plain;charset=unicode;class=java.io.InputStream";
        public static final String TEXT_PLAIN_CLASS_JAVA_LANG_STRING = "text/plain;class=java.lang.String";

        static {
            try {
                flavors.add(new DataFlavor(TEXT_HTML_CLASS_JAVA_LANG_STRING));
                flavors.add(new DataFlavor(TEXT_HTML_CLASS_JAVA_IO_READER));
                flavors.add(new DataFlavor(TEXT_HTML_CHARSET_UNICODE_CLASS_JAVA_IO_INPUT_STREAM));
                flavors.add(new DataFlavor(TEXT_PLAIN_CLASS_JAVA_LANG_STRING));
                flavors.add(new DataFlavor(TEXT_PLAIN_CLASS_JAVA_IO_READER));
                flavors.add(new DataFlavor(TEXT_PLAIN_CHARSET_UNICODE_CLASS_JAVA_IO_INPUT_STREAM));

            } catch (ClassNotFoundException ex) {
                LOGGER.error( "Did not found class for clipboard flavor", ex);
            }
        }

        private final PlainTextAndHtml plainTextAndHtml;

        public HtmlTransferable(PlainTextAndHtml plainTextAndHtml) {
            this.plainTextAndHtml = plainTextAndHtml;
        }



        public DataFlavor[] getTransferDataFlavors() {
            return (DataFlavor[]) flavors.toArray(new DataFlavor[flavors.size()]);
        }



        public boolean isDataFlavorSupported(DataFlavor flavor) {
            LOGGER.trace("Checking if flavor %s is available ", flavor.getMimeType());
            return flavors.contains(flavor);
        }



        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            LOGGER.trace("Getting transfer data for flavor: " + flavor.getMimeType());
            String text = flavor.getMimeType().startsWith("text/html")?plainTextAndHtml.getHtml():plainTextAndHtml.getPlainText();
            if (String.class.equals(flavor.getRepresentationClass())) {
                return text;
            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(text);
            } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                new ByteArrayInputStream(text.getBytes());
            }

            throw new UnsupportedFlavorException(flavor);

        }




    }
}

