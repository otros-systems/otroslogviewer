/**
 * ****************************************************************************
 * Copyright 2014 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package pl.otros.logview.gui.message;

import org.apache.commons.lang.StringUtils;

import java.util.Optional;

/**
 * This class represents the location information about a line of code (the class, method,
 * line number, ...)
 *
 * @author Wim Deblauwe
 */
public class LocationInfo {
  // ------------------------------ FIELDS ------------------------------
  private final Optional<String> packageName;
  private final Optional<String> className;
  private final Optional<String> method;
  private final Optional<String> fileName;
  private final Optional<Integer> lineNumber;
  private final Optional<String> message;
// --------------------------- CONSTRUCTORS ---------------------------

  /**
   * Constructor
   *
   * @param className  the name of the class. If this is a FQN, then the package name will be parsed from it.
   * @param methodName the name of the method.
   * @param message    log message
   */
  public LocationInfo(Optional<String> className, Optional<String> methodName, Optional<String> message) {
    this.message = message;
    this.packageName = className.flatMap(this::parsePackageName);
    this.className = className;
    this.method = methodName;
    fileName = Optional.empty();
    lineNumber = Optional.empty();
  }

  /**
   * Constructor
   *
   * @param className  the name of the class. If this is a FQN, then the package name will be parsed from it.
   * @param methodName the name of the method (can be null)
   * @param fileName   the file name
   * @param lineNumber the line number
   * @param message    log message
   */
  public LocationInfo(Optional<String> className, Optional<String> methodName, Optional<String> fileName, Optional<Integer> lineNumber, Optional<String> message) {
    this.message = message;
    this.packageName = className.flatMap(this::parsePackageName);
    this.className = className;
    this.method = methodName;
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
   * @param lineNumber  the name of the line number in the file.
   * @param message     log message
   */
  public LocationInfo(Optional<String> packageName, Optional<String> className, Optional<String> method, Optional<String> fileName, Optional<Integer> lineNumber, Optional<String> message) {
    this.packageName = packageName;
    this.className = className;
    this.message = message;
    this.method = method;
    this.fileName = fileName;
    this.lineNumber = lineNumber;
  }

  public LocationInfo(String packageName, String className, String method, String fileName, Optional<Integer> lineNumber, String message) {
    this.packageName = Optional.ofNullable(packageName);
    this.className = Optional.ofNullable(className);
    this.message = Optional.ofNullable(message);
    this.method = Optional.ofNullable(method);
    this.fileName = Optional.ofNullable(fileName);
    this.lineNumber = lineNumber;
  }

  public LocationInfo(String clazz, String method, String file, Optional<Integer> lineNumber, Optional<String> message) {
    this.className = Optional.ofNullable(clazz);
    this.packageName = className.flatMap(this::parsePackageName);
    this.message = message;
    this.method = Optional.ofNullable(method);
    this.fileName = Optional.ofNullable(file);
    this.lineNumber = lineNumber;
  }
// -------------------------- PUBLIC METHODS --------------------------


  public Optional<Integer> getLineNumber() {
    return lineNumber;
  }

  public Optional<String> getMessage() {
    return message;
  }

  public Optional<String> getPackageName() {
    return packageName;
  }

  public Optional<String> getClassName() {
    return className;
  }

  public Optional<String> getMethod() {
    return method;
  }

  public Optional<String> getFileName() {
    return fileName;
  }

  @Override
  public String toString() {
    return stringForm();
  }


  public String stringForm() {
    StringBuilder sb = new StringBuilder("LocationInfo{\n");
    packageName.ifPresent(s -> sb.append("package='").append(s).append("'\n"));
    className.ifPresent(s -> sb.append("class='").append(s).append("'\n"));
    method.ifPresent(s -> sb.append("method='").append(s).append("'\n"));
    fileName.ifPresent(s -> sb.append("file='").append(s).append("'\n"));
    lineNumber.ifPresent(s -> sb.append("line='").append(s).append("'\n"));
    message.ifPresent(s -> sb.append("message'").append(s).append("'\n"));
    sb.append("}");
    return sb.toString();
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
    Optional<Integer> lineNumber;
    final String lineNumberString = fullInfo.substring(lastColon + 1, lastClosingBrace);
    lineNumber = Optional.of(Integer.parseInt(lineNumberString));
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
      Optional.of(packageName),
      Optional.of(className),
      Optional.of(methodName),
      Optional.of(fileName),
      lineNumber,
      Optional.empty());
  }

  static String removeLambdas(String fullInfo) {
    return fullInfo.replaceFirst("\\$\\$.*?\\.", "\\$Lambda.");
  }


  // -------------------------- PRIVATE METHODS --------------------------

  private Optional<String> parsePackageName(String className) {
    Optional<String> result;
    if (className == null) {
      result = null;
    } else {
      if (className.contains(".")) {
        int lastDot = className.lastIndexOf(".");
        result = Optional.of(className.substring(0, lastDot));
      } else {
        result = Optional.empty();
      }
    }
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LocationInfo that = (LocationInfo) o;

    return packageName != null ? packageName.equals(that.packageName) : that.packageName == null
      && (className != null ? className.equals(that.className) : that.className == null
      && (method != null ? method.equals(that.method) : that.method == null
      && (fileName != null ? fileName.equals(that.fileName) : that.fileName == null
      && (lineNumber != null ? lineNumber.equals(that.lineNumber) : that.lineNumber == null
      && (message != null ? message.equals(that.message) : that.message == null)))));

  }

  @Override
  public int hashCode() {
    int result = packageName != null ? packageName.hashCode() : 0;
    result = 31 * result + (className != null ? className.hashCode() : 0);
    result = 31 * result + (method != null ? method.hashCode() : 0);
    result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
    result = 31 * result + (lineNumber != null ? lineNumber.hashCode() : 0);
    result = 31 * result + (message != null ? message.hashCode() : 0);
    return result;
  }
}
