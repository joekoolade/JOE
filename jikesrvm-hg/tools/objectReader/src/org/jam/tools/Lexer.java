package org.jam.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Lexer
{
    public String[] lex(String input)
    {
        String[] atoms = input.split("\\p{Space}");
        return atoms;
    }
}
