package org.jam.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;

public class RvmMap {
    private static final boolean DEBUG_FIELDS = false;
    private static final boolean DEBUG_TIBS = false;
    private static final boolean DEBUG_CONSTANTS = false;
    private RandomAccessFile mapFile;
    private ArrayListMultimap<String, MapField> fields;
    private ArrayListMultimap<String, MapConstants> constants;
    private ArrayListMultimap<Integer, MapTib> tibs;
    private String workingDirectory;
    
    public RvmMap()
    {
        this("");
    }
    
    public RvmMap(String directory)
    {
        try
        {
            mapFile = new RandomAccessFile(directory+"RVM.map", "r");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        fields = ArrayListMultimap.create();
        tibs = ArrayListMultimap.create();
        constants = ArrayListMultimap.create();
    }
    
    public void process()
    {
        String line;
        System.out.println("Processing RVM.map");
        try
        {
            line = mapFile.readLine();
            while (line != null)
            {
                String tokens[] = line.split(" +", 5);
                if(tokens.length>2)
                {
                    if (tokens[2].equals("field"))
                    {
                        if(DEBUG_FIELDS) System.out.println(line);
                        MapField f = new MapField(tokens);
                        if(DEBUG_FIELDS) System.out.println(f.getKey());
                        fields.put(f.getKey(), f);
                    }
                    else if(tokens[2].equals("tib"))
                    {
                        if(DEBUG_TIBS) System.out.println(line);
                        MapTib t = new MapTib(tokens);
                        tibs.put(t.getAddress(), t);
                    }
                    else if(tokens[2].equals("literal/field"))
                    {
                        if(DEBUG_CONSTANTS) System.out.println(line);
                        MapConstants c = new MapConstants(tokens);
                        if(DEBUG_CONSTANTS) System.out.println(c.getKey());
                        constants.put(c.getKey(), c);
                    }
                }
                line = mapFile.readLine();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public List<MapField> getField(String field)
    {
        return fields.get(field);
    }
    
    public List<MapConstants> getLiteral(String field)
    {
        return constants.get(field);
    }
    
    public boolean isTib(Integer address)
    {
        return tibs.containsKey(address);
    }
    
    public static void main(String args[])
    {
        RvmMap rmap = new RvmMap();
        rmap.process();
    }
}
