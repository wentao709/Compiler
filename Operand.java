public class Operand{
  Boolean flag;
  String op;
  public Operand(String op){
    flag = true;
    this.op = op;
  }

  public Operand(int op){
    flag = false;
    this.op = Integer.toString(op);
  }

  public boolean getFlag(){
    return flag;
  }

  public String toString(){
    return op;
  }
}
