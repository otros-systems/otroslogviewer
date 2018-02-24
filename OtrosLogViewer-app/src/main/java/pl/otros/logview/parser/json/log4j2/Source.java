package pl.otros.logview.parser.json.log4j2;

import com.google.gson.annotations.SerializedName;

public class Source {
  @SerializedName("class")
  private String clazz;
  private String method;
  private String file;
  private Integer line;

  public String getClazz() {
    return clazz;
  }

  public String getMethod() {
    return method;
  }

  public String getFile() {
    return file;
  }

  public Integer getLine() {
    return line;
  }
}
