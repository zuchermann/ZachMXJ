package innards.util;

import java.io.*;
import java.lang.*;

/**
    A parser for simple file formats. <br>
    This class sets up a StreamTokenizer with some useful options,
    defines a ParseException, and includes some basic functionality
    such as reading the next number or string from the file. <br>

    @author mattb
*/

public abstract class SimpleParser implements Parser
{
    protected StreamTokenizer tokenizer;

    public SimpleParser(String dataFileName)
    {
        init(dataFileName, false);
    }

    public SimpleParser(String fileNameOrString, boolean bUseAStringBuffer)
    {
        init(fileNameOrString, bUseAStringBuffer);
    }


    private void init(String fileNameOrString, boolean bUseAStringBuffer)
    {
        try
        {
            if (!bUseAStringBuffer) {
                tokenizer = new StreamTokenizer(new BufferedReader(new FileReader(fileNameOrString)));
            }
            else {
                tokenizer = new StreamTokenizer(new BufferedReader(new StringReader(fileNameOrString)));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
	    setupSyntax();
    }

	protected void setupSyntax(){
		tokenizer.resetSyntax();
		tokenizer.wordChars('\u0000', '\u00FF');
		tokenizer.slashSlashComments(true);
		tokenizer.slashStarComments(true);
		tokenizer.eolIsSignificant(false);
		tokenizer.whitespaceChars(',', ',');
		tokenizer.whitespaceChars(' ', ' ');
		tokenizer.whitespaceChars('\n', '\n');
		tokenizer.whitespaceChars('\r', '\r');
	}

    protected void requireEndOfFileNow() throws Parser.ParseException
    {
        int currentToken = 0;
        try
        {
            currentToken = tokenizer.nextToken();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if (currentToken != tokenizer.TT_EOF)
        {
            throw new Parser.ParseException("At line " + tokenizer.lineno() +
                                            ", the parser expected end-of-file but read: " + tokenizer.toString());
        }
    }

    protected double getNumber() throws Parser.ParseException
    {
        int currentToken = 0;
        try
        {
            currentToken = tokenizer.nextToken();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (currentToken == tokenizer.TT_WORD)
        {
            String numberStream = tokenizer.sval;
            try
            {
                Double number = new Double(numberStream);
                return number.doubleValue();
            }
            catch (NumberFormatException e)
            {
                throw new Parser.ParseException("At line " + tokenizer.lineno() +
                                                ", the parser expected a number but read: " + tokenizer.toString());
            }
        }
        else
        {
            throw new Parser.ParseException("At line " + tokenizer.lineno() +
                                            ", the parser expected a number but read: " + tokenizer.toString());
        }
    }

    protected String getWord() throws Parser.ParseException
    {
        int currentToken = 0;
        try
        {
            currentToken = tokenizer.nextToken();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (currentToken == tokenizer.TT_WORD)
        {
            return tokenizer.sval;
        }
        else
        {
            throw new Parser.ParseException("At line " + tokenizer.lineno() +
                                            ", the parser expected a word but read: " + tokenizer.toString());
        }
    }
}
