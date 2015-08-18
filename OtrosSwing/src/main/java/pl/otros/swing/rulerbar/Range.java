package pl.otros.swing.rulerbar;

class Range {
  private final int start;
  private final int end;

  public Range(final int start, final int end) {
    super();
    this.start = start;
    this.end = end;
  }

  public boolean isInRange(final int x) {
    return x >= start & x <= end;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + end;
    result = prime * result + start;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Range other = (Range) obj;
    if (end != other.end)
      return false;
    return start == other.start;
  }

  @Override
  public String toString() {
    return "Range [start=" + start + ", end=" + end + "]";
  }

}