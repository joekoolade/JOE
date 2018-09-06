package org.jam.tools;

import java.util.ArrayList;
import java.util.List;

public class Lexer
{
    public List<Token> lex(String input)
    {
        List<Token> result = new ArrayList<Token>();
        String[] atoms = input.split(" \t");
        foreach(String atom : atoms)
        {
            char aChar = input.charAt(index);
            if(Character.isDigit(aChar))
        }
        return result;
    }
}
