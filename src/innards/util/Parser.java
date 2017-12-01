package innards.util;

public interface Parser {
	public void parse() throws ParseException;

    public class ParseException extends Exception
    {
        public ParseException(){}
        public ParseException(String msg){System.err.println(msg);}
    }
}