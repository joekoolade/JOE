package org.jam.fs;

import java.io.File;
import java.io.IOException;

public class JavaFsDev
{

    private static final int BA_EXISTS = 1;

    public static int getBooleanAttributes(File f)
    {
        int attribute = 0;
        String file = null;
        try
        {
            file = f.getCanonicalPath();
            if(JavaFile.exists(file)) attribute |= BA_EXISTS;
        } catch (IOException e)
        {
            System.out.println("No file: " + file);
            e.printStackTrace();
        }
        return attribute;
    }
  
  public static boolean checkAccess(File f, int access)
  {
    return true;
  }

  public static long getLastModifiedTime(File f)
  {
    return 0;
  }

  public static long getLength(File f)
  {
    return 0;
  }

  public static boolean setPermission(File f, int access, boolean enable, boolean owneronly)
  {
    return true;
  }

  public static boolean createFileExclusively(String path)
  {
    return true;
  }

  public static boolean delete(File f)
  {
    return true;
  }

  public static String[] list(File f)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public static boolean createDirectory(File f)
  {
    // TODO Auto-generated method stub
    return false;
  }

  public static boolean rename(File f1, File f2)
  {
    // TODO Auto-generated method stub
    return false;
  }

  public static boolean setLastModifiedTime(File f, long time)
  {
    // TODO Auto-generated method stub
    return false;
  }

  public static boolean setReadOnly(File f)
  {
    // TODO Auto-generated method stub
    return false;
  }

  public static long getSpace(File f, int t)
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public static void open(String name)
  {
    // TODO Auto-generated method stub

  }
}
