/**
 * Created on Nov 28, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public interface Packet
{
    /*
     * The buffer array
     */
    byte[] getArray();

    /*
     * The buffer address
     */
    Address getAddress();

    /*
     * Index to the start of the packet data
     */
    int getOffset();

    /*
     * Size of the packet
     */
    int getSize();

    /*
     * Address where packet data starts
     * getAddress() + getOffset()
     */
    Address getPacketAddress();
    /*
     * Size of the whole buffer
     */
    int getBufferSize();
    void append(Packet packet);

    Address prepend(int size);

    void prepend(Packet packet);

    void setHeadroom(int size);
    
    /**
     * Advance offset by size bytes
     * @param size
     */
    void pull(int size);
}
