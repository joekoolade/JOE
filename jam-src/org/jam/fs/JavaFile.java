package org.jam.fs;

import org.jikesrvm.VM;

public class JavaFile
{

  public JavaFile()
  {

  }

  public JavaFile(String name, boolean append)
  {
    VM.sysWrite("JFS open ", name);
    if (append)
      VM.sysWriteln(" append");
    else
      VM.sysWriteln();
  }
  public JavaFile(String name)
  {
    this(name, false);
  }

  public JavaFile(String name, int mode)
  {
    VM.sysWrite("JFS open ", name);
    VM.sysWriteln(" mode ", mode);
  }

  public int read()
  {
    VM.sysWriteln("JFS read");
    return 0;
  }

  public int readBytes(byte[] b, int off, int len)
  {
    VM.sysWrite("JFS readbytes ", b.length);
    VM.sysWrite(" ", off);
    VM.sysWriteln(" ", len);
    return 0;
  }

  public long skip(long n)
  {
    VM.sysWriteln("JFS skip ", n);
    return 0;
  }

  public int available()
  {
    VM.sysWriteln("JFS availabe");
    return 0;
  }

  public void close()
  {
    VM.sysWriteln("JFS close");

  }

  public void write(int b)
  {
    VM.sysWriteln("JFS write ", b);

  }

  public void writeBytes(byte[] b, int off, int len)
  {
    // TODO Auto-generated method stub
    VM.sysWrite("JFS writebytes ", b.length);
    VM.sysWrite(" ", off);
    VM.sysWriteln(" ", len);
  }

  public long getFilePointer()
  {
    VM.sysWriteln("JFS getfilepointer");
    return 0;
  }

  public void seek(long pos)
  {
    VM.sysWriteln("JFS seek ", pos);

  }

  public long length()
  {
    VM.sysWriteln("JFS length");
    return 0;
  }
}
