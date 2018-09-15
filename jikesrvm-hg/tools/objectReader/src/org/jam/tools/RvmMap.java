package org.jam.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.google.common.collect.ArrayListMultimap;

public class RvmMap {
    private RandomAccessFile mapFile;
    private ArrayListMultimap<String, MapField> fields;
    
    public RvmMap()
    {
        try
        {
            mapFile = new RandomAccessFile("RVM.map", "r");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    
    public void process()
    {
        String line;
        try
        {
            line = mapFile.readLine();
            while (line != null)
            {
                String tokens[] = line.split(" +", 5);
                if (tokens.length>2 && tokens[2].equals("field"))
                {
                    System.out.println(line);
                    MapField f = new MapField(tokens);
                }
                line = mapFile.readLine();
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void main(String args[])
    {
        RvmMap rmap = new RvmMap();
        rmap.process();
    }
}
