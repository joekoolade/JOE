package org.jam.input;

import org.jam.board.pc.I8042;
import org.jam.util.InputObserver;
import org.jikesrvm.VM;

public class HotKey implements InputObserver, Runnable {
    int data[];
    int head, tail;
    final static int SIZE = 64;
    boolean updated;
    static Thread hotKeyThread;
    
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
    
    @Override
    public void run() {
        while(true)
        {
            if(updated)
            {
                int key = getKey();
                VM.sysWriteln("HotKey ", key);
                updated = false;
            }
            Thread.yield();
        }
        
    }
    
    
}
