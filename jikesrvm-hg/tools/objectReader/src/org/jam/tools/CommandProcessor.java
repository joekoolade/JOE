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
    private final RvmMap map;
    
    public CommandProcessor(ObjectReader reader)
    {
        lexer = new Lexer();
        this.reader = reader;
        map = reader.getMap();
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
                if(results == null || results.size() < 2) continue;
                if(results.get(0).getValue().equals("p"))
                {
                    if(results.get(1).isNumber())
                    {
                        reader.dumpObject(results.get(1).getInt());
                    }
                    else
                    {
                        List<MapConstants> constantList;
                        List<MapField> fieldList = map.getField(results.get(1).getValue());
                        if(fieldList.isEmpty())
                        {
                            // Try the constant map
                            constantList = map.getLiteral(results.get(1).getValue());
                            if(constantList.isEmpty())
                            {
                                System.out.println("Field not found!");
                            }
                            else
                            {
                                reader.dumpObject(constantList.get(0));
                            }
                        }
                        else if(fieldList.size()==1)
                        {
                            reader.dumpObject(fieldList.get(0));
                        }
                        else if(fieldList.size()==0)
                        {
                            System.out.println("Field not found!");
                        }
                        else
                        {
                            for(MapField field: fieldList)
                            {
                                System.out.println(field.getKey());
                            }
                        }
                    }
                }
                else if(results.get(0).getValue().equals("c"))
                {
                    
                }
                else if(results.get(0).getValue().equals("d"))
                {
                    
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }
           // System.out.println(command);
        }
    }

    public static void main(String args[])
    {
        CommandProcessor cp = new CommandProcessor(null);
        cp.run();
    }
}
