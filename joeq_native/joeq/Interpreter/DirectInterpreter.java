// DirectInterpreter.java, created Mon Feb  5 23:23:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Interpreter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import joeq.Allocator.ObjectLayout;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_Method;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_Type;
import joeq.Main.TraceFlags;
import joeq.Main.jq;
import joeq.Memory.Address;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.Monitor;
import joeq.Runtime.Reflection;
import joeq.Runtime.SystemInterface;
import joeq.Runtime.TypeCheck;
import joeq.Runtime.Unsafe;
import joeq.UTF.Utf8;
import jwutil.collections.Filter;
import jwutil.util.Assert;
import jwutil.util.Convert;

/**
 * DirectInterpreter
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: DirectInterpreter.java,v 1.20 2004/10/08 08:43:01 joewhaley Exp $
 */
public class DirectInterpreter extends BytecodeInterpreter {

    /** Creates new DirectInterpreter */
    public DirectInterpreter(State initialState) {
        super(new DirectVMInterface(), initialState);
    }

    public static final Set bad_classes;
    public static final Set bad_methods;
    public static final Filter interpret_filter;
    static {
        bad_classes = new HashSet();
        bad_classes.add(SystemInterface._class);
        bad_classes.add(Reflection._class);
        bad_classes.add(PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Runtime/ExceptionDeliverer;"));
        bad_methods = new HashSet();
        bad_methods.add(joeq.Runtime.Arrays._multinewarray);
        interpret_filter = new Filter() {
            public boolean isElement(Object o) {
                jq_Method m = (jq_Method)o;
                if (m.getBytecode() == null) return false;
                if (bad_classes.contains(m.getDeclaringClass())) return false;
                if (bad_methods.contains(m)) return false;
                return true;
            }
        };
    }

    public Object invokeMethod(jq_Method m) throws Throwable {
        //Debug.writeln("Enter: "+m);
        jq_Class k = m.getDeclaringClass();
        k.cls_initialize();
        if (!interpret_filter.isElement(m)) {
            //Debug.writeln("Native call: "+m);
            Object result = invokeMethod(m, null);
            //Debug.writeln("Result: "+result);
            return result;
        }
        int localsize = m.getMaxLocals() * HeapAddress.size();
        int stacksize = m.getMaxStack() * HeapAddress.size();
        StackAddress newframe = StackAddress.alloca(localsize+stacksize);
        DirectState callee = new DirectState((StackAddress) newframe.offset(localsize+stacksize), (StackAddress) newframe.offset(stacksize), m.getMaxLocals());
        Object result = this.invokeMethod(m, callee);
        //Debug.writeln("Leave: "+m);
        return result;
    }
    
    // callee == null -> call compiled version
    public Object invokeMethod(jq_Method m, State callee) throws Throwable {
        //Runtime.SystemInterface.debugwriteln("Invoking method "+m);
        jq_Class k = m.getDeclaringClass();
        Assert._assert(k.isClsInitialized());
        Assert._assert(m.getBytecode() != null);
        jq_Type[] paramTypes = m.getParamTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i=paramTypes.length-1; i>=0; --i) {
            jq_Type t = paramTypes[i];
            if (t.isPrimitiveType()) {
                if (t == jq_Primitive.LONG) {
                    params[i] = new Long(istate.pop_L());
                } else if (t == jq_Primitive.FLOAT) {
                    params[i] = new Float(istate.pop_F());
                } else if (t == jq_Primitive.DOUBLE) {
                    params[i] = new Double(istate.pop_D());
                } else {
                    params[i] = new Integer(istate.pop_I());
                }
            } else {
                params[i] = istate.pop_A();
            }
            //System.out.println("Param "+i+": "+params[i]);
        }
        for (int i=0, j=0; i<paramTypes.length; ++i, ++j) {
            jq_Type t = paramTypes[i];
            if (t.isPrimitiveType()) {
                if (t == jq_Primitive.LONG) {
                    long v = ((Long)params[i]).longValue();
                    if (callee == null) {
                        Unsafe.pushArg((int)(v>>32)); // hi
                        Unsafe.pushArg((int)v);       // lo
                    } else callee.setLocal_L(j, v);
                    ++j;
                } else if (t == jq_Primitive.FLOAT) {
                    float v = ((Float)params[i]).floatValue();
                    if (callee == null) {
                        Unsafe.pushArg(Float.floatToRawIntBits(v));
                    } else callee.setLocal_F(j, v);
                } else if (t == jq_Primitive.DOUBLE) {
                    long v = Double.doubleToRawLongBits(((Double)params[i]).doubleValue());
                    if (callee == null) {
                        Unsafe.pushArg((int)(v>>32)); // hi
                        Unsafe.pushArg((int)v);       // lo
                    } else callee.setLocal_D(j, Double.longBitsToDouble(v));
                    ++j;
                } else {
                    int v = ((Integer)params[i]).intValue();
                    if (callee == null) {
                        Unsafe.pushArg(v);
                    } else callee.setLocal_I(j, v);
                }
            } else {
                Object v = params[i];
                if (callee == null) {
                    Unsafe.pushArgA(HeapAddress.addressOf(v));
                } else callee.setLocal_A(j, v);
            }
        }
        if (callee == null) {
            jq_Type returnType = m.getReturnType();
            if (returnType.isReferenceType()) {
                Address result = Unsafe.invokeA(m.getDefaultCompiledVersion().getEntrypoint());
                if (returnType.isAddressType()) return result;
                return ((HeapAddress) result).asObject();
            }
            long result = Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint());
            if (returnType == jq_Primitive.VOID)
                return null;
            else if (returnType == jq_Primitive.LONG)
                return new Long(result);
            else if (returnType == jq_Primitive.FLOAT)
                return new Float(Float.intBitsToFloat((int)(result)));
            else if (returnType == jq_Primitive.DOUBLE)
                return new Double(Double.longBitsToDouble(result));
            else
                return new Integer((int)(result));
        } else {
            State oldState = this.istate;
            this.istate = callee;
            MethodInterpreter mi = new MethodInterpreter(m);
            Object synchobj = null;
            try {
                if (m.isSynchronized()) {
                    if (!m.isStatic()) {
                        if (mi.getTraceFlag()) mi.getTraceOut().println("synchronized instance method, locking 'this' object");
                        vm.monitorenter(synchobj = istate.getLocal_A(0), mi);
                    } else {
                        if (mi.getTraceFlag()) mi.getTraceOut().println("synchronized static method, locking class object");
                        vm.monitorenter(synchobj = Reflection.getJDKType(m.getDeclaringClass()), mi);
                    }
                }
                mi.forwardTraversal();
                this.istate = oldState;
                if (m.isSynchronized()) {
                    if (mi.getTraceFlag()) mi.getTraceOut().println("exiting synchronized method, unlocking object");
                    vm.monitorexit(synchobj);
                }
                jq_Type returnType = m.getReturnType();
                Object retval;
                if (returnType.isReferenceType()) {
                    retval = callee.getReturnVal_A();
                } else if (returnType == jq_Primitive.VOID) {
                    retval = null;
                } else if (returnType == jq_Primitive.LONG) {
                    retval = new Long(callee.getReturnVal_L());
                } else if (returnType == jq_Primitive.FLOAT) {
                    retval = new Float(callee.getReturnVal_F());
                } else if (returnType == jq_Primitive.DOUBLE) {
                    retval = new Double(callee.getReturnVal_D());
                } else {
                    retval = new Integer(callee.getReturnVal_I());
                }
                if (mi.getTraceFlag())
                    mi.getTraceOut().println("Return value: "+retval);
                return retval;
            } catch (WrappedException ix) {
                this.istate = oldState;
                if (m.isSynchronized()) {
                    if (mi.getTraceFlag()) mi.getTraceOut().println("exiting synchronized method, unlocking object");
                    vm.monitorexit(synchobj);
                }
                throw ix.t;
            }
        }
    }

    public Object invokeUnsafeMethod(jq_Method f) throws Throwable {
        if (f == Unsafe._intBitsToFloat) {
            return new Float(istate.pop_F());
        } else if (f == Unsafe._floatToIntBits) {
            return new Integer(istate.pop_I());
        } else if (f == Unsafe._doubleToLongBits) {
            return new Long(istate.pop_L());
        } else if (f == Unsafe._longBitsToDouble) {
            return new Double(istate.pop_D());
        } else if (f == Unsafe._getThreadBlock) {
            return Unsafe.getThreadBlock();
        } else if (f.getName() == Utf8.get("to32BitValue")) {
            return new Integer(((Address) istate.pop()).to32BitValue());
        } else if (f.getName() == Utf8.get("addressOf")) {
            return HeapAddress.addressOf(istate.pop_A());
        } else if (f.getName() == Utf8.get("asObject")) {
            return ((HeapAddress)istate.pop()).asObject();
        } else if (f.getName() == Utf8.get("offset")) {
            int i = istate.pop_I();
            return ((Address)istate.pop()).offset(i);
        } else if (f.getName() == Utf8.get("asReferenceType")) {
            return (jq_Reference) ((HeapAddress)istate.pop()).asObject();
        } else if (f.getName() == Utf8.get("peek")) {
            return ((Address)istate.pop()).peek();
        } else if (f.getName() == Utf8.get("peek1")) {
            return new Integer(((Address)istate.pop()).peek1());
        } else if (f.getName() == Utf8.get("peek2")) {
            return new Integer(((Address)istate.pop()).peek2());
        } else if (f.getName() == Utf8.get("peek4")) {
            return new Integer(((Address)istate.pop()).peek4());
        } else if (f.getName() == Utf8.get("peek8")) {
            return new Long(((Address)istate.pop()).peek8());
        } else if (f.getName() == Utf8.get("poke")) {
            Address v = (Address) istate.pop();
            Address a = (Address) istate.pop();
            a.poke(v);
            return null;
        } else if (f.getName() == Utf8.get("poke1")) {
            byte v = (byte) istate.pop_I();
            Address a = (Address) istate.pop();
            a.poke1(v);
            return null;
        } else if (f.getName() == Utf8.get("poke2")) {
            short v = (short) istate.pop_I();
            Address a = (Address) istate.pop();
            a.poke2(v);
            return null;
        } else if (f.getName() == Utf8.get("poke4")) {
            int v = istate.pop_I();
            Address a = (Address) istate.pop();
            a.poke4(v);
            return null;
        } else if (f.getName() == Utf8.get("poke8")) {
            long v = istate.pop_L();
            Address a = (Address) istate.pop();
            a.poke8(v);
            return null;
        } else {
            System.err.println(f.toString());
            Assert.UNREACHABLE();
            return null;
        }
    }
        
    public static class DirectState extends BytecodeInterpreter.State {
        final StackAddress fp;
        final int nlocals;
        StackAddress sp;
        int loResult, hiResult;
        
        public DirectState(StackAddress fp, StackAddress sp, int nlocals) {
            this.fp = fp; this.sp = sp;
            this.nlocals = nlocals;
        }

        public void fillInParameters(jq_Type[] paramTypes, Object[] incomingArgs) {
            for (int i=0, j=0; i<paramTypes.length; ++i, ++j) {
                jq_Type t = paramTypes[i];
                if (t.isReferenceType()) {
                    push_A(incomingArgs[j]);
                } else if (t.isIntLike()) {
                    push_I(Convert.unwrapToInt(incomingArgs[j]));
                } else if (t == jq_Primitive.FLOAT) {
                    push_F(Convert.unwrapToFloat(incomingArgs[j]));
                } else if (t == jq_Primitive.LONG) {
                    push_L(Convert.unwrapToLong(incomingArgs[j]));
                    ++j;
                } else if (t == jq_Primitive.DOUBLE) {
                    push_D(Convert.unwrapToDouble(incomingArgs[j]));
                    ++j;
                } else {
                    Assert.UNREACHABLE();
                }
            }
        }
        
        public void push_I(int v) {
            sp = (StackAddress) sp.offset(-HeapAddress.size());
            sp.poke4(v);
        }
        public void push_L(long v) {
            push_I((int)(v>>32)); push_I((int)v); // hi, lo
        }
        public void push_F(float v) {
            push_I(Float.floatToRawIntBits(v));
        }
        public void push_D(double v) {
            push_L(Double.doubleToRawLongBits(v));
        }
        public void push_A(Object v) {
            push_R(HeapAddress.addressOf(v));
        }
        public void push_R(Address v) {
            sp = (StackAddress) sp.offset(-HeapAddress.size());
            sp.poke(v);
        }
        public void push(Object v) {
            push_A(v);
        }
        public int pop_I() {
            int v = sp.peek4();
            sp = (StackAddress) sp.offset(HeapAddress.size());
            return v;
        }
        public long pop_L() {
            int lo=pop_I(); int hi=pop_I();
            return Convert.twoIntsToLong(lo, hi); // lo, hi
        }
        public float pop_F() {
            return Float.intBitsToFloat(pop_I());
        }
        public double pop_D() {
            return Double.longBitsToDouble(pop_L());
        }
        public Object pop_A() {
            return ((HeapAddress) pop_R()).asObject();
        }
        public Address pop_R() {
            Address v = sp.peek();
            sp = (StackAddress) sp.offset(HeapAddress.size());
            return v;
        }
        public Object pop() {
            return pop_A();
        }
        public void popAll() {
            sp = (StackAddress) fp.offset(-(nlocals * HeapAddress.size()));
        }
        public Object peek_A(int depth) {
            HeapAddress v = (HeapAddress) sp.offset(depth * HeapAddress.size()).peek();
            return v.asObject();
        }
        public void setLocal_I(int i, int v) {
            fp.offset(-(i * HeapAddress.size())).poke4(v);
        }
        public void setLocal_L(int i, long v) {
            setLocal_I(i, (int)(v>>32)); setLocal_I(i+1, (int)v); // hi, lo
        }
        public void setLocal_F(int i, float v) {
            setLocal_I(i, Float.floatToRawIntBits(v));
        }
        public void setLocal_D(int i, double v) {
            setLocal_L(i, Double.doubleToRawLongBits(v));
        }
        public void setLocal_A(int i, Object v) {
            setLocal_R(i, HeapAddress.addressOf(v));
        }
        public void setLocal_R(int i, Address v) {
            fp.offset(-(i * HeapAddress.size())).poke(v);
        }
        public int getLocal_I(int i) {
            return fp.offset(-(i * HeapAddress.size())).peek4();
        }
        public long getLocal_L(int i) {
            int lo=getLocal_I(i+1); int hi=getLocal_I(i); // lo, hi
            return Convert.twoIntsToLong(lo, hi);
        }
        public float getLocal_F(int i) {
            return Float.intBitsToFloat(getLocal_I(i));
        }
        public double getLocal_D(int i) {
            return Double.longBitsToDouble(getLocal_L(i));
        }
        public Object getLocal_A(int i) {
            return ((HeapAddress) getLocal_R(i)).asObject();
        }
        public Address getLocal_R(int i) {
            return fp.offset(-(i * HeapAddress.size())).peek();
        }
        public void return_I(int v) {
            loResult = v;
        }
        public void return_L(long v) {
            loResult = (int)(v>>32); hiResult = (int)v;
        }
        public void return_F(float v) {
            loResult = Float.floatToRawIntBits(v);
        }
        public void return_D(double v) {
            return_L(Double.doubleToRawLongBits(v));
        }
        public void return_A(Object v) {
            loResult = HeapAddress.addressOf(v).to32BitValue();
        }
        public void return_V() {}
        public int getReturnVal_I() {
            return loResult;
        }
        public long getReturnVal_L() {
            return Convert.twoIntsToLong(loResult, hiResult);
        }
        public float getReturnVal_F() {
            return Float.intBitsToFloat(loResult);
        }
        public double getReturnVal_D() {
            return Double.longBitsToDouble(getReturnVal_L());
        }
        public Object getReturnVal_A() {
            return ((HeapAddress) getReturnVal_R()).asObject();
        }
        public Address getReturnVal_R() {
            return HeapAddress.address32(loResult);
        }
    }
    
    public static class DirectVMInterface extends BytecodeInterpreter.VMInterface {
        public int getstatic_I(jq_StaticField f) { return f.getAddress().peek4(); }
        public long getstatic_L(jq_StaticField f) { return f.getAddress().peek8(); }
        public float getstatic_F(jq_StaticField f) { return Float.intBitsToFloat(getstatic_I(f)); }
        public double getstatic_D(jq_StaticField f) { return Double.longBitsToDouble(getstatic_L(f)); }
        public Object getstatic_A(jq_StaticField f) { return ((HeapAddress) f.getAddress().peek()).asObject(); }
        public byte getstatic_B(jq_StaticField f) { return (byte)f.getAddress().peek4(); }
        public char getstatic_C(jq_StaticField f) { return (char)f.getAddress().peek4(); }
        public short getstatic_S(jq_StaticField f) { return (short)f.getAddress().peek4(); }
        public boolean getstatic_Z(jq_StaticField f) { return f.getAddress().peek4()!=0; }
        public void putstatic_I(jq_StaticField f, int v) { f.getDeclaringClass().setStaticData(f, v); }
        public void putstatic_L(jq_StaticField f, long v) { f.getDeclaringClass().setStaticData(f, v); }
        public void putstatic_F(jq_StaticField f, float v) { f.getDeclaringClass().setStaticData(f, v); }
        public void putstatic_D(jq_StaticField f, double v) { f.getDeclaringClass().setStaticData(f, v); }
        public void putstatic_A(jq_StaticField f, Object v) { f.getDeclaringClass().setStaticData(f, v); }
        public void putstatic_Z(jq_StaticField f, boolean v) { f.getDeclaringClass().setStaticData(f, v?1:0); }
        public void putstatic_B(jq_StaticField f, byte v) { f.getDeclaringClass().setStaticData(f, v); }
        public void putstatic_C(jq_StaticField f, char v) { f.getDeclaringClass().setStaticData(f, v); }
        public void putstatic_S(jq_StaticField f, short v) { f.getDeclaringClass().setStaticData(f, v); }
        public int getfield_I(Object o, jq_InstanceField f) { return HeapAddress.addressOf(o).offset(f.getOffset()).peek4(); }
        public long getfield_L(Object o, jq_InstanceField f) { return HeapAddress.addressOf(o).offset(f.getOffset()).peek8(); }
        public float getfield_F(Object o, jq_InstanceField f) { return Float.intBitsToFloat(getfield_I(o, f)); }
        public double getfield_D(Object o, jq_InstanceField f) { return Double.longBitsToDouble(getfield_L(o, f)); }
        public Object getfield_A(Object o, jq_InstanceField f) { return ((HeapAddress)HeapAddress.addressOf(o).offset(f.getOffset()).peek()).asObject(); }
        public byte getfield_B(Object o, jq_InstanceField f) { return HeapAddress.addressOf(o).offset(f.getOffset()).peek1(); }
        public char getfield_C(Object o, jq_InstanceField f) { return (char)HeapAddress.addressOf(o).offset(f.getOffset()).peek4(); }
        public short getfield_S(Object o, jq_InstanceField f) { return (short)HeapAddress.addressOf(o).offset(f.getOffset()).peek2(); }
        public boolean getfield_Z(Object o, jq_InstanceField f) { return HeapAddress.addressOf(o).offset(f.getOffset()).peek1() != (byte)0; }
        public void putfield_I(Object o, jq_InstanceField f, int v) { HeapAddress.addressOf(o).offset(f.getOffset()).poke4(v); }
        public void putfield_L(Object o, jq_InstanceField f, long v) { HeapAddress.addressOf(o).offset(f.getOffset()).poke8(v); }
        public void putfield_F(Object o, jq_InstanceField f, float v) { putfield_I(o, f, Float.floatToRawIntBits(v)); }
        public void putfield_D(Object o, jq_InstanceField f, double v) { putfield_L(o, f, Double.doubleToRawLongBits(v)); }
        public void putfield_A(Object o, jq_InstanceField f, Object v) { HeapAddress.addressOf(o).offset(f.getOffset()).poke(HeapAddress.addressOf(v)); }
        public void putfield_B(Object o, jq_InstanceField f, byte v) { HeapAddress.addressOf(o).offset(f.getOffset()).poke1(v); }
        public void putfield_C(Object o, jq_InstanceField f, char v) { HeapAddress.addressOf(o).offset(f.getOffset()).poke2((short)((v<<16)>>16)); }
        public void putfield_S(Object o, jq_InstanceField f, short v) { HeapAddress.addressOf(o).offset(f.getOffset()).poke2(v); }
        public void putfield_Z(Object o, jq_InstanceField f, boolean v) { HeapAddress.addressOf(o).offset(f.getOffset()).poke1(v?(byte)1:(byte)0); }
        public Object new_obj(jq_Type t) { return ((jq_Class)t).newInstance(); }
        public Object new_array(jq_Type t, int length) { return ((jq_Array)t).newInstance(length); }
        public Object checkcast(Object o, jq_Type t) {
            if (t.isAddressType()) return o;
            return TypeCheck.checkcast(o, t);
        }
        public boolean instance_of(Object o, jq_Type t) { return TypeCheck.instance_of(o, t); }
        public int arraylength(Object o) { return HeapAddress.addressOf(o).offset(ObjectLayout.ARRAY_LENGTH_OFFSET).peek4(); }
        public void monitorenter(Object o, MethodInterpreter v) { Monitor.monitorenter(o); }
        public void monitorexit(Object o) { Monitor.monitorexit(o); }
        public Object multinewarray(int[] dims, jq_Type t) { return joeq.Runtime.Arrays.multinewarray_helper(dims, 0, (jq_Array)t); }
        public jq_Reference getJQTypeOf(Object o) { return jq_Reference.getTypeOf(o); }
    }

    // Invoke reflective interpreter from command line.
    public static void main(String[] s_args) throws Throwable {
        String s = s_args[0];
        int dotloc = s.lastIndexOf('.');
        String rootMethodClassName = s.substring(0, dotloc);
        String rootMethodName = s.substring(dotloc+1);
        
        Assert._assert(jq.RunningNative);
        
        jq_Class c = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("L"+rootMethodClassName.replace('.','/')+";");
        c.cls_initialize();

        jq_StaticMethod rootm = null;
        Utf8 rootm_name = Utf8.get(rootMethodName);
        for(Iterator it = Arrays.asList(c.getDeclaredStaticMethods()).iterator();
            it.hasNext(); ) {
            jq_StaticMethod m = (jq_StaticMethod)it.next();
            if (m.getName() == rootm_name) {
                rootm = m;
                break;
            }
        }
        if (rootm == null)
            Assert.UNREACHABLE("root method not found: "+rootMethodClassName+"."+rootMethodName);
        Object[] args = new Object[rootm.getParamWords()];
        jq_Type[] paramTypes = rootm.getParamTypes();
        for (int i=0, j=0; i<paramTypes.length; ++i) {
            j = TraceFlags.parseArg(args, i, paramTypes[i], s_args, j);
        }
        StackAddress newframe = StackAddress.alloca(args.length * HeapAddress.size());
        StackAddress fp = (StackAddress) newframe.offset(args.length * HeapAddress.size());
        StackAddress sp = fp;
        DirectState initialState = new DirectState(fp, sp, 0);
        initialState.fillInParameters(paramTypes, args);
        Object retval = new DirectInterpreter(initialState).invokeMethod(rootm);
        System.out.println("Return value: "+retval);
    }
    
}
