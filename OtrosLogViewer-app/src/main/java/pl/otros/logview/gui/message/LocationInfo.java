/**
 * ****************************************************************************
 * Copyright 2014 Krzysztof Otrebski
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package pl.otros.logview.gui.message;

import org.apache.commons.lang.StringUtils;

/**
 * This class represents the location information about a line of code (the class, method,
 * line number, ...)
 *
 * @author Wim Deblauwe
 */
public class LocationInfo {
  // ------------------------------ FIELDS ------------------------------
  public static final int UNKNOWN_LINE_NUMBER = -1;
  private final String packageName;
  private final String className;
  private final String method;
  private final String fileName;
  private final int lineNumber;
// --------------------------- CONSTRUCTORS ---------------------------

  /**
   * Constructor
   *
   * @param className  the name of the class. If this is a FQN, then the package name will be parsed from it.
   * @param methodName the name of the method.
   */
  public LocationInfo(String className, String methodName) {
    this.packageName = parsePackageName(className);
    this.className = className;
    this.method = isEmpty(methodName) ? "<unknown>" : methodName;
    fileName = null;
    lineNumber = UNKNOWN_LINE_NUMBER;
  }

  /**
   * Constructor
   *
   * @param className  the name of the class. If this is a FQN, then the package name will be parsed from it.
   * @param methodName the name of the method (can be null)
   * @param fileName   the file name
   * @param lineNumber the line number
   */
  public LocationInfo(String className, String methodName, String fileName, int lineNumber) {
    this.packageName = parsePackageName(className);
    this.className = className;
    this.method = isEmpty(methodName) ? "<unknown>" : methodName;
    this.fileName = fileName;
    this.lineNumber = lineNumber;
  }

  /**
   * Constructor
   *
   * @param packageName the package name (of the class)
   * @param className   the name of the class.
   * @param method      the name of the method
   * @param fileName    the name of the file
   * @param lineNumber  the name of the line number in the file. Can be {@link #UNKNOWN_LINE_NUMBER} if not known.
   */
  public LocationInfo(String packageName, String className, String method, String fileName, int lineNumber) {
    this.packageName = packageName;
    this.className = className;
    this.method = isEmpty(method) ? "<unknown>" : method;
    this.fileName = fileName;
    this.lineNumber = lineNumber;
  }
// -------------------------- PUBLIC METHODS --------------------------

  public String getClassName() {
    return className;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getMethod() {
    return method;
  }

  public String getFileName() {
    return fileName;
  }

  public int getLineNumber() {
    return lineNumber;
  }


  @Override
  public String toString() {
    return className + "." + method + "(" + fileName + ":" + lineNumber + ")";
  }
// -------------------------- STATIC METHODS --------------------------

  /**
   * build a LocationInfo instance from one line of a stackTrace
   *
   * @param fullInfo one line from a stacktrace)
   * @return a LocationInfo based on the input (or null if input could not be parsed)
   */
  public static LocationInfo parse(String fullInfo) {
    if (fullInfo == null) {
      return null;
    }
    fullInfo = removeLambdas(fullInfo);
    fullInfo = fullInfo.trim();
    fullInfo = StringUtils.removeStart(fullInfo, "at ");
    int lastClosingBrace = fullInfo.indexOf(')');
    int lastColon = fullInfo.lastIndexOf(':', lastClosingBrace);
    int lastOpeningBrace = fullInfo.lastIndexOf('(', lastColon);
    if (lastOpeningBrace == -1 || lastClosingBrace == -1 || lastColon == -1) {
      return null;
    }
    String packageName;
    String className;
    String methodName;
    String fileName;
    int lineNumber;
    final String lineNumberString = fullInfo.substring(lastColon + 1, lastClosingBrace);
    lineNumber = Integer.parseInt(lineNumberString);
    fileName = fullInfo.substring(lastOpeningBrace + 1, lastColon);
    // packageName
    fullInfo = fullInfo.substring(0, lastOpeningBrace);
    int lastDot = fullInfo.lastIndexOf('.');
    if (lastDot == -1) {
      return null;
    } else {
      methodName = fullInfo.substring(lastDot + 1);
      className = fullInfo.substring(0, lastDot);
      lastDot = className.lastIndexOf(".");
      if (lastDot == -1) {
        packageName = ""; // the default package
      } else {
        packageName = className.substring(0, lastDot);
      }
    }
    return new LocationInfo(
      packageName,
      className,
      methodName,
      fileName,
      lineNumber);
  }

  static String removeLambdas(String fullInfo) {
    System.out.println("LocationInfo.removeLambdas -> " + fullInfo);
    return fullInfo.replaceFirst("\\$\\$.*?\\.","\\$Lambda.");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LocationInfo)) return false;
    LocationInfo that = (LocationInfo) o;
    if (lineNumber != that.lineNumber) return false;
    if (className != null ? !className.equals(that.className) : that.className != null) return false;
    if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;
    if (method != null ? !method.equals(that.method) : that.method != null) return false;
    return !(packageName != null ? !packageName.equals(that.packageName) : that.packageName != null);
  }

  @Override
  public int hashCode() {
    int result = packageName != null ? packageName.hashCode() : 0;
    result = 31 * result + (className != null ? className.hashCode() : 0);
    result = 31 * result + (method != null ? method.hashCode() : 0);
    result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
    result = 31 * result + lineNumber;
    return result;
  }
  // -------------------------- PRIVATE METHODS --------------------------

  private String parsePackageName(String className) {
    String result;
    if (className == null) {
      result = null;
    } else {
      if (className.contains(".")) {
        int lastDot = className.lastIndexOf(".");
        result = className.substring(0, lastDot);
      } else {
        result = null;
      }
    }
    return result;
  }

  private static boolean isEmpty(String value) {
    return (value == null || value.length() == 0);
  }
}
