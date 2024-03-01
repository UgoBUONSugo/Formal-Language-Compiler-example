import java.io.*; 
import java.util.*;

public class Lexer {

    public Token stringToToken(String word){

        if( word.equals(Word.assign.lexeme) ){
            return Word.assign;
        }
        else if( word.equals(Word.to.lexeme) ){
            return Word.to;
        }
        else if( word.equals(Word.iftok.lexeme) ){
            return Word.iftok;
        }
        else if( word.equals(Word.elsetok.lexeme) ){
            return Word.elsetok;
        }
        else if( word.equals(Word.dotok.lexeme) ){
            return Word.dotok;
        }
        else if( word.equals(Word.fortok.lexeme) ){
            return Word.fortok;
        }
        else if( word.equals(Word.begin.lexeme) ){
            return Word.begin;
        }
        else if( word.equals(Word.end.lexeme) ){
            return Word.end;
        }
        else if( word.equals(Word.print.lexeme) ){
            return Word.print;
        }
        else if( word.equals(Word.read.lexeme) ){
            return Word.read;
        }
        else{
            return new Word(Tag.ID, word);
        }
}

    public static int line = 1;
    private char peek = ' ';
    
    private void readch(BufferedReader br) {
        try {
            peek = (char) br.read();
        } catch (IOException exc) {
            peek = (char) -1; // ERROR
        }
    }

    public Token lexical_scan(BufferedReader br) {
        while (peek == ' ' || peek == '\t' || peek == '\n'  || peek == '\r') {
            if (peek == '\n') line++;
            readch(br);
        }
        
        switch (peek) {
            case '!':
                peek = ' ';
                return Token.not;

            case '(':
                peek = ' ';
                return Token.lpt;

            case ')':
                peek = ' ';
                return Token.rpt;

            case '[':
                peek = ' ';
                return Token.lpq;

            case ']':
                peek = ' ';
                return Token.rpq;

            case '{':
                peek = ' ';
                return Token.lpg;

            case '}':
                peek = ' ';
                return Token.rpg;

            case '+':
                peek = ' ';
                return Token.plus;

            case '-':
                peek = ' ';
                return Token.minus;

            case '*':
                peek = ' ';
                return Token.mult;

            case '/':
                readch(br);
                switch(peek){
                    case '/':
                        while(peek != '\n' && peek != (char) -1){
                            readch(br);
                        }
                        return lexical_scan(br);
                    case '*':
                        boolean endComment = false;
                        readch(br);
                        while(!endComment){
                            while(peek != '*' && peek != (char) -1){
                                readch(br);
                            }
                            if(peek == '*'){
                                readch(br);
                                endComment = peek == '/';
                            }
                            else{
                                System.err.println("Error invalid comment");
                                return null; 
                            }                            
                        }
                        peek = ' ';
                        return lexical_scan(br);
                    default:
                }
                peek = ' ';
                return Token.div;

            case ';':
                peek = ' ';
                return Token.semicolon;

            case ':':
                readch(br);
                switch(peek){
                    case '=':
                        peek = ' ';
                        return Word.init;
                    default:
                        return null;
                }

            case ',':
                peek = ' ';
                return Token.comma;

  // ... gestire i casi di ( ) [ ] { } + - * / ; , ... //
  
            case '&':
                readch(br);
                if (peek == '&') {
                    peek = ' ';
                    return Word.and;
                } else {
                    System.err.println("Erroneous character" + " after & : "  + peek );
                    return null;
                }

            case '|':
                readch(br);
                if (peek == '|') {
                    peek = ' ';
                    return Word.or;
                } else {
                    System.err.println("Erroneous character"
                            + " after | : "  + peek );
                    return null;
                }

            case '<':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.le;
                }
                else if( peek == '>'){
                    peek = ' ';
                    return Word.ne;
                } else {
                    return Word.lt;
                }

            case '>':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.ge;
                } else {
                    return Word.gt;
                }

            case '=':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.eq;
                } else {
                    System.err.println("Erroneous character"
                            + " after = : "  + peek );
                    return null;
                }

  // ... gestire i casi di || < > <= >= == <> ... //
          
            case (char)-1:
                return new Token(Tag.EOF);

            default:
                if (Character.isLetter(peek) || peek == '_') {
                  boolean underscoreStart = peek == '_';
                  String word = "";
                  while(underscoreStart){
                    word += peek;
                    readch(br);
                    if(Character.isLetter(peek) || Character.isDigit(peek)){
                      break;
                    }
                    else if(peek == '_'){}
                    else{
                      System.err.println("Erroneous character" + " after _ :  "  + peek );
                      return null;
                    }
                  }
                  while(Character.isLetter(peek) || Character.isDigit(peek) || peek == '_'){
                    word += peek;
                    readch(br);
                  }
                  return stringToToken(word);
                }

  // ... gestire il caso degli identificatori e delle parole chiave //
                 
                else if (Character.isDigit(peek)) {
                    NumberTok number = new NumberTok();
                    while (Character.isDigit(peek)) {
                      number.read(peek);
                      readch(br);
                    }
                      number.cast();
                      return number;
                    }

  // ... gestire il caso dei numeri ... //

                    else {
                        System.err.println("Erroneous character: " 
                                + peek );
                        return null;
                }
         }
    }
    
    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "test.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Token tok;
            do {
                tok = lex.lexical_scan(br);
                System.out.println("Scan: " + tok);
            } while (tok.tag != Tag.EOF);
            br.close();
        } catch (IOException e) {e.printStackTrace();}    
    }

}



