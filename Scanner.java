// CSc 453, PA 3, Fall 2019
// Author: Wentao Zhou, Junyu Liu
// The program's behaviour is take strings as input and extract tokens from
// them.

import java.util.ArrayList;

public class Scanner{
  enum TokenType{
      NUM, ID, PLUS, MINUS, MUL, DIV, NE, EQ, LTE, GTE, LT, GT, LP, RP, ASG,
      INT, SEMI, LB, RB, COMMA, IF, WHILE, VOID, OR, AND, PUBLIC, PRIVATE, CLASS, RETURN, END
  }//
  // extra END tokentype, we only use it to capture extra parenthesis.

  class Token{
    TokenType tokenType;
    String tokenVal;
    public Token(TokenType tokenType, String tokenVal){
      this.tokenType = tokenType;
      this.tokenVal = tokenVal;
    }

    public String toString(){
      return "|" + this.tokenType + ": " + this.tokenVal + "|";
    }
  }

  public Token extractToken(StringBuilder stream){
      String tokenVal = stream.toString();
      int index = 0;
      char peek = tokenVal.charAt(index);
      //handle PLUS
      if (peek == '+'){
        Token token = new Token(TokenType.PLUS, "+");
        return token;
      }
      //handle "-"
      else if (peek == '-'){
        Token token = new Token(TokenType.MINUS, "-");
        return token;
      }
      //handle "*"
      else if (peek == '*'){
        Token token = new Token(TokenType.MUL, "*");
        return token;
      }
      //handle "/"
      else if (peek == '/'){
        Token token = new Token(TokenType.DIV, "/");
        return token;
      }
      else if(peek == '!'){ //!=
        Token token = new Token(TokenType.NE, "!=");
        if (index < tokenVal.length()-1){
          if (tokenVal.charAt(index+1) == '=')
            token = new Token(TokenType.NE, "!=");
          else
            throw new Error("semantic error");
        }else{
          throw new Error("semantic error");
        }
        return token;
      }
      else if(peek == '='){ // '=' and '=='
        Token token = new Token(TokenType.EQ, "==");
        if (index < tokenVal.length()-1){
          if (tokenVal.charAt(index+1) == '=')
            token = new Token(TokenType.EQ, "==");
          else
            token = new Token(TokenType.ASG, "=");
        }else
            token = new Token(TokenType.ASG, "=");
        return token;
      }
      //handle "<" and "<="
      else if (peek == '<'){
        Token token = new Token(TokenType.LTE, "<=");
        if (index < tokenVal.length()-1){
          if (tokenVal.charAt(index+1) == '='){
            token = new Token(TokenType.LTE, "<=");
          }else{
            token = new Token(TokenType.LT, "<");
          }
        }else{token = new Token(TokenType.GT, "<");}
        return token;
      }
      // handle ">" and ">="
      else if (peek == '>'){
        Token token = new Token(TokenType.GTE, ">=");
        if (index < tokenVal.length()-1){
          if (tokenVal.charAt(index+1) == '='){
            token = new Token(TokenType.GTE, ">=");
          }else{
            token = new Token(TokenType.GT, ">");
          }
        }else{token = new Token(TokenType.GT, ">");}
        return token;
      }
      else if (peek == '('){
        Token token = new Token(TokenType.LP, "(");
        return token;
      }
      else if (peek == ')'){
        Token token = new Token(TokenType.RP, ")");
        return token;
      }
      else if (peek == '='){
        Token token = new Token(TokenType.ASG, "=");
        return token;
      }
      else if (peek == ';'){
        Token token = new Token(TokenType.SEMI, ";");
        return token;
      }
      else if (peek == '{'){
        Token token = new Token(TokenType.LB, "{");
        return token;
      }
      else if (peek == '}'){
        Token token = new Token(TokenType.RB, "}");
        return token;
      }
      else if (peek == ','){
        Token token = new Token(TokenType.COMMA, ",");
        return token;
      }
      //handle number
      else if (Character.isDigit(peek)){
        ArrayList<Character> buffer = new ArrayList<Character>();
        while(index < tokenVal.length() && Character.isDigit(peek)){
          buffer.add(peek);
          index ++ ;
          if (index < tokenVal.length())
            peek = tokenVal.charAt(index);
        }
        StringBuilder builder = new StringBuilder(buffer.size());
        for (Character ch : buffer){
          builder.append(ch);
        }
        tokenVal = builder.toString();
        Token token = new Token(TokenType.NUM, tokenVal);
        return token;
      }else if(Character.isLetter(peek)){
        if (peek == 'w'){ // while
          if (index < tokenVal.length()-4){
            if (tokenVal.charAt(index+1) == 'h' && tokenVal.charAt(index+2) == 'i' &&
                tokenVal.charAt(index+3) == 'l' && tokenVal.charAt(index+4) == 'e'){
                Token token = new Token(TokenType.WHILE, "while");
                return token;
            }
          }
        }
        else if(peek == 'v'){ //void
          if (index < tokenVal.length()-3){
            if (tokenVal.charAt(index+1) == 'o' && tokenVal.charAt(index+2) == 'i'
                && tokenVal.charAt(index+3) == 'd'){
                Token token = new Token(TokenType.VOID, "void");
                return token;
            }
          }
        }
        else if (peek == 'i'){// int and if
          if (index < tokenVal.length()-2){
            if (tokenVal.charAt(index+1) == 'n' && tokenVal.charAt(index+2) == 't'){
              Token token = new Token(TokenType.INT, "int");
              return token;
            }
            else if(tokenVal.charAt(index+1) == 'f'){
              Token token = new Token(TokenType.IF, "if");
              return token;
            }
          }
        }
        else if(peek == 'p'){
          if (index < tokenVal.length()-6){
            if (tokenVal.charAt(index+1) == 'r' && tokenVal.charAt(index+2) == 'i' &&
                tokenVal.charAt(index+3) == 'v' && tokenVal.charAt(index+4) == 'a' &&
                tokenVal.charAt(index+5) == 't' && tokenVal.charAt(index+6) == 'e'){
                  Token token = new Token(TokenType.PRIVATE, "private");
                  return token;
            }else if(tokenVal.charAt(index+1) == 'u' && tokenVal.charAt(index+2) == 'b' &&
                    tokenVal.charAt(index+3) == 'l' && tokenVal.charAt(index+4) == 'i' &&
                    tokenVal.charAt(index+5) == 'c'){
                  Token token = new Token(TokenType.PUBLIC, "public");
                  return token;
            }
          }
        }else if(peek == 'c'){
            if (index < tokenVal.length()-4){
              if (tokenVal.charAt(index+1) == 'l' && tokenVal.charAt(index+2) == 'a' &&
                  tokenVal.charAt(index+3) == 's' && tokenVal.charAt(index+4) == 's'){
                    Token token = new Token(TokenType.CLASS, "class");
                    return token;
              }
            }
        }else if(peek == 'r'){
            if (index < tokenVal.length()-5){
              if (tokenVal.charAt(index + 1) == 'e' && tokenVal.charAt(index+2) == 't' &&
                  tokenVal.charAt(index + 3) == 'u' && tokenVal.charAt(index+4) == 'r' &&
                  tokenVal.charAt(index + 5) == 'n'){
                    Token token = new Token(TokenType.RETURN, "return");
                    return token;
                  }
            }
        }
        ArrayList<Character> buffer = new ArrayList<Character>();
        while(index < tokenVal.length() && Character.isLetterOrDigit(peek)){
          buffer.add(peek);
          index ++ ;
          if (index < tokenVal.length())
            peek = tokenVal.charAt(index);
        }
        StringBuilder builder = new StringBuilder(buffer.size());
        for (Character ch : buffer){
          builder.append(ch);
        }
        tokenVal = builder.toString();
        Token token = new Token(TokenType.ID, tokenVal);
        return token;
      }else if(peek == '|'){
        Token token = new Token(TokenType.OR, "||");
        if (index < tokenVal.length()-1){
          if (tokenVal.charAt(index+1) == '|'){
            token = new Token(TokenType.OR, "||");
          }else{
            throw new Error("semantic error");
          }
        }else{
          throw new Error("semantic error");
        }
        return token;
      }else if(peek == '&'){
        Token token = new Token(TokenType.AND, "&&");
        if (index < tokenVal.length()-1){
          if (tokenVal.charAt(index+1) == '&'){
            token = new Token(TokenType.AND, "&&");
          }else{
            throw new Error("semantic error");
          }
        }else{
          throw new Error("semantic error");
        }
        //System.out.println();
        return token;
      }else{//error condition
        throw new Error("semantic error");
      }
  }

  public ArrayList<Token> extractTokens(String arg){
    ArrayList<Token> result = new ArrayList<Token>();
    int index = 0;
    boolean extract = true;
    while(index < arg.length()){
      // skip white space
      if (arg.charAt(index) == ' ' || arg.charAt(index)=='\t' || arg.charAt(index)=='\n'){
        index++;
      }else{
        StringBuilder sb = new StringBuilder();
        sb.append(arg.substring(index));
        Token nextToken = extractToken(sb);
        if (nextToken==null){
          return null;
        }else{
          index += nextToken.tokenVal.length();
          result.add(nextToken);
        }
      }
    }
    return result;
  }

}
