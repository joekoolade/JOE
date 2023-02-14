/*
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang.reflect;

import org.jikesrvm.VM;
import org.jikesrvm.classlibrary.JavaLangReflectSupport;
import org.jikesrvm.classloader.RVMType;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.ReplaceMember;

/**
 * The {@code Array} class provides static methods to dynamically create and
 * access Java arrays.
 *
 * <p>{@code Array} permits widening conversions to occur during a get or set
 * operation, but throws an {@code IllegalArgumentException} if a narrowing
 * conversion would occur.
 *
 * @author Nakul Saraiya
 */
public final
class Array {

    /**
     * Constructor.  Class Array is not instantiable.
     */
    private Array() {}

    /**
     * Creates a new array with the specified component type and
     * length.
     * Invoking this method is equivalent to creating an array
     * as follows:
     * <blockquote>
     * <pre>
     * int[] x = {length};
     * Array.newInstance(componentType, x);
     * </pre>
     * </blockquote>
     *
     * @param componentType the {@code Class} object representing the
     * component type of the new array
     * @param length the length of the new array
     * @return the new array
     * @exception NullPointerException if the specified
     * {@code componentType} parameter is null
     * @exception IllegalArgumentException if componentType is {@link Void#TYPE}
     * @exception NegativeArraySizeException if the specified {@code length}
     * is negative
     */
    public static Object newInstance(Class<?> componentType, int length)
        throws NegativeArraySizeException {
        return JavaLangReflectSupport.createArray(componentType, length);
    }

    /**
     * Creates a new array
     * with the specified component type and dimensions.
     * If {@code componentType}
     * represents a non-array class or interface, the new array
     * has {@code dimensions.length} dimensions and
     * {@code componentType} as its component type. If
     * {@code componentType} represents an array class, the
     * number of dimensions of the new array is equal to the sum
     * of {@code dimensions.length} and the number of
     * dimensions of {@code componentType}. In this case, the
     * component type of the new array is the component type of
     * {@code componentType}.
     *
     * <p>The number of dimensions of the new array must not
     * exceed the number of array dimensions supported by the
     * implementation (typically 255).
     *
     * @param componentType the {@code Class} object representing the component
     * type of the new array
     * @param dimensions an array of {@code int} representing the dimensions of
     * the new array
     * @return the new array
     * @exception NullPointerException if the specified
     * {@code componentType} argument is null
     * @exception IllegalArgumentException if the specified {@code dimensions}
     * argument is a zero-dimensional array, or if the number of
     * requested dimensions exceeds the limit on the number of array dimensions
     * supported by the implementation (typically 255), or if componentType
     * is {@link Void#TYPE}.
     * @exception NegativeArraySizeException if any of the components in
     * the specified {@code dimensions} argument is negative.
     */
    public static Object newInstance(Class<?> componentType, int... dimensions)
        throws IllegalArgumentException, NegativeArraySizeException {
        return JavaLangReflectSupport.createArray(componentType, dimensions);
    }

    private static void checkThatArgumentIsNonNullArray(Object array) {
        if (array == null) {
          throw new NullPointerException();
        }
        RVMType objectType = Magic.getObjectType(array);
        if (!objectType.isArrayType()) {
          throw new IllegalArgumentException("The argument " + array + " is not an array!");
        }
      }

    /**
     * Returns the length of the specified array object, as an {@code int}.
     *
     * @param array the array
     * @return the length of the array
     * @exception IllegalArgumentException if the object argument is not
     * an array
     */
    public static int getLength(Object array) throws IllegalArgumentException {
        checkThatArgumentIsNonNullArray(array);
        return Magic.getArrayLength(array);
      }

    /**
     * Returns the value of the indexed component in the specified
     * array object.  The value is automatically wrapped in an object
     * if it has a primitive type.
     *
     * @param array the array
     * @param index the index
     * @return the (possibly wrapped) value of the indexed component in
     * the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     */
    public static Object get(Object array, int index)  throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        checkThatArgumentIsNonNullArray(array);
        if (array instanceof Object[]) {
          Object[] objArray = (Object[]) array;
          return objArray[index];
        } else if (array instanceof int[]) {
          int[] intArray = (int[]) array;
          return intArray[index];
        } else if (array instanceof long[]) {
          long[] longArray = (long[]) array;
          return longArray[index];
        } else if (array instanceof float[]) {
          float[] floatArray = (float[]) array;
          return floatArray[index];
        } else if (array instanceof double[]) {
          double[] doubleArray = (double[]) array;
          return doubleArray[index];
        } else if (array instanceof short[]) {
          short[] shortArray = (short[]) array;
          return shortArray[index];
        } else if (array instanceof char[]) {
          char[] charArray = (char[]) array;
          return charArray[index];
        } else if (array instanceof byte[]) {
          byte[] byteArray = (byte[]) array;
          return byteArray[index];
        } else if (array instanceof boolean[]) {
          boolean[] booleanArray = (boolean[]) array;
          return booleanArray[index];
        } else {
          if (VM.VerifyAssertions) {
            VM._assert(VM.NOT_REACHED, "Unknown array type: " + array.getClass());
          }
          VM.sysFail("Unknown array type: " + array.getClass());
        }

        return null;
      }

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code boolean}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static boolean getBoolean(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        checkThatArgumentIsNonNullArray(array);
        if (array instanceof boolean[]) {
          boolean[] booleanArray = (boolean[]) array;
          return booleanArray[index];
        } else {
          throw new IllegalArgumentException("Array " + array + " isn't compatible with boolean");
        }
    }

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code byte}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static byte getByte(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        checkThatArgumentIsNonNullArray(array);
        if (array instanceof byte[]) {
          byte[] byteArray = (byte[]) array;
          return byteArray[index];
        } else {
          throw new IllegalArgumentException("Array " + array + " isn't compatible with byte");
        }
    }

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code char}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static char getChar(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        checkThatArgumentIsNonNullArray(array);
        if (array instanceof char[]) {
          char[] charArray = (char[]) array;
          return charArray[index];
        } else {
          throw new IllegalArgumentException("Array " + array + " isn't compatible with char");
        }
    }

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code short}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
	public static short getShort(Object array, int index)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		checkThatArgumentIsNonNullArray(array);
		if (array instanceof short[]) {
			short[] charArray = (short[]) array;
			return charArray[index];
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with short");
		}
	}

    /**
     * Returns the value of the indexed component in the specified
     * array object, as an {@code int}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
	public static int getInt(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		if (array instanceof int[]) {
			int[] intArray = (int[]) array;
			return intArray[index];
		} else if (array instanceof byte[]) {
			byte[] byteArray = (byte[]) array;
			return byteArray[index];
		} else if (array instanceof char[]) {
			char[] charArray = (char[]) array;
			return charArray[index];
		} else if (array instanceof short[]) {
			short[] charArray = (short[]) array;
			return charArray[index];
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with int");
		}
	}

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code long}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
	public static long getLong(Object array, int index)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		if (array instanceof long[]) {
			long[] intArray = (long[]) array;
			return intArray[index];
		} else if (array instanceof int[]) {
			int[] intArray = (int[]) array;
			return intArray[index];
		} else if (array instanceof byte[]) {
			byte[] byteArray = (byte[]) array;
			return byteArray[index];
		} else if (array instanceof char[]) {
			char[] charArray = (char[]) array;
			return charArray[index];
		} else if (array instanceof short[]) {
			short[] charArray = (short[]) array;
			return charArray[index];
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with long");
		}
	}

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code float}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
	public static float getFloat(Object array, int index)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		if (array instanceof float[]) {
			float[] floatArray = (float[]) array;
			return floatArray[index];
		} else if (array instanceof long[]) {
			long[] intArray = (long[]) array;
			return intArray[index];
		} else if (array instanceof int[]) {
			int[] intArray = (int[]) array;
			return intArray[index];
		} else if (array instanceof byte[]) {
			byte[] byteArray = (byte[]) array;
			return byteArray[index];
		} else if (array instanceof char[]) {
			char[] charArray = (char[]) array;
			return charArray[index];
		} else if (array instanceof short[]) {
			short[] charArray = (short[]) array;
			return charArray[index];
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with float");
		}
	}

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code double}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
	public static double getDouble(Object array, int index)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		if (array instanceof double[]) {
			double[] doubleArray = (double[]) array;
			return doubleArray[index];
		} else if (array instanceof float[]) {
			float[] floatArray = (float[]) array;
			return floatArray[index];
		} else if (array instanceof long[]) {
			long[] intArray = (long[]) array;
			return intArray[index];
		} else if (array instanceof int[]) {
			int[] intArray = (int[]) array;
			return intArray[index];
		} else if (array instanceof byte[]) {
			byte[] byteArray = (byte[]) array;
			return byteArray[index];
		} else if (array instanceof char[]) {
			char[] charArray = (char[]) array;
			return charArray[index];
		} else if (array instanceof short[]) {
			short[] charArray = (short[]) array;
			return charArray[index];
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with float");
		}
	}

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified new value.  The new value is first
     * automatically unwrapped if the array has a primitive component
     * type.
     * @param array the array
     * @param index the index into the array
     * @param value the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the array component type is primitive and
     * an unwrapping conversion fails
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     */
	public static void set(Object array, int index, Object value)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		checkThatArgumentIsNonNullArray(array);
		if (array instanceof Object[]) {
			Object[] objArray = (Object[]) array;
			objArray[index] = value;
		} else if (array instanceof int[]) {
			int[] intArray = (int[]) array;
			try {
				int i = (Integer) value;
				intArray[index] = i;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof long[]) {
			long[] longArray = (long[]) array;
			try {
				long l = (Long) value;
				longArray[index] = l;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof float[]) {
			float[] floatArray = (float[]) array;
			try {
				float f = (Float) value;
				floatArray[index] = f;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof double[]) {
			double[] doubleArray = (double[]) array;
			try {
				double d = (Double) value;
				doubleArray[index] = d;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof short[]) {
			short[] shortArray = (short[]) array;
			try {
				short s = (Short) value;
				shortArray[index] = s;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof char[]) {
			char[] charArray = (char[]) array;
			try {
				char c = (Character) value;
				charArray[index] = c;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof byte[]) {
			byte[] byteArray = (byte[]) array;
			try {
				byte b = (Byte) value;
				byteArray[index] = b;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof boolean[]) {
			boolean[] booleanArray = (boolean[]) array;
			try {
				boolean b = (Boolean) value;
				booleanArray[index] = b;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			if (VM.VerifyAssertions) {
				VM._assert(VM.NOT_REACHED, "Unknown array type: " + array.getClass());
			}
			VM.sysFail("Unknown array type: " + array.getClass());
		}
	}

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code boolean} value.
     * @param array the array
     * @param index the index into the array
     * @param z the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
	public static void setBoolean(Object array, int index, boolean z)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		checkThatArgumentIsNonNullArray(array);
		if (array instanceof boolean[]) {
			boolean[] booleanArray = (boolean[]) array;
			booleanArray[index] = z;
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with boolean");
		}
	}

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code byte} value.
     * @param array the array
     * @param index the index into the array
     * @param b the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
	public static void setByte(Object array, int index, byte value)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		checkThatArgumentIsNonNullArray(array);
		if (array instanceof Object[]) {
			Object[] objArray = (Object[]) array;
			objArray[index] = value;
		} else if (array instanceof int[]) {
			int[] intArray = (int[]) array;
			try {
				int i = value;
				intArray[index] = i;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof long[]) {
			long[] longArray = (long[]) array;
			try {
				long l = value;
				longArray[index] = l;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof float[]) {
			float[] floatArray = (float[]) array;
			try {
				float f = value;
				floatArray[index] = f;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof double[]) {
			double[] doubleArray = (double[]) array;
			try {
				double d = value;
				doubleArray[index] = d;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof short[]) {
			short[] shortArray = (short[]) array;
			try {
				short s = value;
				shortArray[index] = s;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof byte[]) {
			byte[] byteArray = (byte[]) array;
			try {
				byte b = value;
				byteArray[index] = b;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with byte");
		}
	}

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code char} value.
     * @param array the array
     * @param index the index into the array
     * @param c the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
	public static void setChar(Object array, int index, char value)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		checkThatArgumentIsNonNullArray(array);
		if (array instanceof Object[]) {
			Object[] objArray = (Object[]) array;
			objArray[index] = value;
		} else if (array instanceof int[]) {
			int[] intArray = (int[]) array;
			try {
				int i = value;
				intArray[index] = i;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof long[]) {
			long[] longArray = (long[]) array;
			try {
				long l = value;
				longArray[index] = l;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof float[]) {
			float[] floatArray = (float[]) array;
			try {
				float f = value;
				floatArray[index] = f;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof double[]) {
			double[] doubleArray = (double[]) array;
			try {
				double d = value;
				doubleArray[index] = d;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with char");
		}
	}

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code short} value.
     * @param array the array
     * @param index the index into the array
     * @param s the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
	public static void setShort(Object array, int index, short value)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		checkThatArgumentIsNonNullArray(array);
		if (array instanceof Object[]) {
			Object[] objArray = (Object[]) array;
			objArray[index] = value;
		} else if (array instanceof int[]) {
			int[] intArray = (int[]) array;
			try {
				int i = value;
				intArray[index] = i;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof long[]) {
			long[] longArray = (long[]) array;
			try {
				long l = value;
				longArray[index] = l;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof float[]) {
			float[] floatArray = (float[]) array;
			try {
				float f = value;
				floatArray[index] = f;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof double[]) {
			double[] doubleArray = (double[]) array;
			try {
				double d = value;
				doubleArray[index] = d;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof short[]) {
			short[] shortArray = (short[]) array;
			try {
				short s = value;
				shortArray[index] = s;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with short");
		}
	}

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code int} value.
     * @param array the array
     * @param index the index into the array
     * @param i the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
	public static void setInt(Object array, int index, int value)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		checkThatArgumentIsNonNullArray(array);
		if (array instanceof Object[]) {
			Object[] objArray = (Object[]) array;
			objArray[index] = value;
		} else if (array instanceof int[]) {
			int[] intArray = (int[]) array;
			try {
				int i = value;
				intArray[index] = i;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof long[]) {
			long[] longArray = (long[]) array;
			try {
				long l = value;
				longArray[index] = l;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof float[]) {
			float[] floatArray = (float[]) array;
			try {
				float f = value;
				floatArray[index] = f;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof double[]) {
			double[] doubleArray = (double[]) array;
			try {
				double d = value;
				doubleArray[index] = d;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with int");
		}
	}

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code long} value.
     * @param array the array
     * @param index the index into the array
     * @param l the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
	public static void setLong(Object array, int index, long value)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		checkThatArgumentIsNonNullArray(array);
		if (array instanceof Object[]) {
			Object[] objArray = (Object[]) array;
			objArray[index] = value;
		} else if (array instanceof long[]) {
			long[] longArray = (long[]) array;
			try {
				long l = value;
				longArray[index] = l;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof float[]) {
			float[] floatArray = (float[]) array;
			try {
				float f = value;
				floatArray[index] = f;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof double[]) {
			double[] doubleArray = (double[]) array;
			try {
				double d = value;
				doubleArray[index] = d;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with long");
		}
	}

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code float} value.
     * @param array the array
     * @param index the index into the array
     * @param f the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
	public static void setFloat(Object array, int index, float value)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		checkThatArgumentIsNonNullArray(array);
		if (array instanceof Object[]) {
			Object[] objArray = (Object[]) array;
			objArray[index] = value;
		} else if (array instanceof float[]) {
			float[] floatArray = (float[]) array;
			try {
				float f = value;
				floatArray[index] = f;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (array instanceof double[]) {
			double[] doubleArray = (double[]) array;
			try {
				double d = value;
				doubleArray[index] = d;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with float");
		}
	}

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code double} value.
     * @param array the array
     * @param index the index into the array
     * @param d the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
	public static void setDouble(Object array, int index, double value)
			throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		checkThatArgumentIsNonNullArray(array);
		if (array instanceof Object[]) {
			Object[] objArray = (Object[]) array;
			objArray[index] = value;
		} else if (array instanceof double[]) {
			double[] doubleArray = (double[]) array;
			try {
				double d = value;
				doubleArray[index] = d;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			throw new IllegalArgumentException("Array " + array + " isn't compatible with double");
		}
	}

}
