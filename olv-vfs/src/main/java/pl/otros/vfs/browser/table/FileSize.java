/*
 * Copyright 2013 Krzysztof Otrebski (otros.systems@gmail.com)
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
 */

package pl.otros.vfs.browser.table;

import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSize implements Comparable<FileSize> {

  private static final long K = 1024;
  private static final long M = K * K;
  private static final long G = M * K;
  private static final long T = G * K;

  private long bytes;

  public FileSize(String string) {
    Pattern p = Pattern.compile("([\\d,.]+)\\s?([kKmMgGtT]{1})[Bb]{1}");
    Matcher matcher = p.matcher(string);
    if (matcher.matches()) {
      double count = Double.parseDouble(matcher.group(1).replace(',','.'));
      long multiplier = 1;
      if (StringUtils.isNotBlank(matcher.group(2))) {
        multiplier = getMultiplier(matcher.group(2).charAt(0));
      }
      bytes =(long)(count * multiplier);
    }
  }

  public FileSize(long bytes) {
    super();
    this.bytes = bytes;
  }

  public long getBytes() {
    return bytes;
  }

  public void setBytes(long bytes) {
    this.bytes = bytes;
  }

  @Override
  public String toString() {
    return convertToStringRepresentation(bytes);
  }

  public long getMultiplier(char multiplierChar) {
    long multiplier = 1;
    multiplierChar = Character.toLowerCase(multiplierChar);
    switch (multiplierChar) {
      case 't':
        multiplier = multiplier * 1024;
      case 'g':
        multiplier = multiplier * 1024;
      case 'm':
        multiplier = multiplier * 1024;
      case 'k':
        multiplier = multiplier * 1024;
        break;
    }
    return multiplier;
  }

  public static String convertToStringRepresentation(final long value) {
    final long[] dividers = new long[]{T, G, M, K, 1};
    final String[] units = new String[]{"TB", "GB", "MB", "KB", "B"};
    if (value == 0) {
      return format(0, 1, "B");
    } else if (value < 1) {
      return "Folder";
    }
    String result = null;
    for (int i = 0; i < dividers.length; i++) {
      final long divider = dividers[i];
      if (value >= divider) {
        result = format(value, divider, units[i]);
        break;
      }
    }
    return result;
  }

  private static String format(final long value,
                               final long divider,
                               final String unit) {
    final double result =
        divider > 1 ? (double) value / (double) divider : (double) value;
    DecimalFormat decimalFormat = new DecimalFormat();
    decimalFormat.setMaximumFractionDigits(1);
    decimalFormat.setMinimumFractionDigits(0);
    decimalFormat.setGroupingUsed(false);
    decimalFormat.setDecimalSeparatorAlwaysShown(false);
    return decimalFormat.format(result) + " " + unit;
  }

  @Override
  public int compareTo(FileSize o) {
    int result;
    if (o == null || bytes > o.bytes) {
      result = 1;
    } else if (bytes < o.bytes) {
      result = -1;
    } else {
      result = 0;
    }
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FileSize fileSize = (FileSize) o;

    return bytes == fileSize.bytes;

  }

  @Override
  public int hashCode() {
    return (int) (bytes ^ (bytes >>> 32));
  }
}
