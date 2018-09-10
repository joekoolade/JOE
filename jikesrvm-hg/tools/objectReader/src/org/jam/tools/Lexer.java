package org.jam.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Lexer
{
    private final static Pattern cmdMatch = Pattern.compile("[\\p{Alpha}]+");
    private final static Pattern numberMatch = Pattern.compile("0?[xX]?[0-9a-fA-F]+");
    public List<Token> lex(String input)
    {
        List<Token> results = new ArrayList<Token>();
        String[] atoms = input.split("\\p{Space}");
        for(String atom : atoms)
        {
            // look for a command
            if(cmdMatch.matcher(atom).matches())
            {
                results.add(new Token(TokenType.COMMAND, atom));
            }
            else if(numberMatch.matcher(atom).matches())
            {
                results.add(new Token(TokenType.NUMBER, atom));
            }
        }
        return results;
    }
}
