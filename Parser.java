import java.io.*;
public class Parser {
	private Lexer lex;
	private BufferedReader pbr;
	private Token look;
	public Parser(Lexer l, BufferedReader br) {
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

	public void start() {  
		statlist();
		match(Tag.EOF);
	}

	private void statlist() { 
		stat();
		statlistp();
	}


	private void statlistp() {
		if(look.tag == ';'){
			match(';');
			stat();
			statlistp();
		}
		else{}
	}

	private void stat() {
		if(look.tag == Tag.ASSIGN){
			match(Tag.ASSIGN);
			assignlist();	
		}
		else if(look.tag == Tag.PRINT){
			match(Tag.PRINT);
			match('(');
			exprlist();
			match(')');
		}
		else if(look.tag == Tag.READ){
			match(Tag.READ);
			match('(');
			idlist();
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
			bexpr();
			match(')');
			stat();
			stat2();
		}
		else{
			match('{');
			statlist();
			match('}');
		}
	}

	private void stat1(){
		if(look.tag == Tag.ID){	
				match(Tag.ID);
				match(Tag.ASSIGN); 
				expr();
				match(';');
				bexpr();
				match(')');
				match(Tag.DO);
				stat();
			}
			else{
				bexpr();
				match(')');
				match(Tag.DO);
				stat();
			}
	}

	private void stat2(){
		if(look.tag == Tag.ELSE){
				match(Tag.ELSE);
				stat();
				match(Tag.END);
			}
			else{
				match(Tag.END);
			}
	}

	private void assignlist() {
		match('[');
		expr();
		match(Tag.TO);
		idlist();
		match(']');
		assignlistp();
	}

	private void assignlistp() {
		if(look.tag == '['){
			match('[');
			expr();
			match(Tag.TO);
			idlist();
			match(']');
			assignlistp();
		}
	}

	private void idlist() {
		match(Tag.ID);
		idlistp();
	}

	private void idlistp() {
		if(look.tag == ','){
			match(',');
			match(Tag.ID);
			idlistp();
		}
	}

	private void bexpr() {
		match(Tag.RELOP);
		expr();
		expr();
	}

	private void expr() {
		switch(look.tag){
			case '+':
				match('+');
				match('(');
				exprlist();
				match(')');
				break;

			case '-':
				match('-');
				expr();
				expr();
				break;

			case '*':
				match('*');
				match('(');
				exprlist();
				match(')');
				break;

			case '/':
				match('/');
				expr();
				expr();
				break;

			case Tag.NUM:
				match(Tag.NUM);
				break;

			default:
				match(Tag.ID);
		}
	}

	private void exprlist() {
		expr();
		exprlistp();
	}

	private void exprlistp() {
		if(look.tag == ','){
			match(',');
			expr();
			exprlistp();
		}
	}

	public static void main(String[] args) {
		Lexer lex = new Lexer();
		String path = "test.txt"; // il percorso del file da leggere
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			Parser parser = new Parser(lex, br);
			parser.start();
			System.out.println("Input OK");
			br.close();
		} 
		catch (IOException e) {e.printStackTrace();}
	}
}
