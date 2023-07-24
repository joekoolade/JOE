package org.jam.input;

import org.jam.board.pc.I8042;
import org.jam.util.InputObserver;
import org.jikesrvm.VM;
import org.jam.board.pc.ScanCodeSet1;

public class HotKey implements InputObserver, Runnable {
    int data[];
    int keys[];
    int head, tail;
    final static int SIZE = 64;
    boolean updated;
    Thread hotKeyThread;
    int next;
    
    public HotKey(I8042 dev)
    {
        dev.attach(this);
        data = new int[SIZE];
        head = 0;
        tail = 0;
        hotKeyThread = new Thread(this);
        hotKeyThread.start();
    }
    
    @Override
    public void update(int data) {
        this.data[tail] = data;
        tail = (tail+1) % SIZE;
        updated = true;
    }

    public boolean hasData()
    {
        return head != tail;
    }

    int getKey()
    {
        if(hasData() == false) return 0;
        
        int key = data[head];
        head = (head+1) % SIZE;
        return key;
    }
    
    /**
     * number of keys strokes in the queue
     * @return keys in the queue
     */
    int length()
    {
        if(head <= tail)
        {
            return tail - head;
        }
        else
        {
            return (SIZE-head) + tail;
        }
    }

    @Override
    public void run() {
        while(true)
        {
            if(updated)
            {
//                VM.sysWriteln("HotKey ", key /*VM.intAsHexString(key)*/);
                checkForHtk();
                updated = false;
            }
            Thread.yield();
        }
        
    }

    void advance()
    {
        head = (head+1) % SIZE;
    }
    
    void advance(int i)
    {
        head = (head + i) % SIZE;
    }
    
    int getData(int i)
    {
        return data[i];
    }
    
    void empty()
    {
        head = tail;
    }
    private void checkForHtk() {
        if(length() < 5) return;
        int i;
        for(i=0; i < length(); i++)
        {
            /*
             * Look for the 
             *  1. extended cmd press, J press, J release, T press
             */
            
            /*
             * Extend
             */
            if(ScanCodeSet1.KEY_EXTENDED.hasCode(getData(head)) == false)
            {
                advance();
                continue;
            }
            /*
             * CMD key
             */
            if(ScanCodeSet1.KEY_CMD.hasCode(getData(head+1)) == false)
            {
                advance(2);
                continue;
            }
            /*
             * Look for the 'j' press
             */
            if(ScanCodeSet1.KEY_J.hasCode(getData(head+2)) == false)
            {
                advance(3);
                VM.sysWriteln("cmdJ ", i);
            }
            /*
             * Look for the 'j' release
             */
            if(ScanCodeSet1.KEY_J.released(getData(head+3)) == false)
            {
                advance(4);
                VM.sysWriteln("cmdJ ", i);
            }
            /*
             * Look for the 't'
             */
            if(ScanCodeSet1.KEY_T.code(getData(head+4)))
            {
                VM.sysWriteln("Displaying threads ", i);
                advance(5);
            }
        }
        VM.sysWrite("htk done: i:", i);
        VM.sysWriteln("head:",head);
    }

}
