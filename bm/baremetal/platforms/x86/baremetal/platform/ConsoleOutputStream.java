/*
 * Created on Oct 27, 2003
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
public class ConsoleOutputStream extends OutputStream {

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int ch) throws IOException {
		Console.writeCh(ch);
	}

}
