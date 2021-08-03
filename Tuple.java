import java.util.*;

public class Tuple{
  ArrayList<ThreeAddress> threeAddList;
  TreeMap <String, String[]> treeMap;
  Node func_decl;

  public Tuple(ArrayList<ThreeAddress> threeAddList, TreeMap <String, String[]> treeMap,Node func_decl){
    this.threeAddList = threeAddList;
    this.treeMap = treeMap;
    this.func_decl = func_decl;
  }

  public void setThreeAddrList(ArrayList<ThreeAddress> threeAddList){
    this.threeAddList = threeAddList;
  }

  public void setTreeMap(TreeMap <String, String[]> treeMap){
    this.treeMap = treeMap;
  }

  public void setName(String name){
    this.func_decl = func_decl;
  }

  public ArrayList<ThreeAddress> getThreeAddrList(){
    return threeAddList;
  }

  public TreeMap <String, String[]> getTreeMap(){
    return treeMap;
  }

  public Node getFunc(){
    return func_decl;
  }
}
