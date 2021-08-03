// CSc 453, PA 5, Fall 2019
// Author: Wentao Zhou, Junyu Liu
/* basically my idea is using leftInd and rightInd to parse out
   the tokens we want. For example, if we get a string like
   "if (2 < 3) {int x =5;} "
   index of if = 0
   then I would call the function expr(2,4), where 2 is the index of "2" and "4" is the
   index of 3, so if expr function actually got the string "2 < 4", then we use the AST
   to parse out the string we want. ALso, I would also control(7,11), 7 is the index of "int",
   and 11 is the index of ";", so the control function are dealing with the string "int x = 5;"
*/

import java.util.*;
import java.lang.String;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AdvancedJava {

  Scanner scan;
  int tempID = 0;
  int tlabelID = 0; // Label id for true
  int flabelID = 0; // Label id for false
  int rlabelID = 0; // Label id for loops

  static Scanner.Token lookahead;
  Node func_decl;
  int index = 0; //index of current token
  ArrayList <ThreeAddress> threeAddressResult;
  ArrayList<Scanner.Token> tokens = null; //array of tokens
  TreeMap<String, String> global = new
              TreeMap<String, String>();
  ArrayList <ThreeAddress> localThreeAddress;
  ArrayList <Tuple> tupleList;
  ArrayList <Node> param_list;
  ArrayList <Node> argu_list;

  public void codeGen(String eval, String fileName) throws IOException{
    tupleList = new ArrayList<Tuple>();
    tlabelID = 0;
    flabelID = 0;
    rlabelID = 0;
    scan = new Scanner();
    index=0;
    threeAddressResult = new ArrayList<ThreeAddress>();
    tokens = scan.extractTokens(eval);
    lookahead = tokens.get(index);
    prog();
    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
    writer.write("#include <stdio.h>\n");
    writer.write("#include <inttypes.h>\n");
    if (!global.isEmpty()){
      writer.write("int64_t ");
      int num = 0;
      for(Map.Entry<String,String> entry : global.entrySet()){
        String type = entry.getValue();
        if (type.equals("int")){
          num += 1;
        }
      }
      int number = 0;
      for(Map.Entry<String,String> entry : global.entrySet()) {
        String id = entry.getKey();
        String type = entry.getValue();
        if (type.equals("int")){
          number += 1;
          if (number < num)
            writer.write(id + " = 0,");
          else
            writer.write(id + " = 0;\n");
        }
      }
    }
    writer.write("\n");
    writer.write("int main(int argc, char **argv){\n");
    writer.write("int64_t r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0, va = 0;\n");
    writer.write("int64_t stack[100];\n");
    writer.write("int64_t *sp = &stack[99];\n");
    writer.write("int64_t *fp = &stack[99];\n");
    writer.write("int64_t *ra = &&exit;\n");
    writer.write("goto mainEntry;\n");
      //System.out.println(func_decl.token.tokenVal + " na");
      for(Tuple tuple : tupleList){
        System.out.println("name " + tuple.getFunc().token.tokenVal);
        writer.write(tuple.getFunc().token.tokenVal + ":\n");
        writer.write("sp = sp - 2;\n");
        writer.write("*(sp+2) = fp;\n");
        writer.write("*(sp+1) = ra;\n");
        writer.write("fp = sp;\n");
        String [] offset = new String[2];
        int count = 0;
        for (Map.Entry<String,String[]> entry : tuple.getTreeMap().entrySet()){
          if(tuple.getTreeMap().get(entry.getKey())[2].equals("false")){
            count += 1;
            tuple.getTreeMap().get(entry.getKey())[1] = Integer.toString(count);
          }
        }
        int count_para = -3;
        System.out.println(tuple.func_decl.getChildList()[1] + " 1");
        if (tuple.func_decl.getChildList()[1] != null){ //function with parameter
          for (int i = 0; i < tuple.func_decl.getChildList()[1].getChildren().size() ;i++){ // for offset of parameters
            System.out.println(tuple.func_decl.getChildList()[1].getChildren().get(i).token.tokenVal + " 2");
            System.out.println(tuple.getTreeMap().containsKey(tuple.func_decl.getChildList()[1].getChildren().get(i).token.tokenVal) + " 3");
            if (tuple.getTreeMap().containsKey(tuple.func_decl.getChildList()[1].getChildren().get(i).token.tokenVal) &&
                tuple.getTreeMap().get(tuple.func_decl.getChildList()[1].getChildren().get(i).token.tokenVal)[2].equals("true")){
              tuple.getTreeMap().get(tuple.func_decl.getChildList()[1].getChildren().get(i).token.tokenVal)[1] = Integer.toString(count_para);
            }
            else
              throw new Error("not exist");
            count_para -= 1;
          }
        }
        writer.write("sp = sp - " + count + ";\n");
        for (ThreeAddress threeAddress: tuple.getThreeAddrList()){
          if (threeAddress.op == ThreeAddress.OpType.ASSIGN && threeAddress.src1.getFlag() == false){ // int
            writer.write("r1 = " + threeAddress.src1 + ";\n");
            if (tuple.getTreeMap().containsKey(threeAddress.dest.toString()) && !threeAddress.getFlag())
              writer.write("*(fp - " + tuple.getTreeMap().get(threeAddress.dest.toString())[1] + ") = r1;\n");
            else{
              if (global.containsKey(threeAddress.dest.toString())){
                writer.write(threeAddress.dest.toString() + " = r1;\n");
              }
            }
          }else if(threeAddress.op == ThreeAddress.OpType.ASSIGN){ //string
            if (tuple.getTreeMap().containsKey(threeAddress.src1.toString()) && !threeAddress.getFlag()){
              writer.write("r1 = " + "*(fp - " + tuple.getTreeMap().get(threeAddress.src1.toString())[1] + ");\n");
            }else{
              if (global.containsKey(threeAddress.src1.toString())){
                writer.write(threeAddress.src1.toString() + " = r1;\n");
              }
            }
            if (tuple.getTreeMap().containsKey(threeAddress.dest.toString()) && !threeAddress.getFlag()){
              writer.write("*(fp - " + tuple.getTreeMap().get(threeAddress.dest.toString())[1] + ") = r1;\n");
            }
            else{
              if (global.containsKey(threeAddress.dest.toString())){
                writer.write(threeAddress.dest.toString() + " = r1;\n");
              }
            }
          }else if(threeAddress.op == ThreeAddress.OpType.GOTO){
            writer.write("goto falseLabel" + threeAddress.dest.toString() + ";\n");
          }else if(threeAddress.op == ThreeAddress.OpType.LABEL){
            writer.write(threeAddress.src1 + ":\n");
          }else if(threeAddress.op == ThreeAddress.OpType.PARAM){
            writer.write("sp = sp - 1\n");
            if (tuple.getTreeMap().containsKey(threeAddress.src1.toString()) && !threeAddress.getFlag())
              writer.write("*(sp+1) = *(fp-(" + tuple.getTreeMap().get(threeAddress.src1.toString())[1] + "));\n");
            else{
              if (global.containsKey(threeAddress.src1.toString()))
                writer.write("*(sp+1) = " + threeAddress.src1.toString() + ";\n");
            }
          }else if(threeAddress.op == ThreeAddress.OpType.FUNC_CALL){
            writer.write("ra = &&retLabel0;\n");
            writer.write("goto main;\n");
            writer.write("retLabel0;\n");
            writer.write("to;\n");
            writer.write("sp = sp + " + threeAddress.src1 + ";\n");
          }else if(threeAddress.op == ThreeAddress.OpType.RETRIEVE){
            if (tuple.getTreeMap().containsKey(threeAddress.src1.toString()) && !threeAddress.getFlag())
              writer.write("*(fp-(" + tuple.getTreeMap().get(threeAddress.src1.toString())[1] + ")) = va;\n");
            else{
              if (global.containsKey(threeAddress.src1.toString()))
                writer.write(threeAddress.src1.toString() + " = va;\n");
            }
          }else if(threeAddress.op == ThreeAddress.OpType.RET){
            if (tuple.getTreeMap().containsKey(threeAddress.src1.toString()) && !threeAddress.getFlag())
              writer.write("va = *(fp-(" + tuple.getTreeMap().get(threeAddress.src1.toString())[1] + "));\n");
            else{
              if (global.containsKey(threeAddress.src1.toString()))
                writer.write("va = " + threeAddress.src1.toString() + ";\n");
            }
          }else{
            if (tuple.getTreeMap().containsKey(threeAddress.src1.toString()) && !threeAddress.getFlag())
              writer.write("r1 = *(fp - " + tuple.getTreeMap().get(threeAddress.src1.toString())[1] + ");\n");
            else{
              if (global.containsKey(threeAddress.src1.toString())){
                writer.write("r1 = " + threeAddress.src1.toString() + ";\n");
              }
            }
            if (tuple.getTreeMap().containsKey(threeAddress.src2.toString()) && !threeAddress.getFlag())
              writer.write("r2 = *(fp - " + tuple.getTreeMap().get(threeAddress.src2.toString())[1] + ");\n");
            else{
              if (global.containsKey(threeAddress.src2.toString()))
                writer.write("r2 = " + threeAddress.src2.toString() + ";\n");
            }
            if(threeAddress.op == ThreeAddress.OpType.PLUS){
              writer.write("r3 = r1 + r2;\n");
              if (tuple.getTreeMap().containsKey(threeAddress.dest.toString()) && !threeAddress.getFlag())
                writer.write("*(fp - " + tuple.getTreeMap().get(threeAddress.dest.toString())[1] + ") = r3;\n");
              else{
                if (global.containsKey(threeAddress.dest.toString()))
                  writer.write(threeAddress.dest.toString() + " = r3;\n");
              }
            }
            else if(threeAddress.op == ThreeAddress.OpType.MINUS){
              writer.write("r3 = r1 - r2;\n");
              if (tuple.getTreeMap().containsKey(threeAddress.dest.toString()) && !threeAddress.getFlag())
                writer.write("*(fp - " + tuple.getTreeMap().get(threeAddress.dest.toString())[1] + ") = r3;\n");
              else{
                if (global.containsKey(threeAddress.dest.toString()))
                  writer.write(threeAddress.dest.toString() + " = r3;\n");
              }
            }
            else if(threeAddress.op == ThreeAddress.OpType.MUL){
              writer.write("r3 = r1 * r2;\n");
              if (tuple.getTreeMap().containsKey(threeAddress.dest.toString()) && !threeAddress.getFlag())
                writer.write("*(fp - " + tuple.getTreeMap().get(threeAddress.dest.toString())[1] + ") = r3;\n");
              else{
                if (global.containsKey(threeAddress.dest.toString()))
                  writer.write(threeAddress.dest.toString() + " = r3;\n");
              }
            }
            else if(threeAddress.op == ThreeAddress.OpType.DIV){
              writer.write("r3 = r1 / r2;\n");
              if (tuple.getTreeMap().containsKey(threeAddress.dest.toString()) && !threeAddress.getFlag())
                writer.write("*(fp - " + tuple.getTreeMap().get(threeAddress.dest.toString())[1] + ") = r3;\n");
              else{
                if (global.containsKey(threeAddress.dest.toString()))
                  writer.write(threeAddress.dest.toString() + " = r3;\n");
              }
            }
            else if(threeAddress.op == ThreeAddress.OpType.EQ){
              writer.write("if(r1 == r2) goto trueLabel" + threeAddress.dest + ";\n");
            }
            else if(threeAddress.op == ThreeAddress.OpType.NE){
              writer.write("if(r1 != r2) goto trueLabel" + threeAddress.dest + ";\n");
            }
            else if(threeAddress.op == ThreeAddress.OpType.LTE){
              writer.write("if(r1 <= r2) goto trueLabel" + threeAddress.dest + ";\n");
            }
            else if(threeAddress.op == ThreeAddress.OpType.GTE){
              writer.write("if(r1 >= r2) goto trueLabel" + threeAddress.dest + ";\n");
            }
            else if(threeAddress.op == ThreeAddress.OpType.LT){
              writer.write("if(r1 < r2) goto trueLabel" + threeAddress.dest + ";\n");
            }
            else if(threeAddress.op == ThreeAddress.OpType.GT){
              writer.write("if(r1 > r2) goto trueLabel" + threeAddress.dest + ";\n");
            }
          }
        }
        writer.write("sp = sp + " + count + ";\n");
        writer.write("fp = *(sp+2);\n");
        writer.write("ra = *(sp+1);\n");
        writer.write("sp = sp + 2;\n");
        writer.write("goto *ra;\n");
      }

    writer.write("exit:\n");
    writer.write("return reserved;\n");
    writer.write("}\n");
    writer.close();
  }

  void prog(){
    int lastInd = tokens.size()-1;
    if (tokens.size() >= 5){
      if ((tokens.get(0).tokenType == Scanner.TokenType.PUBLIC || tokens.get(0).tokenType == Scanner.TokenType.PRIVATE)
          && tokens.get(1).tokenType == Scanner.TokenType.CLASS
          && tokens.get(2).tokenType == Scanner.TokenType.ID && tokens.get(3).tokenType == Scanner.TokenType.LB
          && tokens.get(lastInd).tokenType == Scanner.TokenType.RB){
        index = 4;
        prgm_list(4, lastInd-1);
      }else{
        throw new Error("prog syntax error");
      }
    }else{
      throw new Error("prog syntax error");
    }
  }

  void prgm_list(int leftInd, int rightInd){
    ArrayList<int[]> subTokens = new ArrayList<int[]>(); //use this to collect leftInd and rightInd for all statements in the stmt_list
    int temp = leftInd;
    for (int i = leftInd; i <= rightInd; i++){
      if (tokens.get(i).tokenType == Scanner.TokenType.INT && tokens.get(i+1).tokenType == Scanner.TokenType.ID &&
          tokens.get(i+2).tokenType == Scanner.TokenType.SEMI){
            int[] array = {i, i+2};
            subTokens.add(array);
            i += 2;
      }

      else if(tokens.get(i).tokenType == Scanner.TokenType.VOID || tokens.get(i).tokenType == Scanner.TokenType.INT){
        if (tokens.get(i+1).tokenType == Scanner.TokenType.ID && tokens.get(i+2).tokenType == Scanner.TokenType.LP){
          temp = i;
          int level = 0;
          /* make sure we are in the current statement, that means if we get something like
          "if (){ { {} } }" we would get the index of the outer most brackets*/
          boolean lb = false;
          boolean rb = false;
          for (i = temp; i <= rightInd; i++){
            if (tokens.get(i).tokenType == Scanner.TokenType.LB){
              level += 1; // go to the inner brackets
              lb = true; // make sure there are {} followed after if or while
            }
            if (tokens.get(i).tokenType == Scanner.TokenType.RB){
              level -= 1; // get out of the inner brackets
              rb = true;
            }
            if (tokens.get(i).tokenType == Scanner.TokenType.RB && level == 0){
              int[] array = {temp, i};
              subTokens.add(array);
              break;
            }
          }
          if (lb == false || rb == false){
            throw new Error("prog error");
          }
          if (level != 0)
            throw new Error("missed B");
          }else{
            throw new Error("syntax error");
          }
      }else {
        throw new Error("syntax error");
      }
    }
    for (int[] arr : subTokens){
      tempID = 0;
      TreeMap<String, String[]> local = new
                  TreeMap<String, String[]>();
      if (tokens.get(arr[0]).tokenType == Scanner.TokenType.INT && (arr[1] - arr[0] == 2)){
        var_decl(arr[0], arr[1],true, local);
      }
      else{
        func(arr[0], arr[1],local);
      }
    }
  }

  void ret_type(int leftInd, int rightInd){
    if (!(tokens.get(leftInd).tokenType == Scanner.TokenType.VOID || tokens.get(leftInd).tokenType == Scanner.TokenType.INT)){
      throw new Error("ret_Type Error");
    }
  }

  void ret(int leftInd, int rightInd, TreeMap<String, String[]> local){
    if (leftInd < rightInd){ // otherwise it's empty string
      if (tokens.get(leftInd).tokenType == Scanner.TokenType.RETURN && tokens.get(rightInd).tokenType == Scanner.TokenType.SEMI){
        lookahead = tokens.get(leftInd + 1);
        Node retur = new Node(tokens.get(leftInd));
        index = leftInd + 1;
        Node root = bool(new Node(tokens.get(leftInd+1)), leftInd + 1, rightInd - 1);
        if (lookahead.tokenType != Scanner.TokenType.END)
            throw new Error("syntax error");
        postOrder(root);
        printPostOrder(root, local);
        retur = retur.addRight(retur, root);
        if (root.token.tokenType == Scanner.TokenType.ID){
          if (!local.containsKey(root.token.tokenVal)){
            if (!global.containsKey(root.token.tokenVal))
              throw new Error("undefined variable");
          }
          ThreeAddress threeAddress = new ThreeAddress(ThreeAddress.OpType.RET, new Operand(root.token.tokenVal), null, null);
          //threeAddress.setFlag(isGlobal);
          localThreeAddress.add(threeAddress);
        }
        else{
          String str = "temp" + root.id;
          ThreeAddress threeAddress = new ThreeAddress(ThreeAddress.OpType.RET, new Operand(str), null, null);
          localThreeAddress.add(threeAddress); // x = temp0
        }
        func_decl.addChildList(retur, 0);
      }else{
        throw new Error("ret Error");
      }
    }
  }

  void func(int leftInd, int rightInd, TreeMap<String, String[]> local){
      func_decl = new Node(tokens.get(leftInd + 1));
      param_list = new ArrayList<Node>();
      ret_type(leftInd, leftInd);
      localThreeAddress = new ArrayList<ThreeAddress>();
      Tuple tuple = new Tuple(localThreeAddress, local, func_decl);
      tupleList.add(tuple);
      if (tokens.get(leftInd + 1).tokenType == Scanner.TokenType.ID && tokens.get(leftInd + 2).tokenType == Scanner.TokenType.LP){
        if (global.containsKey(tokens.get(leftInd+1).tokenVal)){
          if (global.get(tokens.get(leftInd+1).tokenVal).equals("INTFUNC"))
            throw new Error("multiple func declarations");
        }else
          global.put(tokens.get(leftInd+1).tokenVal, "INTFUNC");
        if (tokens.get(leftInd + 3).tokenType != Scanner.TokenType.RP){ // a function with parameter
          param(leftInd + 3, leftInd + 4, local);
          int i = 0;
          for (i = leftInd + 5; i < rightInd; i++){
            if (tokens.get(i).tokenType == Scanner.TokenType.RP){
              break;
            }
          }
          param_list(leftInd + 5, i, local);
          Node param_l = new Node(tokens.get(leftInd + 5));
          param_l.setChildren(param_list);
          func_decl.addChildList(param_l, 1);
          if (tokens.get(rightInd - 3).tokenType == Scanner.TokenType.RETURN){ // with return statement
            stmt_list(i + 2, rightInd - 4, local);
            ret(rightInd - 3, rightInd - 1, local);
          }else{
            stmt_list(i + 2, rightInd - 1, local);
          }
        }else{ // functions without parameter
          if (tokens.get(rightInd - 3).tokenType == Scanner.TokenType.RETURN){ // with return statement
            stmt_list(leftInd + 5, rightInd - 4, local);
            ret(rightInd - 3, rightInd - 1, local);
          }else{
            stmt_list(leftInd + 5, rightInd - 1, local);
          }
        }
      }
  }

  /*
  * @param glob: see if it's from global or local
  */
  void var_decl(int leftInd, int rightInd, boolean glob, TreeMap<String, String[]> local){
    if (glob){
      if (!global.containsKey(tokens.get(leftInd+1).tokenVal)){
        global.put(tokens.get(leftInd+1).tokenVal, tokens.get(leftInd).tokenVal);
      }else{
        throw new Error("multiple declaration");
      }
    }else{
      if (!local.containsKey(tokens.get(leftInd+1).tokenVal)){
        local.put(tokens.get(leftInd+1).tokenVal, new String[]{tokens.get(leftInd).tokenVal, null, "false"});
      }else{
        throw new Error("multiple declaration");
      }
    }
  }

  void param(int leftInd, int rightInd, TreeMap<String, String[]> local){
    if ((rightInd - leftInd) == 1){
      if (!(tokens.get(leftInd).tokenType == Scanner.TokenType.INT && tokens.get(rightInd).tokenType == Scanner.TokenType.ID)){
        throw new Error("Param Error");
      }else{
        Node node = new Node(tokens.get(leftInd + 1));
        param_list.add(node);
        local.put(tokens.get(leftInd + 1).tokenVal, new String[]{tokens.get(leftInd).tokenVal, null, "true"});
      }
    }else{
      throw new Error("Param length Error");
    }
  }

  void param_list(int leftInd, int rightInd, TreeMap<String, String[]> local){
    if (tokens.get(leftInd).tokenType == Scanner.TokenType.COMMA){
      param(leftInd + 1, leftInd + 2, local);
      param_list(leftInd + 3, rightInd, local);
    }else if(tokens.get(leftInd).tokenType == Scanner.TokenType.RP){
      // do nothing
    }else{
      throw new Error("Param_list Error");
    }
  }

  /**
   * It represent stmt_list
   * @param leftInd the first tokens in the {}
   * @param rightInd the last tokens in the {}
   * in the example "if (2 < 3) {int x =5;}"
   * leftInd = 7 and rightInd = 11;
   */
  void stmt_list(int leftInd, int rightInd, TreeMap<String, String[]> local){
    ArrayList<int[]> subTokens = new ArrayList<int[]>(); //use this to collect leftInd and rightInd for all statements in the stmt_list
    int temp = leftInd;
    // the for loop interate through what's inside the "{ }"
    for (int i = leftInd; i <= rightInd; i++){
      // first token is INT, so this is an assgiment
      if (tokens.get(i).tokenType == Scanner.TokenType.INT && tokens.get(i+1).tokenType == Scanner.TokenType.ID &&
          tokens.get(i+2).tokenType == Scanner.TokenType.SEMI){ // var_decl
        int[] array = {i, i+2};
        subTokens.add(array);
        if (i + 2 >= rightInd){
          break;
        }else{
          i += 2;
          temp = i;
        }
      }
      else if (tokens.get(i).tokenType == Scanner.TokenType.ID || tokens.get(i).tokenType == Scanner.TokenType.INT){ //assignment
        temp = i;
        boolean semi = false;
        for (i = temp+1; i <= rightInd; i++){
          /*if (tokens.get(i).tokenType == Scanner.TokenType.IF || tokens.get(i).tokenType == Scanner.TokenType.WHILE ||
              tokens.get(i).tokenType == Scanner.TokenType.INT || tokens.get(i).tokenType == Scanner.TokenType.RB)
            throw new Error("missed semi");*/
          if (tokens.get(i).tokenType == Scanner.TokenType.SEMI){
            semi = true;
            int[] array = {temp, i};
            subTokens.add(array);
            // example: void id(){int x = 5; int y = 3;}
            // subTokens would be {(5, 9)(10, 14)}
            break;
          }
        }
        if (semi == false){
          throw new Error("missed semi");
        }
      }
      // if or while
      else if (tokens.get(i).tokenType == Scanner.TokenType.IF || tokens.get(i).tokenType == Scanner.TokenType.WHILE){
        temp = i;
        int level = 0;
        /* make sure we are in the current statement, that means if we get something like
        "if (){ { {} } }" we would get the index of the outer most brackets*/
        boolean lb = false;
        boolean rb = false;
        for (i = temp; i <= rightInd; i++){
          if (tokens.get(i).tokenType == Scanner.TokenType.LB){
            level += 1; // go to the inner brackets
            lb = true; // make sure there are {} followed after if or while
          }
          if (tokens.get(i).tokenType == Scanner.TokenType.RB){
            level -= 1; // get out of the inner brackets
            rb = true;
          }
          if (tokens.get(i).tokenType == Scanner.TokenType.RB && level == 0){
            int[] array = {temp, i};
            subTokens.add(array);
            break;
          }
        }
        if (lb == false || rb == false){
          throw new Error("prog error");
        }
        if (level != 0)
          throw new Error("missed B");
      }else {
        throw new Error("syntax error");
      }
    }
    for (int[] arr : subTokens){
      tempID = 0;
      if (tokens.get(arr[0]).tokenType == Scanner.TokenType.INT && (arr[1] - arr[0] == 2) ){
        var_decl(arr[0], arr[1], false, local);
      }else if(tokens.get(arr[0]).tokenType == Scanner.TokenType.INT || tokens.get(arr[0]).tokenType == Scanner.TokenType.ID){
        assignment(arr[0], arr[1], local);
      }
      else{
        control(arr[0], arr[1], local);
      }
    }
  }

  //assg
  void assignment(int leftInd, int rightInd, TreeMap<String, String[]> local){
    if (!(tokens.get(leftInd).tokenType == Scanner.TokenType.INT && tokens.get(leftInd+1).tokenType == Scanner.TokenType.ID
        && tokens.get(leftInd+2).tokenType == Scanner.TokenType.ASG && tokens.get(rightInd).tokenType == Scanner.TokenType.SEMI)
        && !(tokens.get(leftInd).tokenType == Scanner.TokenType.ID && tokens.get(leftInd+1).tokenType == Scanner.TokenType.ASG &&
            tokens.get(rightInd).tokenType == Scanner.TokenType.SEMI)){
          throw new Error("assg - syntax error");
        }
    Boolean isGlobal = false;
    lookahead = tokens.get(leftInd);
    Scanner.Token idToken = tokens.get(leftInd);
    for (int i = leftInd; i < rightInd; i++){
      if (tokens.get(i).tokenType == Scanner.TokenType.ASG){
        // variable checking
        if (tokens.get(i-2).tokenType == Scanner.TokenType.INT){ //defined a new variable
          if (local.containsKey(tokens.get(i-1).tokenVal)){
            throw new Error("multiple definition");
          }else{
            local.put(tokens.get(i-1).tokenVal,new String[]{tokens.get(leftInd).tokenVal, null, "false"}); // key is x, val is int
          }
        }else{ // assgin value to a defined variable
          if (!local.containsKey(tokens.get(i-1).tokenVal)){
            if (!global.containsKey(tokens.get(i-1).tokenVal)){
              throw new Error("undefined variable");
            }else{
              isGlobal = true;
            }
          }else{
            isGlobal = false;
          }
        }

        idToken = tokens.get(i-1);
        i += 1;
        lookahead = tokens.get(i);
        Node current = new Node(lookahead);
        index = i;
        Node root = bool(current, i, rightInd-1);
        if (lookahead.tokenType != Scanner.TokenType.END)
            throw new Error("syntax error");
        postOrder(root);
        printPostOrder(root, local);
        if (root.token.tokenType == Scanner.TokenType.ID){
          if (!local.containsKey(root.token.tokenVal)){
            if (!global.containsKey(root.token.tokenVal))
              throw new Error("undefined variable");
          }
          ThreeAddress threeAddress = new ThreeAddress(ThreeAddress.OpType.ASSIGN, new Operand(root.token.tokenVal), null, new Operand(idToken.tokenVal));
          threeAddress.setFlag(isGlobal);
          localThreeAddress.add(threeAddress);
        }
        else{
          String str = "temp" + root.id;
          ThreeAddress threeAddress = new ThreeAddress(ThreeAddress.OpType.ASSIGN, new Operand(str), null, new Operand(idToken.tokenVal));
          threeAddress.setFlag(isGlobal);
          localThreeAddress.add(threeAddress); // x = temp0
        }
        break;
      }
    }
  }



  void control(int leftInd, int rightInd, TreeMap<String, String[]> local){
    ArrayList<int[]> subTokens = new ArrayList<int[]>();
    int leftp = -1;
    int rightp = -1;
    int tempRlabel = -1;
    lookahead = tokens.get(leftInd);
    Scanner.Token look = tokens.get(leftInd);
    if (lookahead.tokenType == Scanner.TokenType.WHILE){
      String label = "repeatLabel" + rlabelID;
      localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.LABEL, new Operand(label), null, null));
      tempRlabel = rlabelID;
      rlabelID += 1;
    }
    if (tokens.get(leftInd+1).tokenType == Scanner.TokenType.LP){
      leftp = leftInd+2; // leftp is the index of first element of expr is the if(expr)
    }else{
      throw new Error("missed LP");
    }

    for (int i = leftInd; i < rightInd; i++){
      if (tokens.get(i).tokenType == Scanner.TokenType.LB){
        if (tokens.get(i-1).tokenType != Scanner.TokenType.RP){
          throw new Error("missed RP");
        }
        rightp = i - 2; // index i refers to {, so the last token of the expr is i - 2, as we get (expr){}, we need to skip )
        break;
      }
    }

      lookahead = tokens.get(leftp);
      index = leftp;
      int tempFlabel = flabelID;
      Node root = bool(new Node(lookahead), leftp, rightp); // handles expr
      if (lookahead.tokenType != Scanner.TokenType.END)
          throw new Error("syntax error");
      root.falseLabel = flabelID;
      root.trueLabel = tlabelID;
      postOrder(root);
      printPostOrder(root, local);
      tlabelID += 1;
      flabelID += 1;
      leftInd = rightp + 3; // if(expr){statment}. so rightp is the last index of expr and rightp+3 is the first index of statement
      stmt_list(leftInd, rightInd-1, local);
      if (look.tokenType == Scanner.TokenType.WHILE){
        String label = "GOTO: repeatLabel" + tempRlabel;
        localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.GOTO, null, null, new Operand(label)));
      }
      String label = "falseLabel" + tempFlabel;
      localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.LABEL, new Operand(label), null, null));
  }


  //****** below is the AST ******//

// handles || and &&
/*  Node function(Node current, int leftInd, int rightInd){
    current = ret(current, leftInd, rightInd);
    //while(true)
  }

  Node ret(Node current, int left, int rightInd){
    current = ret(current, leftInd, rightInd);
    while(){

    }
  }

  Node ifCondition(Node current, int leftInd, int rightInd){
    current = bool(current, leftInd, rightInd);
    while(true){
      if (lookahead.tokenType == Scanner.TokenType.IF){
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.IF, "if"), leftInd, rightInd);
        current =
      }
    }
  }*/

  Node bool(Node current, int leftInd, int rightInd){
    current = A(current, leftInd, rightInd);
    while (true){
      if (lookahead.tokenType == Scanner.TokenType.AND){
        //tlabelID += 1;
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.AND, "&&"), leftInd, rightInd);
        current = A(new Node(lookahead), leftInd, rightInd);
        parent = parent.addRight(parent, current);
        current = parent;
        continue;
      }else if(lookahead.tokenType == Scanner.TokenType.OR){
        //flabelID += 1;
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.OR, "||"), leftInd, rightInd);
        current = A(new Node(lookahead), leftInd, rightInd);
        parent = parent.addRight(parent, current);
        current = parent;
        continue;
      }
      return current;
    }
  }

// A handles != and ==
  Node A(Node current, int leftInd, int rightInd){
    current = B(current, leftInd, rightInd);
    while (true) {
      if (lookahead.tokenType == Scanner.TokenType.EQ) {
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.EQ, "=="), leftInd, rightInd);
        current = B(new Node(lookahead), leftInd, rightInd);
        parent = parent.addRight(parent, current);
        current = parent;
        continue;
      } else if (lookahead.tokenType == Scanner.TokenType.NE) {
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.NE, "!="), leftInd, rightInd);
        current = B(new Node(lookahead), leftInd, rightInd);
        parent = parent.addRight(parent, current);
        current = parent;
        continue;
      }
      return current;
    }
  }

// B handles >=, >, <=, <
  Node B(Node current, int leftInd, int rightInd){
    current = E(current, leftInd, rightInd);
    while(true){
      if (lookahead.tokenType == Scanner.TokenType.LT){
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.LT, "<"), leftInd, rightInd);
        current = E(new Node(lookahead), leftInd, rightInd);
        parent = parent.addRight(parent, current);
        current = parent;
        continue;
      }else if (lookahead.tokenType == Scanner.TokenType.GT) {
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.GT, ">"), leftInd, rightInd);
        current = E(new Node(lookahead), leftInd, rightInd);
        parent = parent.addRight(parent, current);
        current = parent;
        continue;
      }else if (lookahead.tokenType == Scanner.TokenType.LTE) {
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.LTE, "<="), leftInd, rightInd);
        current = E(new Node(lookahead), leftInd, rightInd);
        parent = parent.addRight(parent, current);
        current = parent;
        continue;
      }else if (lookahead.tokenType == Scanner.TokenType.GTE) {
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.GTE, ">="), leftInd, rightInd);
        current = E(new Node(lookahead), leftInd, rightInd);
        parent = parent.addRight(parent, current);
        current = parent;
        continue;
      }
      return current;
    }
  }


  /**
   * It represent E
   * @return the last temp ID
   */
  Node E(Node current, int leftInd, int rightInd){
    current = T(current, leftInd, rightInd); //goes to T
    while (true){ //while loop represent E'
      if (lookahead.tokenType == Scanner.TokenType.PLUS){
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.PLUS, "+"), leftInd, rightInd);
        current = T(new Node(lookahead), leftInd, rightInd);
        parent = parent.addRight(parent, current);
        current = parent;
        continue;
      }
      else if (lookahead.tokenType == Scanner.TokenType.MINUS){
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.MINUS, "-"), leftInd, rightInd);
        current = T(new Node(lookahead), leftInd, rightInd);
        parent = parent.addRight(parent, current);
        current = parent;
        continue;
      }
      return current;
    }
  }

  /**
   * It represent T
   * @return the last temp ID
   */
  Node T(Node current, int leftInd, int rightInd) {
    current = F(current, leftInd, rightInd); //goes to F
    while (true) { //represent T
      if (lookahead.tokenType == Scanner.TokenType.MUL) {
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.MUL, "*"), leftInd, rightInd);
        current = F(new Node(lookahead), leftInd, rightInd);
        parent = parent.addRight(parent, current);
        current = parent;
        continue;
      } else if (lookahead.tokenType == Scanner.TokenType.DIV) {
        Node parent = new Node(lookahead);
        parent = parent.addLeft(parent, current);
        match(scan.new Token(Scanner.TokenType.DIV, "/"), leftInd, rightInd);
        current = F(new Node(lookahead), leftInd, rightInd);
        parent = parent.addRight(parent, current);
        current = parent;
        continue;
      }
      return current;
    }
  }

  /**
   * It represent F
   * @return the last temp ID
   */
  Node F(Node current, int leftInd, int rightInd){
    if (lookahead.tokenType == Scanner.TokenType.LP){
      match(scan.new Token(Scanner.TokenType.LP, "("), leftInd, rightInd);
      current = bool(new Node(lookahead), leftInd, rightInd);
      if (lookahead.tokenType == Scanner.TokenType.RP){
        match(scan.new Token(Scanner.TokenType.RP, ")"), leftInd, rightInd);
        return current;
      }
      else
        throw new Error("syntax error");
    }
    else if (lookahead.tokenType == Scanner.TokenType.NUM){ //NUM
      match(lookahead, leftInd, rightInd);
      return current;
    }else if(lookahead.tokenType == Scanner.TokenType.ID){ // ID
      match(lookahead, leftInd, rightInd);
      if (lookahead.tokenType == Scanner.TokenType.LP){ // function call
        Node func_call = new Node(lookahead);
        argu_list = new ArrayList <Node>();
        func_call.setChildren(argu_list);
        func_call.setCall();
        match(lookahead, leftInd, rightInd);
        if (lookahead.tokenType == Scanner.TokenType.RP){
          return func_call;
        }
        int i = 0;
        int j = 0;
        for (i = index; i < rightInd; i++){
            if (tokens.get(i).tokenType == Scanner.TokenType.COMMA){
              break;
            }
        }
        for (j = index; j < rightInd; j++){
            if (tokens.get(j).tokenType == Scanner.TokenType.RP){
              break;
            }
        }
        arg(index, i-1);
        if (tokens.get(i-1).tokenType == Scanner.TokenType.COMMA)
          arg_list(i-1, j);
        return func_call;
      }else{
        return current;
      }
    }
    else throw new Error("syntax error");
  }

  void arg(int leftInd, int rightInd){
    lookahead = tokens.get(leftInd);
    Node current = bool(new Node(lookahead), leftInd, rightInd);
    argu_list.add(current);
  }

  void arg_list(int leftInd, int rightInd){
    if (leftInd < rightInd){
      if (tokens.get(leftInd).tokenType == Scanner.TokenType.COMMA){
        int left = leftInd + 1;
        for(int i = left; i < rightInd; i++){
          if (tokens.get(i).tokenType == Scanner.TokenType.COMMA){
            arg(left, i-1);
          }
        }
      }else{
        throw new Error("Arg Error");
      }
    }
  }

  /**
   * It represent match
   * @param t the token matched
   */
  void match(Scanner.Token t, int leftInd, int rightInd){
    if (lookahead.tokenVal.equals(t.tokenVal)){
      index++;
      if (index <= rightInd){
        lookahead = tokens.get(index);
      }
      else
          lookahead = scan.new Token(Scanner.TokenType.END, "END");
    }
    else{
      throw new Error("syntax error");
    }
  }

  public void postOrder(Node node){
      if (node == null){
        return;
      }
      if (node.token.tokenType == Scanner.TokenType.OR){
        Node leftNode = node.leftNode(node);
        leftNode.setFlag();

        // from book
        node.left.trueLabel = node.trueLabel;
        flabelID += 1;
        node.left.falseLabel = flabelID;
        node.right.trueLabel = node.trueLabel;
        node.right.falseLabel = node.falseLabel;
      }else if(node.token.tokenType == Scanner.TokenType.AND){
        tlabelID += 1;
        node.left.trueLabel = tlabelID;
        node.left.falseLabel = node.falseLabel;
        node.right.trueLabel = node.trueLabel;
        node.right.falseLabel = node.falseLabel;
      }
      postOrder(node.left);
      postOrder(node.right);
      if (node.token.tokenType == Scanner.TokenType.NUM || node.token.tokenType == Scanner.TokenType.PLUS ||
          node.token.tokenType == Scanner.TokenType.MINUS || node.token.tokenType == Scanner.TokenType.MUL ||
          node.token.tokenType == Scanner.TokenType.DIV){
        node.id = tempID;
        tempID += 1;
      }else if(node.token.tokenType == Scanner.TokenType.ID && node.getCall()){
        for (Node child : node.getChildren()){
          postOrder(child);
          node.id = tempID;
          tempID += 1;
        }
      }
  }

  // start from < <= > >=
  /* @param para: determine if the call is from a func_call node
  */
  public void printPostOrder(Node node, TreeMap<String, String[]> local){
      //tupleList.get(0).set
      if (node == null){
        return;
      }
      if (node.token.tokenType == Scanner.TokenType.AND){
        printPostOrder(node.left, local);
        printPostOrder(node.right, local);
      }else if(node.token.tokenType == Scanner.TokenType.OR){
        printPostOrder(node.left, local);
        printPostOrder(node.right, local);
      }else{
        printPostOrder(node.left, local);
        printPostOrder(node.right, local);
        if (node.token.tokenType == Scanner.TokenType.NUM){
          String str = "temp" + node.id;
          int constant = Integer.parseInt(node.token.tokenVal);
          localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.ASSIGN, new Operand(constant), null, new Operand(str)));
          local.put(str, new String[]{null, null, "false"});
        }else{
          if (node.left != null){
            if (node.token.tokenType == Scanner.TokenType.PLUS || node.token.tokenType == Scanner.TokenType.MINUS ||
                node.token.tokenType == Scanner.TokenType.MUL || node.token.tokenType == Scanner.TokenType.DIV){
              if (node.left.id != -1 && node.right.id != -1){
                String dest = "temp" + node.id;
                String str1 = "temp" + node.left.id;
                String str2 = "temp" + node.right.id;
                local.put(dest, new String[]{null, null, "false"});
                local.put(str1, new String[]{null, null, "false"});
                local.put(str2, new String[]{null, null, "false"});
                if (node.token.tokenType == Scanner.TokenType.PLUS)
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.PLUS, new Operand(str1), new Operand(str2), new Operand(dest)));
                else if(node.token.tokenType == Scanner.TokenType.MINUS)
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.MINUS, new Operand(str1), new Operand(str2), new Operand(dest)));
                else if(node.token.tokenType == Scanner.TokenType.MUL)
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.MUL, new Operand(str1), new Operand(str2), new Operand(dest)));
                else
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.DIV, new Operand(str1), new Operand(str2), new Operand(dest)));

              }else if(node.left.id == -1 && node.right.id != -1){
                if (!local.containsKey(node.left.token.tokenVal)){
                  if (!global.containsKey(node.left.token.tokenVal))
                    throw new Error("undefined variable");
                }
                String dest = "temp" + node.id;
                String str1 = null;
                if (node.left.getCall() == false){ // it's not a function
                  str1 = node.left.token.tokenVal;
                }
                else{
                  for (int i = 0;  i < node.getChildren().size(); i++){
                    printPostOrder(node.left.getChildren().get(i), local);
                    localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.PARAM, new Operand(Integer.toString(node.left.id-1)), null, null));
                  }
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.FUNC_CALL, new Operand(Integer.toString(node.left.getChildren().size())), null, null));
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.RETRIEVE, new Operand(Integer.toString(node.left.id)), null, null));
                  str1 = "temp" + node.left.id;
                }
                  String str2 = "temp" + node.right.id;
                  local.put(dest, new String[]{null, null, "false"});
                  if (!global.containsKey(str1))
                    local.put(str1, new String[]{null, null, "false"});
                  local.put(str2, new String[]{null, null, "false"});
                  if (node.token.tokenType == Scanner.TokenType.PLUS)
                    localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.PLUS, new Operand(str1), new Operand(str2), new Operand(dest)));
                  else if(node.token.tokenType == Scanner.TokenType.MINUS)
                    localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.MINUS, new Operand(str1), new Operand(str2), new Operand(dest)));
                  else if(node.token.tokenType == Scanner.TokenType.MUL)
                    localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.MUL, new Operand(str1), new Operand(str2), new Operand(dest)));
                  else
                    localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.DIV, new Operand(str1), new Operand(str2), new Operand(dest)));
              }
              else if(node.left.id != -1 && node.right.id == -1){
                if (!local.containsKey(node.right.token.tokenVal)){
                  if (!global.containsKey(node.right.token.tokenVal))
                    throw new Error("undefined variable");
                }
                String dest = "temp" + node.id;
                String str1 = "temp" + node.left.id;
                String str2 = null;
                if (node.right.getCall() == false){
                  str2 = node.right.token.tokenVal;
                }else{
                  for (int i = 0;  i < node.right.getChildren().size(); i++){
                    printPostOrder(node.right.getChildren().get(i), local);
                    localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.PARAM, new Operand(Integer.toString(node.right.id-1)), null, null));
                  }
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.FUNC_CALL, new Operand(Integer.toString(node.right.getChildren().size())), null, null));
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.RETRIEVE, new Operand(Integer.toString(node.right.id)), null, null));
                  str2 = "temp" + node.right.id;
                }
                //String str2 = node.right.token.tokenVal;
                local.put(dest, new String[]{null, null, "false"});
                local.put(str1, new String[]{null, null, "false"});
                if (!global.containsKey(str2))
                  local.put(str2, new String[]{null, null, "false"});
                if (node.token.tokenType == Scanner.TokenType.PLUS)
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.PLUS, new Operand(str1), new Operand(str2), new Operand(dest)));
                else if(node.token.tokenType == Scanner.TokenType.MINUS)
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.MINUS, new Operand(str1), new Operand(str2), new Operand(dest)));
                else if(node.token.tokenType == Scanner.TokenType.MUL)
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.MUL, new Operand(str1), new Operand(str2), new Operand(dest)));
                else
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.DIV, new Operand(str1), new Operand(str2), new Operand(dest)));
              }else if(node.left.id == -1 && node.right.id == -1){
                if (!(local.containsKey(node.left.token.tokenVal) && local.containsKey(node.right.token.tokenVal))){
                  if (!(global.containsKey(node.left.token.tokenVal) && global.containsKey(node.right.token.tokenVal)))
                    throw new Error("undefined variable");
                }
                String dest = "temp" + node.id;
                String str1 = null;
                if (node.left.getCall() == false){ // it's not a function
                  str1 = node.left.token.tokenVal;
                }
                else{
                  for (int i = 0;  i < node.getChildren().size(); i++){
                    printPostOrder(node.left.getChildren().get(i), local);
                    localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.PARAM, new Operand(Integer.toString(node.left.id-1)), null, null));
                  }
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.FUNC_CALL, new Operand(Integer.toString(node.left.getChildren().size())), null, null));
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.RETRIEVE, new Operand(Integer.toString(node.left.id)), null, null));
                  str1 = "temp" + node.left.id;
                }
                String str2 = null;
                if (node.right.getCall() == false){
                  str2 = node.right.token.tokenVal;
                }else{
                  for (int i = 0;  i < node.right.getChildren().size(); i++){
                    printPostOrder(node.right.getChildren().get(i), local);
                    localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.PARAM, new Operand(Integer.toString(node.right.id-1)), null, null));
                  }
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.FUNC_CALL, new Operand(Integer.toString(node.right.getChildren().size())), null, null));
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.RETRIEVE, new Operand(Integer.toString(node.right.id)), null, null));
                  str2 = "temp" + node.right.id;
                }
                local.put(dest, new String[]{null, null, "false"});
                if (!global.containsKey(str1))
                  local.put(str1, new String[]{null, null, "false"});
                if (!global.containsKey(str2))
                  local.put(str2, new String[]{null, null, "false"});
                if (node.token.tokenType == Scanner.TokenType.PLUS)
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.PLUS, new Operand(str1), new Operand(str2), new Operand(dest)));
                else if(node.token.tokenType == Scanner.TokenType.MINUS)
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.MINUS, new Operand(str1), new Operand(str2), new Operand(dest)));
                else if(node.token.tokenType == Scanner.TokenType.MUL)
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.MUL, new Operand(str1), new Operand(str2), new Operand(dest)));
                else
                  localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.DIV, new Operand(str1), new Operand(str2), new Operand(dest)));
              }
            }
            else if (node.token.tokenType == Scanner.TokenType.NE || node.token.tokenType == Scanner.TokenType.EQ ||
                    node.token.tokenType == Scanner.TokenType.GTE || node.token.tokenType == Scanner.TokenType.LTE ||
                    node.token.tokenType == Scanner.TokenType.GT || node.token.tokenType == Scanner.TokenType.LT){
              ThreeAddress.OpType op = null;
              if (node.token.tokenType == Scanner.TokenType.NE){
                op = ThreeAddress.OpType.NE;
              }else if (node.token.tokenType == Scanner.TokenType.EQ){
                op = ThreeAddress.OpType.EQ;
              }else if (node.token.tokenType == Scanner.TokenType.GTE){
                op = ThreeAddress.OpType.GTE;
              }else if (node.token.tokenType == Scanner.TokenType.LTE){
                op = ThreeAddress.OpType.LTE;
              }else if (node.token.tokenType == Scanner.TokenType.GT){
                op = ThreeAddress.OpType.GT;
              }else if (node.token.tokenType == Scanner.TokenType.LT){
                op = ThreeAddress.OpType.LT;
              }
              if (node.left.id == -1 && node.right.id != -1){
                String str1 = node.left.token.tokenVal;
                String str2 = "temp" + node.right.id;
                int dest = node.trueLabel;
                localThreeAddress.add(new ThreeAddress(op, new Operand(str1), new Operand(str2), new Operand(dest)));
              }
              else if(node.left.id != -1 && node.right.id == -1){
                String str1 = "temp" + node.left.id;
                String str2 = node.right.token.tokenVal;
                int dest = node.trueLabel;
                localThreeAddress.add(new ThreeAddress(op, new Operand(str1), new Operand(str2), new Operand(dest)));
              }
              else if(node.left.id != -1 && node.right.id != -1){
                String str1 = "temp" + node.left.id;
                String str2 = "temp" + node.right.id;
                int dest = node.trueLabel;
                localThreeAddress.add(new ThreeAddress(op, new Operand(str1), new Operand(str2), new Operand(dest)));
              }
              else if(node.left.id == -1 && node.right.id == -1){
                String str1 = node.left.token.tokenVal;
                String str2 = node.right.token.tokenVal;
                int dest = node.trueLabel;
                localThreeAddress.add(new ThreeAddress(op, new Operand(str1), new Operand(str2), new Operand(dest)));
              }
              int label = node.falseLabel;
              localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.GOTO, null, null, new Operand(label)));
              if (node.flag){
                String labelStr = "trueLabel" + node.trueLabel;
                localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.LABEL, new Operand(labelStr), null, null));
              }
              else{
                String labelStr = "falseLabel" + node.falseLabel;
                localThreeAddress.add(new ThreeAddress(ThreeAddress.OpType.LABEL, new Operand(labelStr), null, null));
              }
            }
          }
        }
    }
  }
}
