/*
 * Created on Oct 28, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.tests;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExceptionTests {
  public void test1() {
    try {
      throw new InternalError("Just an Internal Error");
    } catch (RuntimeException e) {
      System.out.println("Test1 failed: RuntimeException");
    } catch (InternalError e) {
      System.out.println("Test1 passed");
    }
    
  }
  
  public void test2() {
    try {
      throw new RuntimeException("Just a Runtime Exception");
    } catch (RuntimeException e) {
      System.out.println("Test2 passed");
    } catch (InternalError e) {
      System.out.println("Test failed: Internal error!");
    }
    
  }
  
  public void test3 (){
    try {
      try {
        throw new Exception("Just a General Exception");
      } catch (RuntimeException e) {
        System.out.println("Test3 failed: RuntimeException");
      } catch (InternalError e) {
        System.out.println("Test3 failed: Internal error!");
      }
    } catch (Throwable t) {
      System.out.println("Test3 passed.");
    }

  }
  
  public void test4() {
    try {
      throwException();
    } catch(InternalError e) {
      System.out.println("Test4 passed.");
    }
  }
  
  private void throwException() throws InternalError {
    throw new InternalError();
  }
}
