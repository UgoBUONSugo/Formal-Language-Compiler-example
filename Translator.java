import java.io.*;

public class Translator {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;
    
    SymbolTable st = new SymbolTable();
    CodeGenerator code = new CodeGenerator();
    int count=0;

    public Translator(Lexer l, BufferedReader br) {
        lex = l;
        pbr = br;
        move();
    }

    void move() {
      look = lex.lexical_scan(pbr);
      System.out.println("token = " + look);
    }

    void error(String s) { 
      throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) {
      if (look.tag == t) {
        if (look.tag != Tag.EOF) move();
      }   
      else error("syntax error");
    }

    public void prog() { 
      if(look.tag == Tag.ASSIGN || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.FOR || look.tag == Tag.IF || look.tag == '{'){       
        statlist();
        match(Tag.EOF);
        try {
        	code.toJasmin();
        }
        catch(java.io.IOException e) {
        	System.out.println("IO error\n");
        };
      }
      else{
        error("prog");
      }
    }

    private void statlist() { 
      if(look.tag == Tag.ASSIGN || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.FOR || look.tag == Tag.IF || look.tag == '{'){
        stat();
        statlistp();
      }
      else{
        error("statlist");
      }
    }


  private void statlistp() {
    if(look.tag == ';'){
      match(';');
      stat();
      statlistp();
    }
    else if(look.tag == Tag.EOF || look.tag == '}'){}
    else{
      error("statlistp");
    }
  }

  private void stat() {
    if(look.tag == Tag.ASSIGN){
      match(Tag.ASSIGN);
      assignlist(); 
    }
    else if(look.tag == Tag.PRINT){
      match(Tag.PRINT);
      match('(');
      exprlist(true);
      match(')');
    }
    else if(look.tag == Tag.READ){
      match(Tag.READ);
      match('(');
      idlist(false);
      match(')');
    }
    else if(look.tag == Tag.FOR){     
      match(Tag.FOR);
      match('(');
      stat1();
    }
    else if(look.tag == Tag.IF){
      match(Tag.IF); 
      match('(');
      int if_body = code.newLabel();
      int if_false = code.newLabel();
      bexpr(if_body);   
      code.emit(OpCode.GOto, if_false);
      match(')');
      code.emitLabel(if_body);
      stat();
      stat2(if_false);
    }
    else if(look.tag == '{'){
      match('{');
      statlist();
      match('}');
    }
    else{
      error("stat");
    }
  }

  private void stat1(){
    if(look.tag == Tag.ID){ 
        int idAddr = st.lookupAddress(((Word)look).lexeme);
        if(idAddr == -1){
          idAddr = count;
          st.insert(((Word)look).lexeme, count++);
        }
        match(Tag.ID);
        match(Tag.INIT); 
        expr();
        code.emit(OpCode.istore, idAddr);
        match(';');

        int for_end = code.newLabel();
        int for_body = code.newLabel();
        int for_start = code.newLabel();

        code.emitLabel(for_start);
        bexpr(for_body);  
        code.emit(OpCode.GOto, for_end);

        match(')');
        match(Tag.DO);
        code.emitLabel(for_body);
        stat();
        code.emit(OpCode.GOto, for_start);
        code.emitLabel(for_end);
    }
    else if(look.tag == Tag.RELOP){
        int for_end = code.newLabel();
        int for_body = code.newLabel();
        int for_start = code.newLabel();

        code.emitLabel(for_start);
        bexpr(for_body); 
        code.emit(OpCode.GOto, for_end);

        match(')');
        match(Tag.DO);
        code.emitLabel(for_body);
        stat();
        code.emit(OpCode.GOto, for_start);
        code.emitLabel(for_end);
    }
    else{
      error("stat1");
    }
  }

  private void stat2(int if_false){
    if(look.tag == Tag.ELSE){
      match(Tag.ELSE);
      int else_end = code.newLabel();
      code.emit(OpCode.GOto, else_end);
      code.emitLabel(if_false);
      stat();
      match(Tag.END);
      code.emitLabel(else_end);
    }
    else if(look.tag == Tag.END){
      match(Tag.END);
      code.emitLabel(if_false);
    }
    else{
      error("stat2");
    }
  }

  private void assignlist() {
    if(look.tag == '['){
      match('[');
      expr();
      match(Tag.TO);
      idlist(true);
      match(']');
      assignlistp();
    }
    else{
      error("assignlist");
    }
  }

  private void assignlistp() {
    if(look.tag == '['){
      match('[');
      expr();
      match(Tag.TO);
      idlist(true);
      match(']');
      assignlistp();
    }
    else if(look.tag == ';' || look.tag == '}' || look.tag == Tag.EOF || look.tag == Tag.ELSE || look.tag == Tag.END){}
    else{
      error("assignlistp");
    }
  }

  private void idlist(boolean assign) {
    int idAddr = 0; 
    int read_id_addr = 0;
    if(assign){
      if(look.tag == Tag.ID){
        idAddr = st.lookupAddress(((Word)look).getLexeme());
        if(idAddr == -1){
          idAddr = count;
          st.insert(((Word)look).getLexeme(), count++);
        }
        code.emit(OpCode.istore, idAddr);
        match(Tag.ID);
        idlistp(true, idAddr);
      }
      else{
        error("idlist");
      }
    }

    else{
      if(look.tag == Tag.ID){
        read_id_addr = st.lookupAddress(((Word)look).getLexeme());
        if(read_id_addr == -1){
          read_id_addr = count;
          st.insert(((Word)look).getLexeme(), count++);
        }
        match(Tag.ID);
        code.emit(OpCode.invokestatic, 0);
        code.emit(OpCode.istore, read_id_addr);
        idlistp(false, -1);
      }
      else{
        error("idlist");
      }
    }
  }

  private void idlistp(boolean assign, int loadAddr) {
    int read_id_addr = 0;
    int idAddr = 0;
    if(look.tag == ','){  
      match(',');
      if(assign){
        if(look.tag == Tag.ID){
          idAddr = st.lookupAddress(((Word)look).getLexeme());
          if(idAddr == -1){
            idAddr = count;
            st.insert(((Word)look).getLexeme(), count++);
          }
          code.emit(OpCode.iload, loadAddr);
          code.emit(OpCode.istore, idAddr);
          match(Tag.ID);
          idlistp(true, loadAddr);
        }
        else{
          error("idlistp");
        }
      }

      else{
        if(look.tag == Tag.ID){
          read_id_addr = st.lookupAddress(((Word)look).getLexeme());
          if(read_id_addr == -1){
            read_id_addr = count;
            st.insert(((Word)look).lexeme, count++);
          }
          match(Tag.ID);
          code.emit(OpCode.invokestatic, 0);
          code.emit(OpCode.istore, read_id_addr);
          idlistp(false, -1);
        }
        else{
         error("idlistp");
        }
      }
    }
    else if(look.tag == ']' || look.tag == ')'){}
    else{
      error("idlistp");
    }
  }

  private void bexpr(int label) {
    switch(look.tag){
    case Tag.RELOP:
        if(((Word)look).lexeme == "<"){
          match(Tag.RELOP);
          expr();
          expr();
          code.emit(OpCode.if_icmplt, label);
        }
        else if(((Word)look).lexeme == ">"){
          match(Tag.RELOP);
          expr();
          expr();
          code.emit(OpCode.if_icmpgt, label);
        }
        else if(((Word)look).lexeme == "<="){
          match(Tag.RELOP);
          expr();
          expr();
          code.emit(OpCode.if_icmple, label);
        }
        else if(((Word)look).lexeme == ">="){
          match(Tag.RELOP);
          expr();
          expr();
          code.emit(OpCode.if_icmpge, label);
        }
        else if(((Word)look).lexeme == "=="){
          match(Tag.RELOP);
          expr();
          expr();
          code.emit(OpCode.if_icmpeq, label);
        }
        else{
          match(Tag.RELOP);
          expr();
          expr();
          code.emit(OpCode.if_icmpne, label);
        }
    break;
    default:
      error("bexpr");
    }
  }

  private void expr() {
    switch(look.tag){
      case '+':
        match('+');
        match('(');
        exprlist(false);
        code.emit(OpCode.iadd);
        match(')');
        break;

      case '-':
        match('-');
        expr();
        expr();
        code.emit(OpCode.isub);
        break;

      case '*':
        match('*');
        match('(');
        exprlist(false);
        code.emit(OpCode.imul);
        match(')');
        break;

      case '/':
        match('/');
        expr();
        expr();
        code.emit(OpCode.idiv);
        break;

      case Tag.NUM:
        code.emit(OpCode.ldc, ((NumberTok)look).getNumber());
        match(Tag.NUM);
        break;

      case Tag.ID:
        int id_addr = st.lookupAddress(((Word)look).lexeme);
        if (id_addr == -1) {
          error("No ID declared as " + ((Word)look).lexeme);
        }
        code.emit(OpCode.iload, id_addr);
        match(Tag.ID);
        break;

      default:
        error("expr");
        break;
    }
  }

  private void exprlist(boolean print) {
    if(look.tag == '+' || look.tag == '-' || look.tag == '*' || look.tag == '/' || look.tag == Tag.NUM || look.tag == Tag.ID){
      if(print){
        expr();
        code.emit(OpCode.invokestatic, 1);
        exprlistp(true);
      }
      else{
        expr();
        exprlistp(false);
      }
    }
    else{
      error("exprlist");
    }
  }

  private void exprlistp(boolean print) {
    if(look.tag == ','){
      match(',');
      if(print){
        expr();
        code.emit(OpCode.invokestatic, 1);
        exprlistp(true);
      }
      else{
        expr();
        exprlistp(false);
      }
    }
    else if(look.tag == ')'){}
    else{
      error("exprlistp");
    }
  }

  public static void main(String[] args) {
    Lexer lex = new Lexer();
    String path = "test.lft";

    try {
      BufferedReader br = new BufferedReader(new FileReader(path));
      Translator t = new Translator(lex, br);

      t.prog();

      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}