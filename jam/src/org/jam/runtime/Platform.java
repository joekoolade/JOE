/**
 * Platform interface.
 * 
 * This interfaced is used to by Jikes for VM services
 * 
 */
package org.jam.runtime;

/**
 * @author Joe Kulig
 * Copyright Joe Kulig 2013
 * All rights reserved. 
 *
 */
public interface Platform {
	public void putChar(char value);
	public void boot();
}
