package org.jam.util;

public interface InputSubject {
    void attach(InputObserver o);
    void detach(InputObserver o);
}
