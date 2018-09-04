package org.jam.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandProcessor
implements Runnable
{

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
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            System.out.println(command);
        }
    }

    public static void main(String args[])
    {
        CommandProcessor cp = new CommandProcessor();
        cp.run();
    }
}
