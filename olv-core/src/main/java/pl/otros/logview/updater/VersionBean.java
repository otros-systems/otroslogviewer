package pl.otros.logview.updater;

import com.google.gson.annotations.SerializedName;

public class VersionBean {

  @SerializedName("major")
  private String major;
  @SerializedName("minor")
  private String minor;
  @SerializedName("patch")
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
