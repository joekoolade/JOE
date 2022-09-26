package org.jikesrvm.runtime;

public class ExternalFile
{
    public String name;
    public byte[] data;
    private String className;
    
    public ExternalFile(String file, byte[] data)
    {
        name = file;
        this.data = data;
        className = null;
    }
    
    public String getClassName()
    {
        if(className == null)
        {
            int classNameIndex = name.indexOf("ext/bin/") + 8;
            // remove the preceeding directory
            className = name.substring(classNameIndex);
            // remove the '.class' suffix
            className = className.substring(0, className.length() - 6);
            className = className.replace('/', '.');
        }
        return className;
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer(name);
        sb.append(' ').append(data.length);
        
        return sb.toString();
    }
}
