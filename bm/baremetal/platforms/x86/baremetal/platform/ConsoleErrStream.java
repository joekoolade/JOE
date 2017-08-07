/*
 * Created on May 19, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package baremetal.platform;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ConsoleErrStream extends OutputStream {

  /* (non-Javadoc)
   * @see java.io.OutputStream#write(int)
   */
  public void write(int b) throws IOException {
    Console.write(b);
    flush();
  }

}
