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
package pl.otros.logview.gui;

public class ConfKeys {

  public static final String TAILING_PANEL_PLAY = "gui.tailingview.play";
  public static final String TAILING_PANEL_FOLLOW = "gui.tailingview.follow";

  public static final String LOAD_EXPERIMENTAL_FEATURES = "loadExperimentalFeatures";
  public static final String VERSION_CHECK_ON_STARTUP = "updateCheck.checkNewVersionOnStart";
  public static final String VERSION_CHECK_SKIP_NOTIFICATION_FOR_VERSION = "updateCheck.doNotNotifyThisVersionOnStart";

  public static final String SOCKET_HUB_APPENDER_ADDRESSES = "gui.actions.sockethub.addresses";
  public static final String SOCKET_HUB_APPENDER_ADDRESSES_MAX_COUNT = "gui.actions.sockethub.addressesMaxCount";

  public static final String SEARCH_LAST_STRING = "gui.search.stringMatcher";
  public static final String SEARCH_LAST_REGEX = "gui.search.regexMatcher";
  public static final String SEARCH_LAST_QUERY = "gui.search.queryMatcher";
  public static final String SEARCH_LAST_COUNT = "gui.search.maxCount";
  public static final String SEARCH_HISTORY = "gui.search.history";

  public static final String FORMATTER_SOAP_REMOVE_MULTIREFS = "gui.formatter.soap.removeMultirefs";

  public static final String FORMATTER_SOAP_REMOVE_XSI_FOR_NIL = "gui.formatter.soap.removeXsiForNil";
  public static final String UUID = "uuid";
  public static final String HTTP_PROXY_USE = "http.proxy.use";
  public static final String HTTP_PROXY_HOST = "http.proxy.host";
  public static final String HTTP_PROXY_PORT = "http.proxy.port";

  public static final String HTTP_PROXY_USER = "http.proxy.user";
  public static final String MESSAGE_FORMATTER_MAX_SIZE = "gui.formatter.general.messageFormatMaximumSize";
  public static final String JUMP_TO_CODE_AUTO_JUMP_ENABLED = "services.jumpToCode.autoJump.enabled";
  public static final String JUMP_TO_CODE_ENABLED = "services.jumpToCode.enabled";
  public static final String JUMP_TO_CODE_HOST = "services.jumpToCode.host";


  public static final String JUMP_TO_CODE_PORT = "services.jumpToCode.port";
  public static final String LOG_TABLE_FORMAT_LEVEL_RENDERER = "gui.LoTableFormat.levelRenderer";
  public static final String LOG_TABLE_FORMAT_DATE_FORMAT = "gui.LogTableFormat.dateFormat";
  public static final String LOG_TABLE_FORMAT_PACKAGE_ABBREVIATIONS = "gui.LogTableFormat.packageAbbreviations";
  public static final String MESSAGE_DETAIL_SCROLL_HORIZONTAL = "gui.messageDetail.scrollHorizontal";
}
