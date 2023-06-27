package org.jam.board.pc;

import org.jam.util.InputObserver;

public class Input implements InputObserver {
    private char queue[];
    private final int QUEUE_SIZE = 32;
    int head, tail, size;
    
    public Input()
    {
        queue = new char[QUEUE_SIZE];
        head = tail = size = 0;
    }
    
    public void register(I8042 dev)
    {
        dev.attach(this);
    }

    @Override
    public void update(int data) {
        // TODO Auto-generated method stub
        
    }
    
    
}
