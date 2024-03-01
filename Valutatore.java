import java.io.*;
public class Valutatore {
	private Lexer lex;
	private BufferedReader pbr;
	private Token look;

	public Valutatore(Lexer l, BufferedReader br) {
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
		int expr_val;
		expr_val = expr();
		match(Tag.EOF);
		System.out.println(expr_val);
	}

	private int expr() { 
		int term_val, exprp_val;
		term_val = term();
		exprp_val = exprp(term_val);
		return exprp_val;
	}


	private int exprp(int exprp_i) {
		int term_val, exprp_val;
		switch(look.tag){
	      case '+':
		      match('+');
		      term_val = term();
		      return exprp(exprp_i + term_val);
		    case '-':
		      match('-');
		      term_val = term();
		      return exprp(exprp_i - term_val);
		    default:
		    	return exprp_i;
    	}
	}

	private int term() {
		int termp_i;
		termp_i = fact();
		return termp(termp_i);
	}

	private int termp(int termp_i) {
		int fact_val;
		switch(look.tag){
	    case '*':
		    match('*');
		    fact_val = fact();
		    return termp(termp_i * fact_val);
		  case '/':
		    match('/');
		    fact_val = fact();
		    return termp(termp_i / fact_val);
		  default:
		  	return termp_i;
    	}
	}

	private int fact() {
		int exprp_val;
		int fact_val;
		if(look.tag == Tag.NUM){ 
			fact_val = ((NumberTok) look).getNumber();
			match(Tag.NUM);
			return fact_val;
		}
		else{
			match('(');
			exprp_val = expr();
			match(')');
			return exprp_val;
		}
	}

	public static void main(String[] args) {
		Lexer lex = new Lexer();
		String path = "test.txt"; // il percorso del file da leggere
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			Valutatore valutatore = new Valutatore(lex, br);
			valutatore.start();
			System.out.println("Input OK");
			br.close();
		} 
		catch (IOException e) {e.printStackTrace();}
	}
}
