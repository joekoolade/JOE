package org.jikesrvm.runtime;

public class ExternalFile
{
    public String name;
    public byte[] data;
    
    public ExternalFile(String file, byte[] data)
    {
        name = file;
        this.data = data;
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer(name);
        sb.append(' ').append(data.length);
        
        return sb.toString();
    }
}
