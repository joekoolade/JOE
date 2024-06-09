package org.jam.fs;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jikesrvm.VM;
import org.jikesrvm.util.HashMapRVM;

public class JavaFile {
    public static HashMapRVM<String, byte[]> fileMap;
    String fileName;
    int pos;
    byte fileData[];
    
    public JavaFile() throws FileNotFoundException {
        
    }

    public JavaFile(String name, boolean append) throws FileNotFoundException {
        VM.sysWrite("JFS open ", name);
        if (append)
            VM.sysWriteln(" append");
        else
            VM.sysWriteln();
        fileData = fileMap.get(name);
        if(fileData == null) throw new FileNotFoundException();
        fileName = name;
        VM.sysWriteln("file length ", fileData.length);
    }

    public JavaFile(String name) throws FileNotFoundException {
        this(name, false);
    }

    public JavaFile(String name, int mode) {
        VM.sysWrite("JFS open ", name);
        VM.sysWriteln(" mode ", mode);
    }

    public int read() throws IOException {
        int data;
//        VM.sysWriteln("JFS read pos ", pos);
        if(pos >= fileData.length) data = -1;
        else data = fileData[pos++];
        return data & 0xFF;
    }

    public int readBytes(byte[] b, int off, int len) throws IOException {
        int readLength;
        VM.sysWrite("JFS readbytes ", b.length);
        VM.sysWrite(" off ", off);
        VM.sysWrite(" pos ", pos);
        VM.sysWriteln(" len ", len);
        if(b == null) throw new IOException();
//        if(off + len >= b.length) throw new IOException();
        if(pos >= fileData.length) return -1;
        if(pos + len >= fileData.length)
        {
            readLength = fileData.length - pos;
        }
        else
        {
            readLength = len;
        }
        System.arraycopy(fileData, pos, b, off, readLength);
        pos += readLength;
        VM.sysWrite("JFS2 readbytes ", b.length);
        VM.sysWrite(" ", off);
        VM.sysWrite(" ", len);
        VM.sysWrite(" ", len);
        VM.sysWriteln(" ", readLength);
        return readLength;
    }

    public long skip(long n)
    {
        if (pos + n > fileData.length)
        {
            n = fileData.length - pos;
            pos = fileData.length;
        }
        else
        {
            pos += (int) n;
        }
        VM.sysWrite("JFS skip ", (int)n);
        VM.sysWriteln(" pos ", pos);
        return n;
    }

    public int available() {
        VM.sysWriteln("JFS availabe");
        return 0;
    }

    public void close() {
        VM.sysWriteln("JFS close");
    }

    public void write(int b) {
        VM.sysWriteln("JFS write ", b);

    }

    public void writeBytes(byte[] b, int off, int len) {
        // TODO Auto-generated method stub
        VM.sysWrite("JFS writebytes ", b.length);
        VM.sysWrite(" ", off);
        VM.sysWriteln(" ", len);
    }

    public long getFilePointer() {
        VM.sysWriteln("JFS getfilepointer");
        return 0;
    }

    public void seek(long pos) {
        VM.sysWriteln("JFS seek ", pos);
        this.pos = (int)pos;
    }

    public long length() {
        VM.sysWriteln("JFS length");
        return fileData.length;
    }

    public static void create(String name, byte[] data) {
        fileMap.put(name, data);
    }
    
    public static boolean exists(String fileName)
    {
        return fileMap.get(fileName) != null;
    }
}
