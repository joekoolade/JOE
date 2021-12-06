package org.jam.tools;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class LexerTest
{

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testLex()
    {
        Lexer l = new Lexer();
        List<Token> results = l.lex("cmd 1234");
        assertEquals(2, results.size());
        Token t = results.get(0);
        assertEquals(TokenType.COMMAND, t.getType());
        assertEquals("cmd", t.getValue());
        t = results.get(1);
        assertEquals(TokenType.NUMBER, t.getType());
        assertEquals("1234", t.getValue());
        results = l.lex("0x1234");
        t = results.get(0);
        assertEquals(TokenType.NUMBER, t.getType());
        assertEquals("0x1234", t.getValue());
        results = l.lex("x1234");
        t = results.get(0);
        assertEquals(TokenType.NUMBER, t.getType());
        assertEquals("x1234", t.getValue());
    }

}
