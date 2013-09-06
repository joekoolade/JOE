// HostedVM.java, created Sat Dec 14  2:52:34 2002 by mcmartin
// Copyright (C) 2001-3 mcmartin
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Main;

import java.util.Iterator;
import joeq.Class.PrimordialClassLoader;
import joeq.ClassLib.ClassLibInterface;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.Reflection;
import jwutil.util.Assert;

/**
 * @author  Michael Martin <mcmartin@stanford.edu>
 * @version $Id: HostedVM.java,v 1.12 2004/09/22 22:17:31 joewhaley Exp $
 */
public abstract class HostedVM {
    public static void initialize() {
        if (jq.RunningNative) return;
        
        jq.DontCompile = true;

        CodeAddress.FACTORY = new CodeAddress.CodeAddressFactory() {
            public int size() {
                return 4;
            }
            public CodeAddress getNull() {
                return null;
            }
        };
        HeapAddress.FACTORY = new HeapAddress.HeapAddressFactory() {
            public int size() {
                return 4;
            }
            
            public int logSize() {
                return 2;
            }
            
            public int pageAlign() {
                return 12; // 2**12 = 4096
            }

            public HeapAddress getNull() {
                return bogus_heap_address;
            }

            public HeapAddress addressOf(Object o) {
                return bogus_heap_address;
            }

            public HeapAddress address32(int val) {
                return bogus_heap_address;
            }
        };
        StackAddress.FACTORY = new StackAddress.StackAddressFactory() {
            public int size() {
                return 4;
            }

            public StackAddress alloca(int a) {
                Assert.UNREACHABLE();
                return null;
            }

            public StackAddress getBasePointer() {
                Assert.UNREACHABLE();
                return null;
            }

            public StackAddress getStackPointer() {
                Assert.UNREACHABLE();
                return null;
            }
        };
        String classpath = System.getProperty("sun.boot.class.path") + System.getProperty("path.separator") + System.getProperty("java.class.path");
        for (Iterator it = PrimordialClassLoader.classpaths(classpath); it.hasNext();) {
            String s = (String) it.next();
            PrimordialClassLoader.loader.addToClasspath(s);
        }
        Reflection.obj_trav = ClassLibInterface.DEFAULT.getObjectTraverser();
        Reflection.obj_trav.initialize();
    }
    
    public static final BogusHeapAddress bogus_heap_address = new BogusHeapAddress();
    
    public static class BogusHeapAddress extends HeapAddress {
        
        private BogusHeapAddress() {}
        
        /* (non-Javadoc)
         * @see joeq.Memory.Address#align(int)
         */
        public Address align(int shift) {
            return this;
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#difference(joeq.Memory.Address)
         */
        public int difference(Address v) {
            return 0;
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#isNull()
         */
        public boolean isNull() {
            return true;
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#offset(int)
         */
        public Address offset(int offset) {
            return this;
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#peek()
         */
        public Address peek() {
            return this;
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#peek1()
         */
        public byte peek1() {
            return 0;
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#peek2()
         */
        public short peek2() {
            return 0;
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#peek4()
         */
        public int peek4() {
            return 0;
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#peek8()
         */
        public long peek8() {
            return 0;
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#poke(joeq.Memory.Address)
         */
        public void poke(Address v) {
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#poke1(byte)
         */
        public void poke1(byte v) {
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#poke2(short)
         */
        public void poke2(short v) {
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#poke4(int)
         */
        public void poke4(int v) {
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#poke8(long)
         */
        public void poke8(long v) {
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#stringRep()
         */
        public String stringRep() {
            return "bogus";
        }
        /* (non-Javadoc)
         * @see joeq.Memory.Address#to32BitValue()
         */
        public int to32BitValue() {
            return 0;
        }
}

}
