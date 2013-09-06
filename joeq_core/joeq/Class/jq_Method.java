// jq_Method.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import joeq.ClassLib.ClassLibInterface;
import joeq.Compiler.BytecodeAnalysis.Bytecodes;
import joeq.Main.jq;
import joeq.UTF.Utf8;
import jwutil.util.Assert;
import jwutil.util.Convert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_Method.java,v 1.52 2004/09/22 22:17:28 joewhaley Exp $
 */
public abstract class jq_Method extends jq_Member {
    
    // Available after loading
    protected char max_stack;
    protected char max_locals;
    protected jq_Class[] thrown_exceptions_table;
    protected byte[] bytecode;
    protected jq_TryCatchBC[] exception_table;
    protected jq_LineNumberBC[] line_num_table;
    protected jq_LocalVarTableEntry[] localvar_table;
    protected Map codeattribMap;
    protected jq_Type[] param_types;
    protected jq_Type return_type;
    protected int param_words;
    
    // Available after compilation
    protected jq_CompiledCode default_compiled_version;
    
    // inherited: clazz, name, desc, access_flags, attributes
    protected jq_Method(jq_Class clazz, jq_NameAndDesc nd) {
        super(clazz, nd);
        parseMethodSignature();
    }
    
    public final void load(jq_Method that) {
        this.access_flags = that.access_flags;
        this.max_stack = that.max_stack;
        this.max_locals = that.max_locals;
        this.bytecode = that.bytecode;
        this.exception_table = that.exception_table;
        this.line_num_table = that.line_num_table;
        this.codeattribMap = that.codeattribMap;
        this.attributes = new HashMap();
        state = STATE_LOADED;
    }
    
    public final void load(char access_flags, char max_stack, char max_locals, byte[] bytecode,
                           jq_TryCatchBC[] exception_table, jq_LineNumberBC[] line_num_table,
                           Map codeattribMap) {
        this.access_flags = access_flags;
        this.max_stack = max_stack;
        this.max_locals = max_locals;
        this.bytecode = bytecode;
        this.exception_table = exception_table;
        this.line_num_table = line_num_table;
        this.codeattribMap = codeattribMap;
        this.attributes = new HashMap();
        state = STATE_LOADED;
        if (jq.RunningNative) {
            if (this instanceof jq_Initializer) {
                ClassLibInterface.DEFAULT.initNewConstructor((java.lang.reflect.Constructor)this.getJavaLangReflectMemberObject(), (jq_Initializer)this);
            } else {
                ClassLibInterface.DEFAULT.initNewMethod((java.lang.reflect.Method)this.getJavaLangReflectMemberObject(), this);
            }
        }
    }

    public final void load(char access_flags, Map attributes) throws ClassFormatError {
        super.load(access_flags, attributes);
        parseAttributes();
    }
    
    public final void load(char access_flags, DataInput in)
    throws IOException, ClassFormatError {
        super.load(access_flags, in);
        parseAttributes();
    }
    
    private final jq_Class getResolvedClassFromCP(char cpidx) {
    if (clazz.getCPtag(cpidx) != CONSTANT_ResolvedClass)
        throw new ClassFormatError();
    jq_Type class_type = clazz.getCPasType(cpidx);
    if (!class_type.isClassType())
        throw new ClassFormatError();
    return (jq_Class)class_type;
    }

    private final void parseAttributes() throws ClassFormatError {
        // parse attributes
        byte[] a = getAttribute("Code");
        if (a != null) {
            max_stack = Convert.twoBytesToChar(a, 0);
            max_locals = Convert.twoBytesToChar(a, 2);
            int bytecode_length = Convert.fourBytesToInt(a, 4);
            if (bytecode_length <= 0)
                throw new ClassFormatError();
            bytecode = new byte[bytecode_length];
            System.arraycopy(a, 8, bytecode, 0, bytecode_length);
            int ex_table_length = Convert.twoBytesToChar(a, 8+bytecode_length);
            exception_table = new jq_TryCatchBC[ex_table_length];
            int idx = 10+bytecode_length;
            for (int i=0; i<ex_table_length; ++i) {
                char start_pc = Convert.twoBytesToChar(a, idx);
                char end_pc = Convert.twoBytesToChar(a, idx+2);
                char handler_pc = Convert.twoBytesToChar(a, idx+4);
                char catch_cpidx = Convert.twoBytesToChar(a, idx+6);
                idx += 8;
                jq_Class catch_class = null;
                if (catch_cpidx != 0) {
            catch_class = getResolvedClassFromCP(catch_cpidx);
                }
                exception_table[i] = new jq_TryCatchBC(start_pc, end_pc, handler_pc, catch_class);
            }
            int attrib_count = Convert.twoBytesToChar(a, idx);
            codeattribMap = new HashMap();
            idx += 2;
            for (int i=0; i<attrib_count; ++i) {
                char name_index = Convert.twoBytesToChar(a, idx);
                if (clazz.getCPtag(name_index) != CONSTANT_Utf8)
                    throw new ClassFormatError();
                Utf8 attribute_desc = (Utf8)clazz.getCPasUtf8(name_index);
                int attribute_length = Convert.fourBytesToInt(a, idx+2);
                // todo: maybe we only want to parse attributes we care about...
                byte[] attribute_data = new byte[attribute_length];
                System.arraycopy(a, idx+6, attribute_data, 0, attribute_length);
                codeattribMap.put(attribute_desc, attribute_data);
                idx += 6 + attribute_length;
            }
            if (idx != a.length) {
                throw new ClassFormatError();
            }
            a = getCodeAttribute(Utf8.get("LineNumberTable"));
            if (a != null) {
                char num_of_line_attribs = Convert.twoBytesToChar(a, 0);
                if (a.length != (num_of_line_attribs*4+2))
                    throw new ClassFormatError();
                this.line_num_table = new jq_LineNumberBC[num_of_line_attribs];
                for (int i=0; i<num_of_line_attribs; ++i) {
                    char start_pc = Convert.twoBytesToChar(a, i*4+2);
                    char line_number = Convert.twoBytesToChar(a, i*4+4);
                    this.line_num_table[i] = new jq_LineNumberBC(start_pc, line_number);
                }
                Arrays.sort(this.line_num_table);
            } else {
                this.line_num_table = new jq_LineNumberBC[0];
            }
            a = getCodeAttribute(Utf8.get("LocalVariableTable"));
            if (a != null) {
                char num_of_local_vars = Convert.twoBytesToChar(a, 0);
                if (a.length != (num_of_local_vars*10+2))
                    throw new ClassFormatError();

                this.localvar_table = new jq_LocalVarTableEntry[num_of_local_vars];
                for (int i=0; i<num_of_local_vars; ++i) {
                    char start_pc = Convert.twoBytesToChar(a, i*10+2);
                    char length = Convert.twoBytesToChar(a, i*10+4);
                    char name_index = Convert.twoBytesToChar(a, i*10+6);
                    if (clazz.getCPtag(name_index) != CONSTANT_Utf8)
                        throw new ClassFormatError();
                    char desc_index = Convert.twoBytesToChar(a, i*10+8);
                    if (clazz.getCPtag(desc_index) != CONSTANT_Utf8)
                        throw new ClassFormatError();
                    char index = Convert.twoBytesToChar(a, i*10+10);
                    this.localvar_table[i] = new jq_LocalVarTableEntry(start_pc, length, new jq_NameAndDesc(clazz.getCPasUtf8(name_index), clazz.getCPasUtf8(desc_index)), index);
                }
                Arrays.sort(this.localvar_table);
            }
        } else {
            if (!isNative() && !isAbstract())
                throw new ClassFormatError();
        }
        // read thrown Exceptions
        a = getAttribute("Exceptions");
        if (a != null) {
            char number_of_thrown_exceptions = Convert.twoBytesToChar(a, 0);
        thrown_exceptions_table = new jq_Class[number_of_thrown_exceptions];
        for (int i = 0; i < number_of_thrown_exceptions; i++) {
        char cpidx = Convert.twoBytesToChar(a, 2 + 2*i);
        thrown_exceptions_table[i] = getResolvedClassFromCP(cpidx);
        }
    }
        state = STATE_LOADED;
        if (jq.RunningNative) {
            if (this instanceof jq_Initializer) {
                ClassLibInterface.DEFAULT.initNewConstructor((java.lang.reflect.Constructor)this.getJavaLangReflectMemberObject(), (jq_Initializer)this);
            } else {
                ClassLibInterface.DEFAULT.initNewMethod((java.lang.reflect.Method)this.getJavaLangReflectMemberObject(), this);
            }
        }
    }

    public Bytecodes.CodeException[] getExceptionTable(Bytecodes.InstructionList il) {
        Bytecodes.CodeException[] ex_table = new Bytecodes.CodeException[exception_table.length];
        for (int i=0; i<ex_table.length; ++i) {
            ex_table[i] = new Bytecodes.CodeException(il, bytecode, exception_table[i]);
        }
        return ex_table;
    }
    
    public Bytecodes.LineNumber[] getLineNumberTable(Bytecodes.InstructionList il) {
        Bytecodes.LineNumber[] line_num = new Bytecodes.LineNumber[line_num_table.length];
        for (int i=0; i<line_num.length; ++i) {
            line_num[i] = new Bytecodes.LineNumber(il, line_num_table[i]);
        }
        return line_num;
    }
    
    public void setCode(Bytecodes.InstructionList il,
                       Bytecodes.CodeException[] ex_table,
                       Bytecodes.LineNumber[] line_num,
                       jq_ConstantPool.ConstantPoolRebuilder cpr) {
        cpr.resetIndices(il);
        bytecode = il.getByteCode();
        if (exception_table.length != ex_table.length)
            exception_table = new jq_TryCatchBC[ex_table.length];
        // TODO: recalculate max_stack and max_locals.
        for (int i=0; i<ex_table.length; ++i) {
            exception_table[i] = ex_table[i].finish();
        }
        if (line_num_table.length != line_num.length)
            line_num_table = new jq_LineNumberBC[line_num.length];
        for (int i=0; i<line_num.length; ++i) {
            line_num_table[i] = line_num[i].finish();
        }
    }
    
    public void update(jq_ConstantPool.ConstantPoolRebuilder cpr) {
        if (bytecode != null) {
            Bytecodes.InstructionList il = new Bytecodes.InstructionList(getDeclaringClass().getCP(), bytecode);
            Bytecodes.CodeException[] ex_table = getExceptionTable(il);
            Bytecodes.LineNumber[] line_num = getLineNumberTable(il);
            setCode(il, ex_table, line_num, cpr);
        }
    }
    
    public void remakeCodeAttribute(jq_ConstantPool.ConstantPoolRebuilder cpr) {
        if (bytecode != null) {
            // TODO: include line number table.
            int size = 8 + bytecode.length + 2 + 8*exception_table.length + 2;
            byte[] code = new byte[size];
            Convert.charToTwoBytes(max_stack, code, 0);
            Convert.charToTwoBytes(max_locals, code, 2);
            Convert.intToFourBytes(bytecode.length, code, 4);
            System.arraycopy(bytecode, 0, code, 8, bytecode.length);
            Convert.charToTwoBytes((char)exception_table.length, code, 8+bytecode.length);
            int idx = 10+bytecode.length;
            for (int i=0; i<exception_table.length; ++i) {
                Convert.charToTwoBytes(exception_table[i].getStartPC(), code, idx);
                Convert.charToTwoBytes(exception_table[i].getEndPC(), code, idx+2);
                Convert.charToTwoBytes(exception_table[i].getHandlerPC(), code, idx+4);
                char c;
                if (exception_table[i].getExceptionType() == null) c = (char) 0;
                else c = cpr.get(exception_table[i].getExceptionType());
                Convert.charToTwoBytes(c, code, idx+6);
                idx += 8;
            }
            char attrib_count = (char)0; // TODO: code attributes
            // TODO: LocalVariableTable
            Convert.charToTwoBytes(attrib_count, code, idx);
            attributes.put(Utf8.get("Code"), code);
        }
    }
    
    public void dumpAttributes(DataOutput out, jq_ConstantPool.ConstantPoolRebuilder cpr) throws IOException {
        update(cpr);
        remakeCodeAttribute(cpr);
        // TODO: Exceptions
        super.dumpAttributes(out, cpr);
    }

    public abstract void prepare();

    static interface Delegate {
        jq_CompiledCode compile_stub(jq_Method m);
        jq_CompiledCode compile(jq_Method m);
    }

    private static Delegate _delegate;

    public final jq_CompiledCode compile_stub() {
        chkState(STATE_PREPARED);
        if (state >= STATE_SFINITIALIZED) return default_compiled_version;
        if (jq.DontCompile) {
            state = STATE_SFINITIALIZED;
            return default_compiled_version = new jq_CompiledCode(this, null, 0, null, null, null, null, 0, null, null);
        }
        if (_compile.getState() < STATE_CLSINITIALIZED) _compile.compile();
        default_compiled_version = _delegate.compile_stub(this);
        state = STATE_SFINITIALIZED;
        return default_compiled_version;
    }
    public final jq_CompiledCode compile() {
        if (state == STATE_CLSINITIALIZED) return default_compiled_version;
        synchronized (this) {
            Assert._assert(!jq.DontCompile);
            chkState(STATE_PREPARED);
            default_compiled_version = _delegate.compile(this);
            state = STATE_CLSINITIALIZED;
        }
        return default_compiled_version;
    }
    
    public final void setDefaultCompiledVersion(jq_CompiledCode cc) {
        default_compiled_version = cc;
    }
    
    public final int getReturnWords() {
        if (return_type == jq_Primitive.VOID) return 0;
        if (return_type == jq_Primitive.LONG ||
            return_type == jq_Primitive.DOUBLE) return 2;
        return 1;
    }
    
    protected abstract void parseMethodSignature();
    public final boolean isSynchronized() { return checkAccessFlag(ACC_SYNCHRONIZED); }
    public final boolean isNative() { return checkAccessFlag(ACC_NATIVE); }
    public final boolean isAbstract() { return checkAccessFlag(ACC_ABSTRACT); }
    public final boolean isStrict() { return checkAccessFlag(ACC_STRICT); }
    public final jq_CompiledCode getDefaultCompiledVersion() { chkState(STATE_SFINITIALIZED); return default_compiled_version; }
    public char getMaxStack() {
        chkState(STATE_LOADED);
        Assert._assert(getBytecode() != null);
        return max_stack;
    }
    public void setMaxStack(char m) {
        this.max_stack = m;
    }
    public char getMaxLocals() {
        chkState(STATE_LOADED);
        Assert._assert(getBytecode() != null);
        return max_locals;
    }
    public void setMaxLocals(char m) {
        this.max_locals = m;
    }
    public byte[] getBytecode() {
        chkState(STATE_LOADED);
        return bytecode;
    }
    public jq_TryCatchBC[] getExceptionTable() {
        chkState(STATE_LOADED);
        Assert._assert(getBytecode() != null);
        return exception_table;
    }
    public jq_Class[] getThrownExceptionsTable() { 
        chkState(STATE_LOADED);
    return thrown_exceptions_table; 
    }
    public jq_LocalVarTableEntry getLocalVarTableEntry(int bci, int index) {
        if (localvar_table == null)
            return null;
        int inspoint = Arrays.binarySearch(localvar_table, new jq_LocalVarTableEntry((char)bci, (char)index));
        if (inspoint >= 0)
            return localvar_table[inspoint];
        inspoint = -inspoint-2;
        if(inspoint >= 0 && localvar_table[inspoint].isInRange(bci, index))
            return localvar_table[inspoint];
        return null;
    }
    public int getLineNumber(int bci) {
        // todo: binary search
        if (line_num_table == null)
            return -1;
        for (int i=line_num_table.length-1; i>=0; --i) {
            if (bci >= line_num_table[i].getStartPC()) return line_num_table[i].getLineNum();
        }
        return -1;
    }
    public jq_LineNumberBC[] getLineNumberTable() { return line_num_table; }
    public jq_Type[] getParamTypes() { return param_types; }
    public int getParamWords() { return param_words; }
    public final jq_Type getReturnType() { return return_type; }
    public byte[] getCodeAttribute(Utf8 a) { chkState(STATE_LOADING2); return (byte[])codeattribMap.get(a); }
    public final byte[] getCodeAttribute(String name) { return getCodeAttribute(Utf8.get(name)); }

    public jq_LineNumberBC getLineNumber(char linenum) {
        // todo: binary search
        jq_LineNumberBC[] ln = getLineNumberTable();
        if (ln == null)
            return null;
        for (int i = 0; i < ln.length; ++i) {
            if (ln[i].getLineNum() == linenum)
                return ln[i];
        }
        return null;
    }

    public void accept(jq_MethodVisitor mv) {
        mv.visitMethod(this);
    }
    
    public String toString() { return getDeclaringClass()+"."+nd; }

    public boolean isBodyLoaded() {
        return getBytecode()!=null;
    }
    
    public static final jq_Class _class;
    public static final jq_InstanceMethod _compile;
    public static final jq_InstanceField _default_compiled_version;
    static {
        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_Method;");
        _compile = _class.getOrCreateInstanceMethod("compile", "()Ljoeq/Class/jq_CompiledCode;");
        _default_compiled_version = _class.getOrCreateInstanceField("default_compiled_version", "Ljoeq/Class/jq_CompiledCode;");
        /* Set up delegates. */
        _delegate = null;
        boolean nullVM = jq.nullVM;
        if (!nullVM) {
            _delegate = attemptDelegate("joeq.Class.Delegates$Method");
        }
        if (_delegate == null) {
            _delegate = new NullDelegates.Method();
        }
    }

    private static Delegate attemptDelegate(String s) {
        String type = "method delegate";
        try {
            Class c = Class.forName(s);
            return (Delegate)c.newInstance();
        } catch (java.lang.ClassNotFoundException x) {
            //System.err.println("Cannot find "+type+" "+s+": "+x);
        } catch (java.lang.InstantiationException x) {
            //System.err.println("Cannot instantiate "+type+" "+s+": "+x);
        } catch (java.lang.IllegalAccessException x) {
            //System.err.println("Cannot access "+type+" "+s+": "+x);
        }
        return null;
    }
}
