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

package pl.otros.logview.gui.message.update;

import pl.otros.logview.api.MessageFragmentStyle;

import javax.swing.*;
import javax.swing.text.Style;

/**
 */
public class TextChunkWithStyle {
    private String string;
    private MessageFragmentStyle messageFragmentStyle;
    private Style style;
    private Icon icon;

    public TextChunkWithStyle(Icon icon) {
        this.icon = icon;
    }


    public TextChunkWithStyle(String string, Style style) {

        this.string = string;
        this.style = style;
    }

    public TextChunkWithStyle(String string, MessageFragmentStyle messageFragmentStyle) {
        this.string = string;
        this.messageFragmentStyle = messageFragmentStyle;
    }

    public Icon getIcon() {
        return icon;
    }

    public Style getStyle() {
        return style;
    }

    public String getString() {
        return string;
    }


    public MessageFragmentStyle getMessageFragmentStyle() {
        return messageFragmentStyle;
    }

    @Override
    public String toString() {
        return "TextChunkWithStyle{" +
                "string='" + string + '\'' +
                ", messageFragmentStyle=" + messageFragmentStyle +
                ", style=" + style +
                ", icon=" + icon +
                '}';
    }
}
