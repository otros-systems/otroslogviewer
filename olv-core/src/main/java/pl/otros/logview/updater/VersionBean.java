package pl.otros.logview.updater;

import com.alibaba.fastjson.annotation.JSONField;

public class VersionBean {

  @JSONField(name = "major")
  private String major;
  @JSONField(name = "minor")
  private String minor;
  @JSONField(name = "patch")
  private String patch;

  public String getMajor() {
    return major;
  }

  public void setMajor(String major) {
    this.major = major;
  }

  public String getMinor() {
    return minor;
  }

  public void setMinor(String minor) {
    this.minor = minor;
  }

  public String getPatch() {
    return patch;
  }

  public void setPatch(String patch) {
    this.patch = patch;
  }

  @Override
  public String toString() {
    return major + "." + minor + "." + patch;
  }
}
