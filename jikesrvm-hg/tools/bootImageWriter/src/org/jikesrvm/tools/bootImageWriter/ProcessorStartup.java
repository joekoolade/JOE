package org.jikesrvm.tools.bootImageWriter;

public interface ProcessorStartup
{

    void writeImage(String filename);

    byte[] getArray();

}