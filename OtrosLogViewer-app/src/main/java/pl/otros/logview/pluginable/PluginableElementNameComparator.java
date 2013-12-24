package pl.otros.logview.pluginable;

import java.util.Comparator;

public class PluginableElementNameComparator implements Comparator<PluginableElement> {

  @Override
  public int compare(PluginableElement o1, PluginableElement o2) {
    if (o1 !=null && o2!=null){
      String name1 = o1.getName();
      String name2 = o2.getName();
      if (name1 != null && name2 !=null){
        return name1.compareTo(name2);
      } else if (name1 ==null){
        return -1;
      }   else if (name2 == null){
        return 1;
      }

    }
    return 0;
  }
}
