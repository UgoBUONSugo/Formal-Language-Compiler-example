public class NumberTok extends Token {
  private String numberStr;
  private int number;

  public NumberTok() {
    super(Tag.NUM);
    numberStr = "";
  }

  public int getNumber(){return this.number;}

  public String toString() {return "<256, " + this.number + ">";}

  public void read(char digit) {
    numberStr += digit;
  }

  public void cast() {
    this.number = Integer.parseInt(numberStr);
  }
}