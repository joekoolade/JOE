package org.jam.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Lexer
{
    private final static Pattern cmdMatch = Pattern.compile("[\\p{Alnum}\\._\\$]+");
    private final static Pattern numberMatch = Pattern.compile("0?[xX]?[0-9a-fA-F]+");
    public List<Token> lex(String input)
    {
        List<Token> results = new ArrayList<Token>();
        String[] atoms = input.split("\\p{Space}");
        for(String atom : atoms)
        {
            // look for a command
            if(atom.length()>2 && numberMatch.matcher(atom).matches())
            {
                results.add(new Token(TokenType.NUMBER, atom));
            }
            else if(cmdMatch.matcher(atom).matches())
            {
                results.add(new Token(TokenType.STRING, atom));
            }
        }
        return results;
    }
}
