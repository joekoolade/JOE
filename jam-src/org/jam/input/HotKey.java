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

    void next()
    {
        head = (head+1) % SIZE;
    }
    
    int next(int i)
    {
        i = (i+1) % SIZE;
        return i;
    }
    
    int getData(int i)
    {
        return data[i];
    }
    
    private void checkForHtk() {
        boolean cmdKey = false;
        boolean cmdJ = false;
        boolean displayThreads = false;
        boolean displayLocks = false;
        
        if(length() < 6) return;
        int start=head;
        int i;
        for(i=0; i < length(); i++)
        {
            int key = getData(start);
            start = next(start);
            VM.sysWriteln("key ", key);
            /*
             * Look for the comman key
             */
            if(ScanCodeSet1.KEY_EXTENDED.hasCode(key))
            {
                key = getData(start);
                start = next(start);
                if(ScanCodeSet1.KEY_CMD.hasCode(key))
                {
                    cmdKey = true;
                }
                else if(ScanCodeSet1.KEY_CMD.released(key))
                {
                    cmdKey = false;
                }
                continue;
            }
            /*
             * Look for the 'j'
             */
            if(ScanCodeSet1.KEY_J.hasCode(key))
            {
                cmdJ = cmdKey;
            }
            /*
             * Look for the 't'
             */
            if(ScanCodeSet1.KEY_T.hasCode(key))
            {
                if(cmdJ && cmdKey)
                {
                    VM.sysWriteln("Displaying threads");
                    cmdJ = false;
                    break;
                }
            }
            
        }
        
    }

}
