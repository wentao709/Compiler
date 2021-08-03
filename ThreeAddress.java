// CSc 453, PA 4, Fall 2019
// Author: Wentao Zhou, Junyu Liu

public class ThreeAddress{
  enum OpType{
      PLUS, MINUS, MUL, DIV, EQ, NE, LTE, GTE, LT, GT, ASSIGN, LABEL, GOTO, CONTROL, FUNC_CALL, PARAM, RETRIEVE, RET
  }
  OpType op;
  Operand src1;
  Operand src2;
  Operand dest;
  boolean isGlobal;

  public ThreeAddress(OpType op,Operand src1, Operand src2, Operand dest){
    this.op = op;
    this.src1 = src1;
    this.src2 = src2;
    this.dest = dest;
    this.isGlobal = false;
  }

  public void setFlag(boolean isGlobal){
    this.isGlobal = isGlobal;
  }

  public boolean getFlag(){
    return isGlobal;
  }



  public String toString(){
    if (this.op == OpType.ASSIGN){
      if (src2 == null)
        return this.dest.toString() + " = " + this.src1.toString() + "\n";
      else
        return this.dest.toString() + " = " + this.src1.toString() + this.src2.toString() + "\n";
    }
  /*  else if(this.op == OpType.ASSIGN && Character.isDigit(this.src1.toString().charAt(0))){
      return this.dest.toString() + " = temp" + this.src1.toString() + "\n";
    }*/
    if (this.op == OpType.LABEL){
      return this.src1.toString() + "\n";
    }
    if (this.op == OpType.GOTO){
      return this.dest.toString() + "\n";
    }
    if (this.op == OpType.CONTROL){
      return "IF_" + this.dest.toString() + ": " + this.src1.toString() + ", " + this.src2.toString() + "\n";
    }
    return null;
  }
}
