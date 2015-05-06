/*
 * Copyright 2013 Krzysztof Otrebski
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
package pl.otros.logview.gui;

import pl.otros.logview.loader.Path;

import javax.swing.*;

public class Icons {

  @Path(path = "/img/icon.png")
  public static ImageIcon LOGO_SMALL;

  @Path(path = "/img/otros/logo16.png")
  public static ImageIcon LOGO_OTROS_16;

  @Path(path = "/img/otros/logo32.png")
  public static ImageIcon LOGO_OTROS_32;

  @Path(path = "/img/otros/logo64.png")
  public static ImageIcon LOGO_OTROS_64;

  @Path(path = "/img/fugue/control-double-270.png")
  public static ImageIcon FOLLOW_ON;

  @Path(path = "/img/fugue/control-skip-270.png")
  public static ImageIcon FOLLOW_OFF;

  @Path(path = "/img/fugue/control.png")
  public static ImageIcon TAILING_LIVE;

  @Path(path = "/img/fugue/control-stop.png")
  public static ImageIcon TAILING_PAUSE;

  @Path(path = "/img/class.png")
  public static ImageIcon CLASS;

  @Path(path = "/img/class_ignore.png")
  public static ImageIcon CLASS_IGNORED;

  @Path(path = "/img/package_open.png")
  public static ImageIcon PACKAGE_OPEN;

  @Path(path = "/img/package_open_ignore.png")
  public static ImageIcon PACKAGE_OPEN_IGNORED;

  @Path(path = "/img/package_closed.png")
  public static ImageIcon PACKAGE_CLOSE;

  @Path(path = "/img/package_closed_ignore.png")
  public static ImageIcon PACKAGE_CLOSE_IGNORED;

  @Path(path = "/img/note_empty.png")
  public static ImageIcon NOTE_EMPTY;

  @Path(path = "/img/note_exist.png")
  public static ImageIcon NOTE_EXIST;

  @Path(path = "/img/magnifier.png")
  public static ImageIcon MAGNIFIER;

  @Path(path = "/img/fugue/arrow-270-medium.png")
  public static ImageIcon ARROW_DOWN;

  @Path(path = "/img/fugue/arrow-090-medium.png")
  public static ImageIcon ARROW_UP;

  @Path(path = "/img/fugue/navigation-270-button.png")
  public static ImageIcon ARROW_DOWN_IN_BOX;

  @Path(path = "/img/fugue/navigation-090-button.png")
  public static ImageIcon ARROW_UP_IN_BOX;

  @Path(path = "/img/fugue/arrow-turn-090.png")
  public static ImageIcon ARROW_TURN_090;

  @Path(path = "/img/fugue/arrow-turn-270.png")
  public static ImageIcon ARROW_TURN_270;

  @Path(path = "/img/fugue/arrow-branch-270-left.png")
  public static ImageIcon ARROW_BRANCH_270;

  @Path(path = "/img/fugue/arrow-join.png")
  public static ImageIcon ARROW_JOIN;


  @Path(path = "/img/fugue/broom.png")
  public static ImageIcon CLEAR;

  @Path(path = "/img/fugue/beans.png")
  public static ImageIcon LEVEL_FINEST;
  @Path(path = "/img/fugue/bean.png")
  public static ImageIcon LEVEL_FINER;
  @Path(path = "/img/fugue/bean-green.png")
  public static ImageIcon LEVEL_FINE;
  @Path(path = "/img/fugue/hammer.png")
  public static ImageIcon LEVEL_CONFIG;
  @Path(path = "/img/fugue/information-button.png")
  public static ImageIcon LEVEL_INFO;
  @Path(path = "/img/fugue/exclamation-button.png")
  public static ImageIcon LEVEL_WARNING;
  @Path(path = "/img/fugue/exclamation-red.png")
  public static ImageIcon LEVEL_SEVERE;

  @Path(path = "/img/fugue/clipboard--plus.png")
  public static ImageIcon AUTOMATIC_MARKERS;
  @Path(path = "/img/fugue/clipboard--minus.png")
  public static ImageIcon AUTOMATIC_UNMARKERS;

  @Path(path = "/img/fugue/clipboard.png")
  public static ImageIcon MARKINGS_CLEAR;

  @Path(path = "/img/fugue24/wand-hat.png")
  public static ImageIcon WIZARD;

  @Path(path = "/img/fugue/application-export.png")
  public static ImageIcon EXPORT;

  @Path(path = "/img/fugue/application-import.png")
  public static ImageIcon IMPORT;

  @Path(path = "/img/fugue24/application-import.png")
  public static ImageIcon IMPORT_24;

  @Path(path = "/img/fugue/funnel--arrow.png")
  public static ImageIcon FILTER;

  @Path(path = "/img/fugue/table-insert-column.png")
  public static ImageIcon TABLE_COLUMN;

  @Path(path = "/img/fugue/layer-resize-replicate.png")
  public static ImageIcon TABLE_RESIZE;

  @Path(path = "/img/fugue/control-power.png")
  public static ImageIcon TURN_OFF;

  @Path(path = "/img/fugue/arrow-continue-180.png")
  public static ImageIcon TAIL;

  @Path(path = "/img/fugue/minus-white.png")
  public static ImageIcon TAB_HEADER_NORMAL;

  @Path(path = "/img/fugue/minus-circle.png")
  public static ImageIcon TAB_HEADER_HOVER;

  @Path(path = "/img/fugue/category.png")
  public static ImageIcon MARKER;

  @Path(path = "/img/fugue/compile.png")
  public static ImageIcon NEXT_LEVEL_INFO;

  @Path(path = "/img/fugue/compile-warning.png")
  public static ImageIcon NEXT_LEVEL_WARNING;

  @Path(path = "/img/fugue/compile-error.png")
  public static ImageIcon NEXT_LEVEL_ERROR;

  @Path(path = "/img/fugue/compile-flip.png")
  public static ImageIcon PREV_LEVEL_INFO;

  @Path(path = "/img/fugue/compile-warning-flip.png")
  public static ImageIcon PREV_LEVEL_WARNING;

  @Path(path = "/img/fugue/compile-error-flip.png")
  public static ImageIcon PREV_LEVEL_ERROR;

  @Path(path = "/img/fugue/question-button.png")
  public static ImageIcon HELP;

  @Path(path = "/img/fugue/wrench.png")
  public static ImageIcon WRENCH;

  @Path(path = "/img/fugue/wrench--arrow.png")
  public static ImageIcon WRENCH_ARROW;

  @Path(path = "/img/fugue/disk.png")
  public static ImageIcon DISK;

  @Path(path = "/img/fugue/disk--plus.png")
  public static ImageIcon DISK_PLUS;

  @Path(path = "/img/fugue/folder-open.png")
  public static ImageIcon FOLDER_OPEN;

  @Path(path = "/img/fugue/arrow-repeat.png")
  public static ImageIcon ARROW_REPEAT;

  @Path(path = "/img/fugue/plug.png")
  public static ImageIcon PLUGIN;

  @Path(path = "/img/fugue/plug-connect.png")
  public static ImageIcon PLUGIN_CONNECT;

  @Path(path = "/img/fugue/plug-disconnect.png")
  public static ImageIcon PLUGIN_DISCONNECT;

  @Path(path = "/img/fugue/plug--plus.png")
  public static ImageIcon PLUGIN_PLUS;

  @Path(path = "/img/fugue/color.png")
  public static ImageIcon MESSAGE_COLORIZER;

  @Path(path = "/img/fugue/edit-alignment-center.png")
  public static ImageIcon MESSAGE_FORMATTER;

  @Path(path = "/img/fugue/minus-button.png")
  public static ImageIcon DELETE;

  @Path(path = "/img/fugue/plus-button.png")
  public static ImageIcon ADD;

  @Path(path = "/img/fugue/table-select-row.png")
  public static ImageIcon TABLE_SELECT_ROW;

  @Path(path = "/img/fugue/credit-cards.png")
  public static ImageIcon CREDIT_CARDS;

  @Path(path = "/img/btn_donateCC_LG.gif")
  public static ImageIcon DONATE;

  @Path(path = "/img/fugue24/megaphone.png")
  public static ImageIcon MEGAPHONE_24;

  @Path(path = "/img/fugue/bin.png")
  public static ImageIcon BIN;

  @Path(path = "/img/fugue/document-copy.png")
  public static ImageIcon DOCUMENT_COPY;

  @Path(path = "/img/ide/eclipse-disconnected.png")
  public static ImageIcon ICE_ECLIPSE_DISCONNECTED;

  @Path(path = "/img/ide/idea-disconnected.png")
  public static ImageIcon IDE_IDEA_DISCONNCTED;

  @Path(path = "/img/ide/eclipse.gif")
  public static ImageIcon IDE_ECLIPSE;

  @Path(path = "/img/ide/idea.png")
  public static ImageIcon IDE_IDEA;

  @Path(path = "/img/fugue/tick-button.png")
  public static ImageIcon STATUS_OK;

  @Path(path = "/img/fugue/cross-button.png")
  public static ImageIcon STATUS_ERROR;

  @Path(path = "/img/fugue/question-button.png")
  public static ImageIcon STATUS_UNKNOWN;

  @Path(path = "/img/fugue/gear.png")
  public static ImageIcon GEAR;

  @Path(path = "/img/fugue/arrow-step-over.png")
  public static ImageIcon ARROW_STEP_OVER;

  @Path(path = "/img/fugue/arrow-step-over.png", gray = true)
  public static ImageIcon ARROW_STEP_OVER_GRAY;

  @Path(path = "/img/fugue/table-export.png")
  public static ImageIcon TABLE_EXPORT;

  @Path(path = "/img/fugue/table-import.png")
  public static ImageIcon TABLE_IMPORT;

  @Path(path = "/img/fugue/clipboard-paste.png")
  public static ImageIcon CLIPBOARD_PASTE;

  @Path(path = "/img/fugue/edit-signature.png")
  public static ImageIcon EDIT_SIGNATURE;

  @Path(path = "/img/fugue/edit-column.png")
  public static ImageIcon EDIT_COLUMNS;

  @Path(path = "/img/fugue/ui-scroll-pane-horizontal.png")
  public static ImageIcon SCROLL_HORIZONTAL;

}
