package org.jam.tools;

public class Token
{
    final private TokenType type;
    final private String value;
    private Long num;
    
    public Token(TokenType command, String atom)
    {
        type = command;
        value = atom;
        if(command == TokenType.NUMBER)
        {
            if(value.charAt(0)=='x' || value.charAt(0)=='X')
            {
                num = Long.parseLong(value.substring(1), 16);
            }
            else if(value.charAt(1)=='x' || value.charAt(1)=='X')
            {
                num = Long.parseLong(value.substring(2), 16);
            }
            else
            {
                try
                {
                    num = Long.parseLong(value);
                } catch (NumberFormatException e)
                {
                    num = Long.parseLong(value,16);
                }
            }
            
        }
        else
        {
            num = null;
        }
    }

    public Token(String atom)
    {
        value = atom;
        type = TokenType.STRING;
    }

    public TokenType getType()
    {
        return type;
    }
    
    public boolean isString()
    {
        return type == TokenType.STRING;
    }
    
    public boolean isNumber()
    {
        return type == TokenType.NUMBER;
    }
    public String getValue()
    {
        return value;
    }
    
    public int getInt()
    {
        return num.intValue();
    }
    
    public long getLong()
    {
        return num.longValue();
    }
}
