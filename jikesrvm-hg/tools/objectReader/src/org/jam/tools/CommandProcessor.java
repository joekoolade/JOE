package org.jam.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CommandProcessor
implements Runnable
{
    private final Lexer lexer;
    private final ObjectReader reader;
    
    public CommandProcessor(ObjectReader reader)
    {
        lexer = new Lexer();
        this.reader = reader;
    }
    
    public void run()
    {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        while(true)
        {
            System.out.print("## ");
            String command=null;
            try
            {
                command = input.readLine();
                List<Token> results = lexer.lex(command);
                if(results == null || results.size()==0) continue;
                if(results.get(0).getValue().equals("xo"))
                {
                    reader.dumpObject(results.get(1).getInt());
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            System.out.println(command);
        }
    }

    public static void main(String args[])
    {
        CommandProcessor cp = new CommandProcessor(null);
        cp.run();
    }
}
