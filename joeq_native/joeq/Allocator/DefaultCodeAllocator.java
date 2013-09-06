// DefaultCodeAllocator.java, created Mon Apr  9  1:01:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Allocator;

import joeq.Allocator.CodeAllocator.x86CodeBuffer;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_StaticField;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Runtime.Unsafe;

/**
 * Provides access functions to the default code allocator.
 * If the default_allocator is set, it is used as the default global code allocator.
 * Otherwise, the code allocator of the current thread is used.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: DefaultCodeAllocator.java,v 1.12 2005/01/21 07:13:15 joewhaley Exp $
 */
public abstract class DefaultCodeAllocator {

    /**
     * The default global code allocator.  If this is set, all threads use this
     * allocator instead of their thread-local allocators.
     */
    public static CodeAllocator default_allocator;

    /**
     * Gets the default code allocator for the current thread.
     * 
     * @return default code allocator for the current thread
     */
    public static final CodeAllocator def() {
        if (default_allocator != null) return default_allocator;
        return Unsafe.getThreadBlock().getNativeThread().getCodeAllocator();
    }
    
    /**
     * Initialize the default code allocator.
     */
    public static final void init() {
        def().init();
    }
    
    /**
     * Get a code buffer from the default code allocator.
     * 
     * @param estimatedSize
     * @param offset
     * @param alignment
     * @return  a new code buffer
     */
    public static final x86CodeBuffer getCodeBuffer(int estimatedSize, int offset, int alignment) {
        x86CodeBuffer o = def().getCodeBuffer(estimatedSize, offset, alignment);
        return o;
    }
    
    /**
     * Patch the code address to point to the given heap address in the default
     * code allocator.
     * 
     * @param code
     * @param heap
     */
    public static final void patchAbsolute(Address code, Address heap) {
        def().patchAbsolute(code, heap);
    }
    
    /**
     * Patch the code address to be a relative offset to another code address.
     * 
     * @param code
     * @param target
     */
    public static final void patchRelativeOffset(CodeAddress code, CodeAddress target) {
        def().patchRelativeOffset(code, target);
    }
    
    public static final jq_StaticField _default_allocator;
    static {
        jq_Class k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Allocator/DefaultCodeAllocator;");
        _default_allocator = k.getOrCreateStaticField("default_allocator", "Ljoeq/Allocator/CodeAllocator;");
    }
}
