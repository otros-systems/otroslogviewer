package pl.otros.logview.api.model;

public class ClassWrapper {

  private final String className;

  public ClassWrapper(String className) {
    this.className = className;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ClassWrapper that = (ClassWrapper) o;

    return !(className != null ? !className.equals(that.className) : that.className != null);

  }

  @Override
  public int hashCode() {
    return className != null ? className.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "ClassWrapper{" +
      "className='" + className + '\'' +
      '}';
  }
}
