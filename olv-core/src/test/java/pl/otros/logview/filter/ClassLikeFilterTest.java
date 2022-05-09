package pl.otros.logview.filter;

import org.testng.annotations.Test;
import pl.otros.logview.api.model.LogData;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ClassLikeFilterTest {


  private ClassLikeFilter filter = new ClassLikeFilter("", "") {

    @Override
    public Function<LogData, String> extractValueFunction() {
      return logData -> logData.getLoggerName();
    }
  };

  @Test
  public void testPreparePackagesNodes() throws Exception {
    //given
    final HashSet<String> clazzes = new HashSet<>(Arrays.asList("a.b.C", "a.b.D", "c.d.A", "A"));

    //when
    final TreeSet<String> packages = filter.preparePackagesNodes(clazzes);

    //then
    assertEquals(packages.size(), 4);
    assertEquals(packages, new HashSet<>(Arrays.asList("a", "a.b", "c", "c.d")));
    packages.stream().forEach(System.out::println);
  }

  @Test
  public void testCreatePackagesTree() throws Exception {
    //given
    TreeSet<String> packages = new TreeSet<>(Arrays.asList("a", "a.b", "c", "c.d"));
    HashMap<ClassLikeFilter.Clazz, DefaultMutableTreeNode> map = new HashMap<>();
    final DefaultMutableTreeNode root = new DefaultMutableTreeNode(new ClassLikeFilter.Clazz("root"));

    //when
    filter.createPackagesTree(packages, map, root);

    //then
    assertEquals(map.size(), 4);
    assertTrue(map.containsKey(new ClassLikeFilter.Clazz("a")));
    assertTrue(map.containsKey(new ClassLikeFilter.Clazz("a.b")));
    assertTrue(map.containsKey(new ClassLikeFilter.Clazz("c")));
    assertTrue(map.containsKey(new ClassLikeFilter.Clazz("c.d")));
    final DefaultMutableTreeNode child = map.get(new ClassLikeFilter.Clazz("a.b"));
    final DefaultMutableTreeNode parent = map.get(new ClassLikeFilter.Clazz("a"));
    System.out.println(parent);
    final TreeNode parent1 = child.getParent();
    assertEquals(parent1, parent);
  }


  @Test
  public void testAddClassesLeafs() throws Exception {
    //given
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    Set<String> classesSet = new HashSet<>(Arrays.asList("a.C1","a.b.C1","b.A","C1","C2"));
    TreeSet<String> packages = filter.preparePackagesNodes(classesSet);
    HashMap<ClassLikeFilter.Clazz, DefaultMutableTreeNode> clazzNodeMap = new HashMap<>();
    filter.createPackagesTree(packages, clazzNodeMap, root);

    //when
    // Create classes leafs
    filter.addClassesLeafs(classesSet, root, clazzNodeMap);

    //then
    final List<DefaultMutableTreeNode> level1Nodes = Collections.list(root.children()).stream().map(t -> (DefaultMutableTreeNode)t).collect(Collectors.toList());
    assertEquals(level1Nodes.size(),4);
    final List<ClassLikeFilter.Clazz> clazzesLevel1 = level1Nodes.stream()
      .map(n -> (ClassLikeFilter.Clazz) n.getUserObject())
      .sorted((c1,c2)->c1.toFullString().compareTo(c2.toFullString()))
      .collect(Collectors.toList());

    clazzesLevel1.stream().forEach(l -> System.out.println("\"" + l + "\""));
    assertEquals(clazzesLevel1.get(0),new ClassLikeFilter.Clazz("C1"));
    assertEquals(clazzesLevel1.get(1),new ClassLikeFilter.Clazz("C2"));
    assertEquals(clazzesLevel1.get(2),new ClassLikeFilter.Clazz("a"));
    assertEquals(clazzesLevel1.get(3),new ClassLikeFilter.Clazz("b"));
  }

}