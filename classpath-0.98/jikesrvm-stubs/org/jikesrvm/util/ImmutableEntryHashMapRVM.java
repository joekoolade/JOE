package org.jikesrvm.util;

import java.util.Iterator;

public class ImmutableEntryHashMapRVM<K, V> {
    public V get(K k) { return null; }
    public void put(K k, V v) {}
    public Iterable<V> values() { return null; }
    public Iterator<V> valueIterator() { return null; }
    public int size() { return 0; }
}
