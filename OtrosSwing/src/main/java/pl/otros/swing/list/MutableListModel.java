package pl.otros.swing.list;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public class MutableListModel<T> extends AbstractListModel {

  private final ArrayList<T> list;

  public MutableListModel() {
    list = new ArrayList<>();
  }

  @Override
  public int getSize() {
    return list.size();
  }

  @Override
  public T getElementAt(int index) {
    return list.get(index);
  }

  public void clear(){
    final int size = list.size();
    list.clear();
    if (size > 0){
      fireIntervalRemoved(this,0, size-1);
    }
  }

  public void add(T element) {
    list.add(element);
    fireIntervalAdded(this, list.size() - 1, list.size() - 1);
  }
  public void addAll(Collection<T> elements) {
    if (!elements.isEmpty()){
      final int size = list.size();
      list.addAll(elements);
      fireIntervalAdded(this, size, list.size() - 1);
    }
  }

  public void remove(int index) {
    list.remove(index);
    fireIntervalRemoved(this, index, index);
  }

  public void change(int index, T element) {
    list.set(index, element);
    fireContentsChanged(this, index, index);
  }

  public void move(int from, int to) {
    list.add(to, list.get(from));
    if (to < from) {
      from++;
    }
    list.remove(from);
    fireContentsChanged(this, Math.min(from, to), Math.max(from, to));

  }

  public List<T> getList() {
    return new ArrayList<>(list);
  }

}