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
package net.sf.vfsjfilechooser.accessories.bookmarks;

/**
 * Utility class for dealing with byte strings
 *
 * @author Stan Love
 * @version 0.0.1
 */
final class Util {
  public static byte[] hexByteArrayToByteArray(byte[] b) {
    int len = b.length;
    byte[] data = new byte[len / 2];
    char c1, c2;
    for (int i = 0; i < len - 1; i = i + 2) {
      c1 = (char) b[i];
      c2 = (char) b[i + 1];
      data[i / 2] = (byte) ((Character.digit(c1, 16) << 4) + Character
          .digit(c2, 16));
    }
    return data;
  }

  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i = i + 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
          .digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  public static String tohex(byte b) {
    if (b == 0)
      return "0";
    else if (b == 1)
      return "1";
    else if (b == 2)
      return "2";
    else if (b == 3)
      return "3";
    else if (b == 4)
      return "4";
    else if (b == 5)
      return "5";
    else if (b == 6)
      return "6";
    else if (b == 7)
      return "7";
    else if (b == 8)
      return "8";
    else if (b == 9)
      return "9";
    else if (b == 10)
      return "a";
    else if (b == 11)
      return "b";
    else if (b == 12)
      return "c";
    else if (b == 13)
      return "d";
    else if (b == 14)
      return "e";
    else if (b == 15)
      return "f";
    System.out.println("FATAL ERROR-- BIG TIME error calling tohex");
    System.exit(10);
    return "z"; // we should never be here
  }

  public static String byteArraytoHexString(byte[] b) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < b.length; i++) {
      byte btemp = b[i];
      byte b1 = (byte) ((btemp >> 4) & 0x0f);
      sb.append(tohex(b1));
      byte b2 = (byte) (btemp & 0x0f);
      sb.append(tohex(b2));
    }
    return sb.toString();
  }

}
