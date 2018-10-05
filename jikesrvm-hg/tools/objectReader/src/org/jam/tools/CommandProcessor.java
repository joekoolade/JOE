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
                String[] results = lexer.lex(command);
                if(results == null || results.length < 2) continue;
                if(results[0].equals("p"))
                {
                    try
                    {
                        int address = parseNumber(results[1]);
                        reader.dumpObject(address);
                    }
                    catch(NumberFormatException nfe)
                    {
                        List<MapConstants> constantList;
                        List<MapField> fieldList = map.getField(results[1]);
                        if(fieldList.isEmpty())
                        {
                            // Try the constant map
                            constantList = map.getLiteral(results[1]);
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
                else if(results[0].equals("c"))
                {
                    try
                    {
                        int address = parseNumber(results[1]);
                        MapCode code = map.getCode(address);
                        System.out.println(code.getTypeName()+"."+code.getName()+code.getParameters());
                    }
                    catch (NumberFormatException nfe)
                    {
                        System.err.println("Bad number: "+results[1]);
                    }
                }
                else if(results[0].equals("d"))
                {
                    
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }
           // System.out.println(command);
        }
    }

    /**
     * Return a number from the string
     * @param parameter
     * @return number
     */
    private int parseNumber(String parameter)
    {
        /*
         * All strings are assumed be in hex.
         * To specify a decimal precede with a 'i' -> i999
         * To specify a hex precede with 'x' or '0x'
         * To specify octal precede with 'o'
         */
        Integer number=null;
        
        char prefix=parameter.charAt(0);
        if(prefix=='i')
        {
            number = Integer.valueOf(parameter.substring(1), 10);
        }
        else if(prefix=='x')
        {
            number = Integer.valueOf(parameter.substring(1), 16);
        }
        else if(prefix=='o')
        {
            number = Integer.valueOf(parameter.substring(1), 8);
        }
        else if(parameter.startsWith("0x"))
        {
            number = Integer.valueOf(parameter.substring(2), 16);
        }
        else
        {
            number = Integer.valueOf(parameter, 16);
        }
        return number;
    }

    public static void main(String args[])
    {
        CommandProcessor cp = new CommandProcessor(null);
        cp.run();
    }
}
