package org.jam.input;

import org.jam.board.pc.I8042;
import org.jam.util.InputObserver;
import org.jikesrvm.VM;
import org.jikesrvm.scheduler.Lock;
import org.jikesrvm.scheduler.RVMThread;
import org.jam.board.pc.ScanCodeSet1;

public class HotKey implements InputObserver, Runnable {
    int data[];
    int keys[];
    int head, tail, size;
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
        size = 0;
        hotKeyThread = new Thread(this);
        hotKeyThread.start();
    }
    
    @Override
    public void update(int data) {
        this.data[tail] = data;
        tail = (tail+1) % SIZE;
        updated = true;
        size++;
    }

    public boolean hasData()
    {
        return size > 0;
    }

    int getKey()
    {
        if(hasData() == false) return 0;
        
        int key = data[head];
        head = (head+1) % SIZE;
        size--;
        return key;
    }
    
    /**
     * number of keys strokes in the queue
     * @return keys in the queue
     */
    int length()
    {
        return size;
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

    final private void advance()
    {
        advance(1);
    }
    
    final private void advance(int i)
    {
        if(i > size) i = size;
        head = (head + i) % SIZE;
        size -= i;
//        VM.sysWrite("head ", head);
//        VM.sysWriteln(" tail ", tail);
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
        int i;
//        VM.sysWrite("START size ", length());
//        VM.sysWrite(" tail: ", tail);
//        VM.sysWriteln(" head:",head);
        if(length() < 5) return;
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
//                VM.sysWriteln("cmdJ ", i);
                continue;
            }
            /*
             * Look for the 'j' release
             */
            if(ScanCodeSet1.KEY_J.released(getData(head+3)) == false)
            {
                advance(4);
//                VM.sysWriteln("cmdJ ", i);
                continue;
            }
            /*
             * Look for the 't'
             */
            if(ScanCodeSet1.KEY_T.code(getData(head+4)))
            {
//                VM.sysWriteln("Displaying threads ", i);
                advance(5);
                RVMThread.dumpAcct();
            }
            else if(ScanCodeSet1.KEY_L.code(getData(head+4)))
            {
                advance(5);
                Lock.dumpLocks();
            }
        }
//        VM.sysWrite("HTK DONE i:", i);
//        VM.sysWrite(" size: ", size);
//        VM.sysWrite(" tail: ", tail);
//        VM.sysWriteln(" head:",head);
    }

}
