// SystemInterface.java, created Mon Feb  5 23:23:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Runtime;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_StaticField;
import joeq.Main.jq;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Scheduler.jq_RegisterState;
import joeq.Scheduler.jq_Thread;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: SystemInterface.java,v 1.37 2004/09/30 03:35:35 joewhaley Exp $
 */
public abstract class SystemInterface {

    public static class ExternalLink {
        private final String name;
        private Library library;
        private CodeAddress address;
        
        public ExternalLink(String name) {
            this.name = name;
            this.library = null;
            this.address = CodeAddress.getNull();
        }
        
        public CodeAddress resolve() {
            if (!address.isNull())
                return address;
            synchronized (libraries) {
                for (Iterator/*<Library>*/ i=libraries.iterator(); i.hasNext(); ) {
                    Library lib = (Library) i.next();
                    address = lib.getProcAddress(name);
                    if (!address.isNull()) {
                        library = lib;
                        lib.registerLink(this);
                        return address;
                    }
                }
            }
            Debug.write("Error: cannot resolve external procedure ");
            Debug.writeln(name);
            return CodeAddress.getNull();
        }
        
        public CodeAddress resolve(Library lib) {
            if (!address.isNull())
                return address;
            address = lib.getProcAddress(name);
            if (!address.isNull()) {
                library = lib;
                lib.registerLink(this);
            }
            return address;
        }
        
        public void unlink() {
            if (this.library != null) {
                this.library.unlink(this);
            }
            this._unlink();
        }
        
        void _unlink() {
            this.library = null;
            this.address = CodeAddress.getNull();
        }
        
    }

    public static class Library {
        private final String name;
        private int/*CPointer*/ library_pointer;
        private boolean opened;
        private final Collection externals;
        
        public Library(String name) {
            this.name = name;
            this.opened = false;
            this.externals = new LinkedList();
        }
        
        public String getName() { return name; }
        
        public synchronized boolean open() {
            if (opened) return true;
            library_pointer = open_library(toCString(name));
            if (library_pointer == 0) return false;
            opened = true; return true;
        }
        
        public CodeAddress getProcAddress(String procName) {
            if (!opened) open();
            CodeAddress x = get_proc_address(library_pointer, toCString(procName));
            //Debug.write(name);
            //Debug.write(" procedure ");
            //Debug.write(procName);
            //Debug.writeln(" = ", x);
            return x;
        }
        
        public ExternalLink resolve(String procname) {
            ExternalLink x = new ExternalLink(procname);
            CodeAddress c = x.resolve(this);
            if (!c.isNull()) return x;
            else return null;
        }
        
        public synchronized void registerLink(ExternalLink e) {
            externals.add(e);
        }
        
        public synchronized void unlink(ExternalLink e) {
            externals.remove(e);
        }
        
        public synchronized void close() {
            for (Iterator i=externals.iterator(); i.hasNext(); ) {
                ExternalLink x = (ExternalLink) i.next();
                x._unlink();
                i.remove();
            }
            if (opened) {
                close_library(library_pointer);
                opened = false;
            }
        }
        
        protected void finalize() throws Throwable {
            super.finalize();
            close();
        }

    }
    
    public static CodeAddress open_library_4;
    public static CodeAddress get_proc_address_8;
    public static CodeAddress close_library_4;
    
    public static int/*CPointer*/ open_library(byte[] library_name) {
        Unsafe.pushArgA(HeapAddress.addressOf(library_name));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int) Unsafe.invoke(open_library_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); return 0; }
    }

    public static void close_library(int/*CPointer*/ library) {
        Unsafe.pushArg(library);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(close_library_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }

    public static CodeAddress get_proc_address(int/*CPointer*/ library, byte[] name) {
        Unsafe.pushArgA(HeapAddress.addressOf(name));
        Unsafe.pushArg(library);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            CodeAddress v = (CodeAddress) Unsafe.invokeA(get_proc_address_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); return null; }
    }
    
    public static final Collection/*<Library>*/ libraries = new LinkedList();
    
    public static Library registerLibrary(String libraryName) {
        synchronized (libraries) {
            for (Iterator i=libraries.iterator(); i.hasNext(); ) {
                Library lib = (Library) i.next();
                if (libraryName.equals(lib.getName()))
                    return lib;
            }
            Library lib = new Library(libraryName);
            if (lib.open()) {
                libraries.add(lib);
                return lib;
            } else {
                return null;
            }
        }
    }
    
    public static ExternalLink tryLink(String name) {
        ExternalLink x = new ExternalLink(name);
        if (!x.resolve().isNull()) return x;
        else return null;
    }
    
    public static CodeAddress debugwrite_8;
    public static CodeAddress debugwwrite_8;
    public static CodeAddress debugwriteln_8;
    public static CodeAddress debugwwriteln_8;
    public static CodeAddress syscalloc_4;
    public static CodeAddress sysfree_4;
    public static CodeAddress die_4;
    public static CodeAddress currentTimeMillis_0;
    public static CodeAddress mem_cpy_12;
    public static CodeAddress mem_set_12;
    public static CodeAddress file_open_12;
    public static CodeAddress file_stat_8;
    public static CodeAddress file_readbytes_12;
    public static CodeAddress file_writebyte_8;
    public static CodeAddress file_writebytes_12;
    public static CodeAddress file_sync_4;
    public static CodeAddress file_seek_16;
    public static CodeAddress file_close_4;
    public static CodeAddress console_available_0;
    public static CodeAddress main_argc_0;
    public static CodeAddress main_argv_length_4;
    public static CodeAddress main_argv_8;
    public static CodeAddress fs_getdcwd_12;
    public static CodeAddress fs_fullpath_12;
    public static CodeAddress fs_gettruename_4;
    public static CodeAddress fs_getfileattributes_4;
    public static CodeAddress fs_access_8;
    public static CodeAddress fs_getfiletime_4;
    public static CodeAddress fs_stat_size_4;
    public static CodeAddress fs_remove_4;
    public static CodeAddress fs_opendir_4;
    public static CodeAddress fs_readdir_4;
    public static CodeAddress fs_closedir_4;
    public static CodeAddress fs_mkdir_4;
    public static CodeAddress fs_rename_8;
    public static CodeAddress fs_chmod_8;
    public static CodeAddress fs_setfiletime_12;
    public static CodeAddress fs_getlogicaldrives_0;
    public static CodeAddress yield_0;
    public static CodeAddress msleep_4;
    public static CodeAddress create_thread_8;
    public static CodeAddress init_thread_0;
    public static CodeAddress resume_thread_4;
    public static CodeAddress suspend_thread_4;
    public static CodeAddress set_thread_priority_8;
    public static CodeAddress allocate_stack_4;
    public static CodeAddress get_current_thread_handle_0;
    public static CodeAddress get_thread_context_8;
    public static CodeAddress set_thread_context_8;
    public static CodeAddress set_current_context_8;
    public static CodeAddress set_interval_timer_8;
    public static CodeAddress init_semaphore_0;
    public static CodeAddress wait_for_single_object_8;
    public static CodeAddress release_semaphore_8;

    public static final jq_Class _class;
    public static final jq_StaticField _debugwrite;
    public static final jq_StaticField _debugwriteln;
    public static final jq_InstanceField _string_value;
    public static final jq_InstanceField _string_offset;
    public static final jq_InstanceField _string_count;
    static {
        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Runtime/SystemInterface;");
        _debugwrite = _class.getOrCreateStaticField("debugwrite_8", "Ljoeq/Memory/CodeAddress;");
        _debugwriteln = _class.getOrCreateStaticField("debugwriteln_8", "Ljoeq/Memory/CodeAddress;");
        // cannot use getJavaLangString here, as it may not yet have been initialized.
        jq_Class jls = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/String;");
        _string_value = jls.getOrCreateInstanceField("value", "[C");
        _string_offset = jls.getOrCreateInstanceField("offset", "I");
        _string_count = jls.getOrCreateInstanceField("count", "I");
    }

    
    public static void debugwrite(String msg) {
        if (!jq.RunningNative) {
            System.err.println(msg);
            return;
        }
        HeapAddress value = (HeapAddress)HeapAddress.addressOf(msg).offset(_string_value.getOffset()).peek();
        int offset = HeapAddress.addressOf(msg).offset(_string_offset.getOffset()).peek4();
        int count = HeapAddress.addressOf(msg).offset(_string_count.getOffset()).peek4();
        Unsafe.pushArg(count);
        Unsafe.pushArgA(value.offset(offset*2));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(debugwwrite_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }
    
    public static void debugwrite(Utf8 msg) {
        msg.debugWrite();
    }
    
    public static void debugwrite(byte[] msg) {
        debugwrite(msg, msg.length);
    }
    
    public static void debugwrite(byte[] msg, int count) {
        Unsafe.pushArg(count);
        Unsafe.pushArgA(HeapAddress.addressOf(msg));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(debugwrite_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }
    
    public static void debugwriteln(String msg) {
        if (!jq.RunningNative) {
            System.err.println(msg);
            return;
        }
        HeapAddress value = (HeapAddress)HeapAddress.addressOf(msg).offset(_string_value.getOffset()).peek();
        int offset = HeapAddress.addressOf(msg).offset(_string_offset.getOffset()).peek4();
        int count = HeapAddress.addressOf(msg).offset(_string_count.getOffset()).peek4();
        Unsafe.pushArg(count);
        Unsafe.pushArgA(value.offset(offset*2));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(debugwwriteln_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }
    
    public static void debugwriteln(byte[] msg) {
        debugwriteln(msg, msg.length);
    }
    
    public static void debugwriteln(byte[] msg, int count) {
        Unsafe.pushArg(count);
        Unsafe.pushArgA(HeapAddress.addressOf(msg));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(debugwriteln_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }

    public static Address syscalloc(int size) {
        Unsafe.pushArg(size);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Address v = Unsafe.invokeA(syscalloc_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return null;
    }
    
    public static void sysfree(Address a) {
        Unsafe.pushArgA(a);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(sysfree_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }

    public static void die(int code) {
        Unsafe.pushArg(code);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(die_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) {
            throw new InternalError();
        }
    }
    
    public static final String DEFAULT_ENCODING = "ISO-8859-1";

    public static byte[] toCString(String s) {
        try {
            byte[] b = s.getBytes(DEFAULT_ENCODING);
            byte[] b2 = new byte[b.length+1];
            System.arraycopy(b, 0, b2, 0, b.length);
            return b2;
        } catch (java.io.UnsupportedEncodingException x) { return null; }
    }

    public static String fromCString(Address p) {
        int len;
        for (len=0; (byte)p.offset(len).peek1()!=(byte)0; ++len) ;
        byte[] b = new byte[len];
        mem_cpy(HeapAddress.addressOf(b), p, len);
        return new String(b);
    }
    
    public static long currentTimeMillis() {
        //if (!jq.RunningNative)
        //    return System.currentTimeMillis();
        //else
            try {
                Unsafe.getThreadBlock().disableThreadSwitch();
                long v = Unsafe.invoke(currentTimeMillis_0);
                Unsafe.getThreadBlock().enableThreadSwitch();
                return v;
            } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }

    public static void mem_cpy(Address to, Address from, int size) {
        Unsafe.pushArg(size);
        Unsafe.pushArgA(from);
        Unsafe.pushArgA(to);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(mem_cpy_12);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }

    public static void mem_set(Address to, byte b, int size) {
        Unsafe.pushArg(size);
        Unsafe.pushArg(b);
        Unsafe.pushArgA(to);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(mem_set_12);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }
    
    // constants from fcntl.h
    public static final int _O_RDONLY = 0x0000;
    public static final int _O_WRONLY = 0x0001;
    public static final int _O_RDWR   = 0x0002;
    public static final int _O_APPEND = 0x0008;
    public static final int _O_CREAT  = 0x0100;
    public static final int _O_TRUNC  = 0x0200;
    public static final int _O_EXCL   = 0x0400;
    public static final int _O_TEXT   = 0x4000;
    public static final int _O_BINARY = 0x8000;
    public static int file_open(String fn, int mode, int smode) {
        byte[] filename = toCString(fn);
        Unsafe.pushArg(smode);
        Unsafe.pushArg(mode);
        Unsafe.pushArgA(HeapAddress.addressOf(filename));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(file_open_12);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public abstract static class Stat {}
    public static int file_stat(String fn, Stat s) {
        byte[] filename = toCString(fn);
        Unsafe.pushArgA(HeapAddress.addressOf(s));
        Unsafe.pushArgA(HeapAddress.addressOf(filename));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(file_stat_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int file_readbytes(int fd, Address startAddress, int length) {
        Unsafe.pushArg(length);
        Unsafe.pushArgA(startAddress);
        Unsafe.pushArg(fd);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(file_readbytes_12);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int file_writebyte(int fd, int b) {
        Unsafe.pushArg(b);
        Unsafe.pushArg(fd);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(file_writebyte_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int file_writebytes(int fd, Address startAddress, int length) {
        Unsafe.pushArg(length);
        Unsafe.pushArgA(startAddress);
        Unsafe.pushArg(fd);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(file_writebytes_12);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int file_sync(int fd) {
        Unsafe.pushArg(fd);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(file_sync_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static final int SEEK_SET = 0; // from stdio.h
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;
    public static long file_seek(int fd, long offset, int origin) {
        Unsafe.pushArg((int)origin);
        Unsafe.pushArg((int)(offset>>32)); // hi
        Unsafe.pushArg((int)offset);       // lo
        Unsafe.pushArg(fd);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            long v = Unsafe.invoke(file_seek_16);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int file_close(int fd) {
        Unsafe.pushArg(fd);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(file_close_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    
    public static int console_available() {
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(console_available_0);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    
    public static int main_argc() {
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(main_argc_0);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int main_argv_length(int i) {
        Unsafe.pushArg(i);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(main_argv_length_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static void main_argv(int i, byte[] b) {
        Unsafe.pushArgA(HeapAddress.addressOf(b));
        Unsafe.pushArg(i);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(main_argv_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }
    
    public static int fs_getdcwd(int i, byte[] b) {
        Unsafe.pushArg(b.length);
        Unsafe.pushArgA(HeapAddress.addressOf(b));
        Unsafe.pushArg(i);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(fs_getdcwd_12);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int fs_fullpath(String s, byte[] b) {
        Unsafe.pushArg(b.length);
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s)));
        Unsafe.pushArgA(HeapAddress.addressOf(b));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(fs_fullpath_12);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static Address fs_gettruename(String s) {
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s)));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Address v = Unsafe.invokeA(fs_gettruename_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return null;
    }
    public static final int FILE_ATTRIBUTE_READONLY  = 0x001; // in mapiwin.h
    public static final int FILE_ATTRIBUTE_HIDDEN    = 0x002;
    public static final int FILE_ATTRIBUTE_SYSTEM    = 0x004;
    public static final int FILE_ATTRIBUTE_DIRECTORY = 0x010;
    public static final int FILE_ATTRIBUTE_ARCHIVE   = 0x020;
    public static final int FILE_ATTRIBUTE_NORMAL    = 0x080;
    public static final int FILE_ATTRIBUTE_TEMPORARY = 0x100;
    public static int fs_getfileattributes(String s) {
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s)));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(fs_getfileattributes_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int fs_access(String s, int mode) {
        Unsafe.pushArg(mode);
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s)));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(fs_access_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static long fs_getfiletime(String s) {
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s)));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            long v = Unsafe.invoke(fs_getfiletime_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0L;
    }
    public static long fs_stat_size(String s) {
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s)));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            long v = Unsafe.invoke(fs_stat_size_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0L;
    }
    public static int fs_remove(String s) {
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s)));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(fs_remove_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int fs_opendir(String s) {
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s)));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(fs_opendir_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static final int readdir_name_offset = 11;
    public static Address fs_readdir(int p) {
        Unsafe.pushArg(p);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Address v = Unsafe.invokeA(fs_readdir_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return null;
    }
    public static int fs_closedir(int p) {
        Unsafe.pushArg(p);
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(fs_closedir_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int fs_mkdir(String s) {
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s)));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(fs_mkdir_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int fs_rename(String s, String s1) {
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s1)));
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s)));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(fs_rename_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static final int _S_IEXEC  = 0x0000040; // from sys/stat.h
    public static final int _S_IWRITE = 0x0000080;
    public static final int _S_IREAD  = 0x0000100;
    public static int fs_chmod(String s, int mode) {
        Unsafe.pushArg(mode);
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s)));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(fs_chmod_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int fs_setfiletime(String s, long time) {
        Unsafe.pushArg((int)(time>>32)); // hi
        Unsafe.pushArg((int)time);       // lo
        Unsafe.pushArgA(HeapAddress.addressOf(toCString(s)));
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(fs_setfiletime_12);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int fs_getlogicaldrives() {
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(fs_getlogicaldrives_0);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static void yield() {
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(yield_0);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }
    public static void msleep(int ms) {
        try {
            Unsafe.pushArg(ms);
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(msleep_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }
    public static int/*CPointer*/ create_thread(CodeAddress start_address, HeapAddress param) {
        try {
            Unsafe.pushArgA(param);
            Unsafe.pushArgA(start_address);
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(create_thread_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int init_thread() {
        try {
            int v = (int)Unsafe.invoke(init_thread_0);
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int resume_thread(int/*CPointer*/ thread_handle) {
        try {
            Unsafe.pushArg(thread_handle);
            Assert._assert(!Unsafe.getThreadBlock().isThreadSwitchEnabled());
            int v = (int)Unsafe.invoke(resume_thread_4);
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int suspend_thread(int/*CPointer*/ thread_handle) {
        try {
            Unsafe.pushArg(thread_handle);
            Assert._assert(!Unsafe.getThreadBlock().isThreadSwitchEnabled());
            int v = (int)Unsafe.invoke(suspend_thread_4);
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    // from winnt.h
    public static final int THREAD_BASE_PRIORITY_LOWRT = 15;
    public static final int THREAD_BASE_PRIORITY_MAX = 2;
    public static final int THREAD_BASE_PRIORITY_MIN = -2;
    public static final int THREAD_BASE_PRIORITY_IDLE = -15;
    // from winbase.h
    public static final int THREAD_PRIORITY_LOWEST = THREAD_BASE_PRIORITY_MIN;
    public static final int THREAD_PRIORITY_BELOW_NORMAL = THREAD_PRIORITY_LOWEST+1;
    public static final int THREAD_PRIORITY_NORMAL = 0;
    public static final int THREAD_PRIORITY_HIGHEST = THREAD_BASE_PRIORITY_MAX;
    public static final int THREAD_PRIORITY_ABOVE_NORMAL = THREAD_PRIORITY_HIGHEST-1;
    public static final int THREAD_PRIORITY_TIME_CRITICAL = THREAD_BASE_PRIORITY_LOWRT;
    public static final int THREAD_PRIORITY_IDLE = THREAD_BASE_PRIORITY_IDLE;

    public static int set_thread_priority(int/*CPointer*/ thread_handle, int level) {
        try {
            Unsafe.pushArg(level);
            Unsafe.pushArg(thread_handle);
            Assert._assert(!Unsafe.getThreadBlock().isThreadSwitchEnabled());
            int v = (int)Unsafe.invoke(set_thread_priority_8);
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static StackAddress allocate_stack(int size) {
        try {
            Unsafe.pushArg(size);
            Unsafe.getThreadBlock().disableThreadSwitch();
            StackAddress v = (StackAddress)Unsafe.invokeA(allocate_stack_4);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return null;
    }
    public static int/*CPointer*/ get_current_thread_handle() {
        try {
            Assert._assert(!Unsafe.getThreadBlock().isThreadSwitchEnabled());
            int v = (int)Unsafe.invoke(get_current_thread_handle_0);
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static boolean get_thread_context(int pid, jq_RegisterState context) {
        try {
            Unsafe.pushArgA(HeapAddress.addressOf(context));
            Unsafe.pushArg(pid);
            Assert._assert(!Unsafe.getThreadBlock().isThreadSwitchEnabled());
            int v = (int)Unsafe.invoke(get_thread_context_8);
            return v!=0;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return false;
    }
    public static boolean set_thread_context(int pid, jq_RegisterState context) {
        try {
            Unsafe.pushArgA(HeapAddress.addressOf(context));
            Unsafe.pushArg(pid);
            Assert._assert(!Unsafe.getThreadBlock().isThreadSwitchEnabled());
            int v = (int)Unsafe.invoke(set_thread_context_8);
            return v!=0;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return false;
    }
    public static void set_current_context(jq_Thread thread, jq_RegisterState context) {
        try {
            Unsafe.pushArgA(HeapAddress.addressOf(context));
            Unsafe.pushArgA(HeapAddress.addressOf(thread));
            Assert._assert(!Unsafe.getThreadBlock().isThreadSwitchEnabled());
            Unsafe.invoke(set_current_context_8);
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }
    public static final int ITIMER_VIRTUAL = 1;
    public static void set_interval_timer(int type, int ms) {
        try {
            Unsafe.pushArg(ms);
            Unsafe.pushArg(type);
            Unsafe.getThreadBlock().disableThreadSwitch();
            Unsafe.invoke(set_interval_timer_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
        } catch (Throwable t) { Assert.UNREACHABLE(); }
    }
    public static int/*CPointer*/ init_semaphore() {
        try {
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(init_semaphore_0);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static final int INFINITE = -1;
    public static final int WAIT_ABANDONED = 0x00000080;
    public static final int WAIT_OBJECT_0  = 0x00000000;
    public static final int WAIT_TIMEOUT   = 0x00000102;
    public static int wait_for_single_object(int/*CPointer*/ obj, int timeout) {
        try {
            Unsafe.pushArg(timeout);
            Unsafe.pushArg(obj);
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(wait_for_single_object_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
    public static int release_semaphore(int/*CPointer*/ semaphore, int v1) {
        try {
            Unsafe.pushArg(v1);
            Unsafe.pushArg(semaphore);
            Unsafe.getThreadBlock().disableThreadSwitch();
            int v = (int)Unsafe.invoke(release_semaphore_8);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0;
    }
}
