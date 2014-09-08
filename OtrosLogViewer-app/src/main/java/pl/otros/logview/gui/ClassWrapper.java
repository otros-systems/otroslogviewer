package pl.otros.logview.gui;

public class ClassWrapper {

    private String className;

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

        if (className != null ? !className.equals(that.className) : that.className != null) return false;

        return true;
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
