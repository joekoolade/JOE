/*
 * Created on Sep 24, 2003
 *
 * Object creation methods that are used by the
 * metalizer. 
 * 
 */
package baremetal.kernel;
import baremetal.kernel.JVM;

/**
 * @author Joe Kulig
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
final class ObjectFactory {
	static int classTableAddr;
	static Heap heap;

	/*
	 * offset of class id
	 */
	final static int CLASSID = 0;
	/*
	 * offset of class array type
	 */
	final static int CLASSTYPE = 0x4;
	/*
	 * offset of page field
	 */
	final static int PAGE = 0x8;
	/*
	 * offset of object size field
	 */
	final static int OBJECT_SIZE = 0xc;
	final static int CLASS_OBJECT_SIZE = 0x8;
	/*
	 * offset of first object field
	 */
	final static int OBJECT_OFFSET = 0x10;
	final static int OBJECT_HEADER_SIZE = 0x10;
	/*
	 * offset of array field. Points to the beginning
	 * of the array
	 */
	 final static int ARRAY_FIELD = 0x10;
	 final static int ARRAY_START = 0x14;
	 
	private ObjectFactory() {
	}
	
	static int newClass(int classId) {
		int objectTemplate = Memory.readWord(classTableAddr+(classId<<2));
		int size = Memory.readWord(objectTemplate+CLASS_OBJECT_SIZE);
		int object = Heap.allocate(size+OBJECT_HEADER_SIZE);
		Memory.writeWord(object+OBJECT_SIZE, size);
		Memory.writeWord(object, classId);
		return object;
	}
	
	static int newIntArray(int size) {
		size<<=2;
		int object = Heap.allocate(size+OBJECT_HEADER_SIZE);
		Memory.writeWord(object+OBJECT_SIZE, size);
		Memory.writeWord(object, JVM.T_INT);
		Memory.writeWord(object+ARRAY_FIELD, object+ARRAY_START);
		return object;
	}
	
	static int newCharArray(int size) {
		size<<=2;
		int object = Heap.allocate(size+OBJECT_HEADER_SIZE);
		Memory.writeWord(object+OBJECT_SIZE, size);
		Memory.writeWord(object, JVM.T_CHAR);
		Memory.writeWord(object+ARRAY_FIELD, object+ARRAY_START);
		return object;
	}

	static int newLongArray(int size) {
		size<<=3;
		int object = Heap.allocate(size+OBJECT_HEADER_SIZE);
		Memory.writeWord(object+OBJECT_SIZE, size);
		Memory.writeWord(object, JVM.T_LONG);
		Memory.writeWord(object+ARRAY_FIELD, object+ARRAY_START);
		return object;
	}
	
	static int newFloatArray(int size) {
		size<<=2;
		int object = Heap.allocate(size+OBJECT_HEADER_SIZE);
		Memory.writeWord(object+OBJECT_SIZE, size);
		Memory.writeWord(object, JVM.T_FLOAT);
		Memory.writeWord(object+ARRAY_FIELD, object+ARRAY_START);
		return object;
	}
	
	static int newDoubleArray(int size) {
		size<<=3;
		int object = Heap.allocate(size+OBJECT_HEADER_SIZE);
		Memory.writeWord(object+OBJECT_SIZE, size);
		Memory.writeWord(object, JVM.T_DOUBLE);
		Memory.writeWord(object+ARRAY_FIELD, object+ARRAY_START);
		return object;
	}
	
	static int newByteArray(int size) {
		size<<=2;
		int object = Heap.allocate(size+OBJECT_HEADER_SIZE);
		Memory.writeWord(object+OBJECT_SIZE, size);
		Memory.writeWord(object, JVM.T_BYTE);
		Memory.writeWord(object+ARRAY_FIELD, object+ARRAY_START);
		return object;
	}
	
	static int newBooleanArray(int size) {
		size<<=2;
		int object = Heap.allocate(size+OBJECT_HEADER_SIZE);
		Memory.writeWord(object+OBJECT_SIZE, size);
		Memory.writeWord(object, JVM.T_BOOLEAN);
		Memory.writeWord(object+ARRAY_FIELD, object+ARRAY_START);
		return object;
	}
	
	static int newShortArray(int size) {
		size<<=2;
		int object = Heap.allocate(size+OBJECT_HEADER_SIZE);
		Memory.writeWord(object+OBJECT_SIZE, size);
		Memory.writeWord(object, JVM.T_SHORT);
		Memory.writeWord(object+ARRAY_FIELD, object+ARRAY_START);
		return object;
	}
	
	static int newClassArray(int size) {
		size<<=2;
		int object = Heap.allocate(size+OBJECT_HEADER_SIZE);
		Memory.writeWord(object+OBJECT_SIZE, size);
		Memory.writeWord(object, JVM.T_CLASS);
		Memory.writeWord(object+ARRAY_FIELD, object+ARRAY_START);
		return object;
	}
	
	static int newArrayArray(int size) {
		size<<=2;
		int object = Heap.allocate(size+OBJECT_HEADER_SIZE);
		Memory.writeWord(object+OBJECT_SIZE, size);
		Memory.writeWord(object, JVM.T_CLASS);
		Memory.writeWord(object+ARRAY_FIELD, object+ARRAY_START);
		return object;
	}
}
