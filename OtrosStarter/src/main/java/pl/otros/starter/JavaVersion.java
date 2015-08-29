package pl.otros.starter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaVersion implements Comparable<JavaVersion> {
  private int version;
  private int major;
  private int minor;

  public static void main(String[] args) {
    System.out.println(JavaVersion.fromString(System.getProperty("java.version")));
  }

  public static JavaVersion fromString(String s) {
    final Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+).*");
    System.out.println("Parsing " + s);
    final Matcher matcher = pattern.matcher(s);
    final boolean b = matcher.find();
    if (b) {
      return new JavaVersion(
        Integer.parseInt(matcher.group(1)),
        Integer.parseInt(matcher.group(2)),
        Integer.parseInt(matcher.group(3)));
    }
    return new JavaVersion(100, 0, 0);
  }

  public JavaVersion(int version, int major, int minor) {
    this.version = version;
    this.major = major;
    this.minor = minor;
  }


  @Override
  public int compareTo(JavaVersion o) {
    if (this.version - o.version == 0) {
      if (this.major - o.major == 0) {
        return this.minor - o.minor;
      } else {
        return this.major - o.major;
      }
    }
    return this.version - o.version;

  }

  @Override
  public String toString() {
    return "JavaVersion{" +
      ", version=" + version +
      ", major=" + major +
      ", minor=" + minor +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    JavaVersion that = (JavaVersion) o;

    return version == that.version && major == that.major && minor == that.minor;

  }

  @Override
  public int hashCode() {
    int result = version;
    result = 31 * result + major;
    result = 31 * result + minor;
    return result;
  }
}
