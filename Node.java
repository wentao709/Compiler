// CSc 453, PA 3, Fall 2019
// Author: Wentao Zhou, Junyu Liu
import java.util.*;
public class Node{
    Scanner.Token token;
    int id;
    Node left;
    Node right;
    Node[] childrenList;
    ArrayList <Node> children;
    int level; //error checking
    boolean flag; //behind 'or' or 'and'
    int trueLabel; // for control flow
    int falseLabel; // for control flow
    boolean call;

    Node(Scanner.Token token){
      this.token = token;
      id = -1;
      right = null;
      left = null;
      level = 0;
      flag = true;
      trueLabel = 0;
      falseLabel = 0;
      call = false;
      children = new ArrayList <Node>();
      childrenList = new Node[4]; // for func_decl
    }

    public void setCall(){
      call = true;
    }

    public boolean getCall(){
      return call;
    }

    public void addChildList(Node node, int index){
      childrenList[index] = node;
    }

    public Node[] getChildList(){
      return childrenList;
    }

    public void addChildren(Node child){
      children.add(child);
    }

    public void setChildren(ArrayList<Node> children){
      this.children = children;
    }

    public ArrayList <Node> getChildren(){
      return children;
    }

    public Node addLeft(Node parent, Node current){
      parent.left = current;
      return parent;
    }

    public Node addRight(Node parent, Node current){
      parent.right = current;
      return parent;
    }

    public void setID(int id){
      this.id = id;
    }

    public Node leftNode(Node node){
      node = node.left;
      while(node.token.tokenType==Scanner.TokenType.AND || node.token.tokenType == Scanner.TokenType.OR){
        node = node.right;
      }
      return node;
    }

    public void incrementTrue(){
      trueLabel+=1;
    }

    public void incrementfalse(){
      falseLabel+=1;
    }

    public void setFlag(){
      flag = false;
    }

}
