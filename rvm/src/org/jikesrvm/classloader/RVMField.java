/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.jikesrvm.classloader;

import static org.jikesrvm.classloader.ClassLoaderConstants.ACC_ENUM;
import static org.jikesrvm.classloader.ClassLoaderConstants.ACC_FINAL;
import static org.jikesrvm.classloader.ClassLoaderConstants.ACC_PRIVATE;
import static org.jikesrvm.classloader.ClassLoaderConstants.ACC_STATIC;
import static org.jikesrvm.classloader.ClassLoaderConstants.ACC_SYNTHETIC;
import static org.jikesrvm.classloader.ClassLoaderConstants.ACC_TRANSIENT;
import static org.jikesrvm.classloader.ClassLoaderConstants.ACC_VOLATILE;
import static org.jikesrvm.classloader.ClassLoaderConstants.APPLICABLE_TO_FIELDS;
import static org.jikesrvm.mm.mminterface.Barriers.NEEDS_OBJECT_GETFIELD_BARRIER;
import static org.jikesrvm.mm.mminterface.Barriers.NEEDS_OBJECT_GETSTATIC_BARRIER;
import static org.jikesrvm.mm.mminterface.Barriers.NEEDS_OBJECT_PUTFIELD_BARRIER;
import static org.jikesrvm.mm.mminterface.Barriers.NEEDS_OBJECT_PUTSTATIC_BARRIER;

import java.io.DataInputStream;
import java.io.IOException;

import org.jikesrvm.VM;
import org.jikesrvm.mm.mminterface.Barriers;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.runtime.Statics;
import org.vmmagic.pragma.Entrypoint;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * A field of a java class.
 */
public final class RVMField extends RVMMember {

  /**
   * constant pool index of field's value (0 --&gt; not a "static final constant")
   */
  private final int constantValueIndex;

  /**
   * The size of the field in bytes
   */
  @Entrypoint(fieldMayBeFinal = true)
  private final byte size;

  /**
   * Does the field hold a reference value?
   */
  @Entrypoint(fieldMayBeFinal = true)
  private final boolean reference;

  /**
   * Has the field been made traced?
   */
  private boolean madeTraced;

  /**
   * Create a field.
   *
   * @param declaringClass the TypeReference object of the class
   * that declared this field
   * @param memRef the canonical memberReference for this member.
   * @param modifiers modifiers associated with this field.
   * @param signature generic type of this field.
   * @param constantValueIndex constant pool index of constant value
   * @param annotations runtime visible annotations
   */
  private RVMField(TypeReference declaringClass, MemberReference memRef, short modifiers, Atom signature,
                   int constantValueIndex, Annotations annotations) {
    super(declaringClass, memRef, modifiers, signature, annotations);
    this.constantValueIndex = constantValueIndex;
    TypeReference typeRef = memRef.asFieldReference().getFieldContentsType();
    this.size = (byte)typeRef.getMemoryBytes();
    this.reference = typeRef.isReferenceType();
    this.madeTraced = false;
    if (VM.runningVM && isUntraced()) {
      VM.sysFail("Untraced field " + toString() + " created at runtime!" +
          " Untraced fields must be resolved at build time to ensure that" +
          " the Untraced annotation is visible to the compilers!");
    }
  }

  /**
   * Reads and creates a field. NB only {@link RVMClass} is allowed to
   * create an instance of a RVMField.
   *
   * @param declaringClass the TypeReference object of the class
   * that declared this field
   * @param constantPool the constant pool of the class loading this field
   * @param memRef the canonical memberReference for this member.
   * @param modifiers modifiers associated with this member.
   * @param input the DataInputStream to read the field's attributed from
   * @param loggingStream the logging stream used to spy on the DataInputStream
   *
   * @return the newly created field
   * @throws IOException when a skip is shorter than expected
   */
  static RVMField readField(TypeReference declaringClass, int[] constantPool, MemberReference memRef,
                            short modifiers, DataInputStream input, LoggingInputStream loggingStream) throws IOException {
    // Read the attributes, processing the "non-boring" ones
    int cvi = 0;
    Atom signature = null;
    RVMAnnotation[] annotations = null;
    byte[] rawAnnotations = null;
    for (int i = 0, n = input.readUnsignedShort(); i < n; ++i) {
      Atom attName = ConstantPool.getUtf(constantPool, input.readUnsignedShort());
      int attLength = input.readInt();
      if (attName == RVMClassLoader.constantValueAttributeName) {
        cvi = input.readUnsignedShort();
      } else if (attName == RVMClassLoader.syntheticAttributeName) {
        modifiers |= ACC_SYNTHETIC;
      } else if (attName == RVMClassLoader.signatureAttributeName) {
        signature = ConstantPool.getUtf(constantPool, input.readUnsignedShort());
      } else if (attName == RVMClassLoader.runtimeVisibleAnnotationsAttributeName) {
        loggingStream.startLogging();
        annotations = AnnotatedElement.readAnnotations(constantPool, input, declaringClass.getClassLoader());
        loggingStream.stopLogging();
        rawAnnotations = loggingStream.getLoggedBytes();
        loggingStream.clearLoggedBytes();
      } else {
        // all other attributes are boring...
        int skippedAmount = input.skipBytes(attLength);
        if (skippedAmount != attLength) {
          throw new IOException("Unexpected short skip");
        }
      }
    }
    Annotations encapsulatedAnnotations = new Annotations(annotations);
    encapsulatedAnnotations.setRawAnnotations(rawAnnotations);
    return new RVMField(declaringClass,
                        memRef,
                        (short) (modifiers & APPLICABLE_TO_FIELDS),
                        signature,
                        cvi,
                        encapsulatedAnnotations);
  }

  public static RVMField createSyntheticFieldForReplacementClass(
      TypeReference declaringClass, short modifiers, Atom fieldName, Atom genericSignature, MemberReference memRef) {
    final int fieldIsNotConstant = 0;
    return new RVMField(declaringClass,
                        memRef,
                        (short) (modifiers & APPLICABLE_TO_FIELDS),
                        genericSignature,
                        fieldIsNotConstant,
                        Annotations.noAnnotations());
  }


  static RVMField createAnnotationField(TypeReference annotationClass, MemberReference memRef) {
    return new RVMField(annotationClass, memRef, (short) (ACC_PRIVATE | ACC_SYNTHETIC), null, 0, null);
  }

  /**
   * @return type of this field's value.
   */
  @Uninterruptible
  public TypeReference getType() {
    return memRef.asFieldReference().getFieldContentsType();
  }

  /**
   * @return the number of stackslots that value of this type takes
   */
  public int getNumberOfStackSlots() {
    return getType().getStackWords();
  }

  /**
   * @return the number of bytes of memory words that a value of this type takes
   */
  public int getSize() {
    return size;
  }

  /**
   * Does the field hold a reference?
   *
   * @return {@code true} if this field needs to be traced by the garbage collector
   */
  public boolean isTraced() {
    return (reference && !isUntraced()) || madeTraced;
  }

  /**
   * Does the field hold a made-traced reference?
   *
   * @return {@code true} if this field was made traced
   */
  @Uninterruptible
  public boolean madeTraced() {
    return madeTraced;
  }


  /**
   * @return {@code true} if this field holds a reference
   */
  public boolean isReferenceType() {
    return reference;
  }

  /**
   * @return {@code true} if this field is shared among all instances of this class
   */
  @Uninterruptible
  public boolean isStatic() {
    return (modifiers & ACC_STATIC) != 0;
  }

  /**
   * @return {@code true} if this field may only be assigned once
   */
  @Uninterruptible
  public boolean isFinal() {
    return (modifiers & ACC_FINAL) != 0;
  }

  /**
   * @return {@code true} if the value of this field may not be cached in a register
   */
  @Uninterruptible
  public boolean isVolatile() {
    return (modifiers & ACC_VOLATILE) != 0;
  }

  /**
   * @return {@code true} if this field's value is not to be written/read by persistent object manager
   */
  @Uninterruptible
  public boolean isTransient() {
    return (modifiers & ACC_TRANSIENT) != 0;
  }

  /**
   * @return {@code true} if this field is not present in source code file
   */
  public boolean isSynthetic() {
    return (modifiers & ACC_SYNTHETIC) != 0;
  }

  /**
   * @return {@code true} if this field is an enum constant
   */
  public boolean isEnumConstant() {
    return (modifiers & ACC_ENUM) != 0;
  }

  /**
   * Is the field RuntimeFinal? That is can the annotation value be used in
   * place a reading the field.
   * @return whether the method is final at runtime
   */
  public boolean isRuntimeFinal() {
   return hasRuntimeFinalAnnotation();
  }

  /**
   * @return {@code true} if this this field is invisible to the memory
   *  management system.
   */
  public boolean isUntraced() {
    return hasUntracedAnnotation();
  }

  /**
   * Make this field a traced field by garbage collection. Affects all
   * subclasses of the class in which this field is defined.
   */
  public void makeTraced() {
    madeTraced = true;
    getDeclaringClass().makeFieldTraced(this);
  }

  /**
   * Gets the value from the runtime final field.
   * @return the value of the field, i.e. if the method is final at runtime or
   *  not
   */
  public boolean getRuntimeFinalValue() {
    org.vmmagic.pragma.RuntimeFinal ann;
    if (VM.runningVM) {
      ann = getAnnotation(org.vmmagic.pragma.RuntimeFinal.class);
    } else {
      try {
      ann = getDeclaringClass().getClassForType().getField(getName().toString())
      .getAnnotation(org.vmmagic.pragma.RuntimeFinal.class);
      } catch (NoSuchFieldException e) {
        throw new Error(e);
      }
    }
    return ann.value();
  }

  /**
   * Get index of constant pool entry containing this
   * "static final constant" field's value.
   * @return constant pool index (0 --&gt; field is not a "static final constant")
   */
  @Uninterruptible
  int getConstantValueIndex() {
    return constantValueIndex;
  }

  //-------------------------------------------------------------------//
  // Low level support for various reflective operations               //
  // Because different clients have different error checking           //
  // requirements, these operations are completely unsafe and we       //
  // assume that the client has done the required error checking.      //
  //-------------------------------------------------------------------//

  /**
   * Read the contents of the field.
   * If the contents of this field is an object, return that object.
   * If the contents of this field is a primitive, get the value and wrap it in an object.
   *
   * @param obj the object whose field is to be read
   * @return the value of this field. For primitiv types, a boxed value is used.
   */
  public Object getObjectUnchecked(Object obj) {
    if (isReferenceType()) {
      return getObjectValueUnchecked(obj);
    } else {
      TypeReference type = getType();
      if (type.isCharType()) return getCharValueUnchecked(obj);
      if (type.isDoubleType()) return getDoubleValueUnchecked(obj);
      if (type.isFloatType()) return getFloatValueUnchecked(obj);
      if (type.isLongType()) return getLongValueUnchecked(obj);
      if (type.isIntType()) return getIntValueUnchecked(obj);
      if (type.isShortType()) return getShortValueUnchecked(obj);
      if (type.isByteType()) return getByteValueUnchecked(obj);
      if (type.isBooleanType()) return getBooleanValueUnchecked(obj);
      return null;
    }
  }

  /**
   * Read one object ref from heap using RVM object model, GC safe.
   * @param obj the object whose field is to be read,
   * or {@code null} if the field is static.
   * @return the reference described by this RVMField from the given object.
   */
  public Object getObjectValueUnchecked(Object obj) {
    if (isStatic()) {
      if (NEEDS_OBJECT_GETSTATIC_BARRIER && !isUntraced()) {
        return Barriers.objectStaticRead(getOffset(), getId());
      } else {
        return Statics.getSlotContentsAsObject(getOffset());
      }
    } else {
      if (NEEDS_OBJECT_GETFIELD_BARRIER && !isUntraced()) {
        return Barriers.objectFieldRead(obj, getOffset(), getId());
      } else {
        return Magic.getObjectAtOffset(obj, getOffset());
      }
    }
  }

  public Word getWordValueUnchecked(Object obj) {
    if (isStatic()) {
      return Statics.getSlotContentsAsAddress(getOffset()).toWord();
    } else {
      return Magic.getWordAtOffset(obj, getOffset());
    }
  }

  public Address getAddressValueUnchecked(Object obj) {
    if (isStatic()) {
      return Statics.getSlotContentsAsAddress(getOffset()).toWord().toAddress();
    } else {
      return Magic.getAddressAtOffset(obj, getOffset());
    }
  }

  public Offset getOffsetValueUnchecked(Object obj) {
    if (isStatic()) {
      return Statics.getSlotContentsAsAddress(getOffset()).toWord().toOffset();
    } else {
      return Magic.getOffsetAtOffset(obj, getOffset());
    }
  }

  public Extent getExtentValueUnchecked(Object obj) {
    if (isStatic()) {
      return Statics.getSlotContentsAsAddress(getOffset()).toWord().toExtent();
    } else {
      return Magic.getExtentAtOffset(obj, getOffset());
    }
  }

  public boolean getBooleanValueUnchecked(Object obj) {
    byte bits;
    if (isStatic()) {
      bits = (byte) Statics.getSlotContentsAsInt(getOffset());
    } else {
      bits = Magic.getUnsignedByteAtOffset(obj, getOffset());
    }
    return (bits != 0);
  }

  public byte getByteValueUnchecked(Object obj) {
    if (isStatic()) {
      return (byte) Statics.getSlotContentsAsInt(getOffset());
    } else {
      return Magic.getByteAtOffset(obj, getOffset());
    }
  }

  public char getCharValueUnchecked(Object obj) {
    if (isStatic()) {
      return (char) Statics.getSlotContentsAsInt(getOffset());
    } else {
      return Magic.getCharAtOffset(obj, getOffset());
    }
  }

  public short getShortValueUnchecked(Object obj) {
    if (isStatic()) {
      return (short) Statics.getSlotContentsAsInt(getOffset());
    } else {
      return Magic.getShortAtOffset(obj, getOffset());
    }
  }

  public int getIntValueUnchecked(Object obj) {
    return get32Bits(obj);
  }

  public long getLongValueUnchecked(Object obj) {
    return get64Bits(obj);
  }

  public float getFloatValueUnchecked(Object obj) {
    return Magic.intBitsAsFloat(get32Bits(obj));
  }

  public double getDoubleValueUnchecked(Object obj) {
    return Magic.longBitsAsDouble(get64Bits(obj));
  }

  private int get32Bits(Object obj) {
    if (isStatic()) {
      return Statics.getSlotContentsAsInt(getOffset());
    } else {
      return Magic.getIntAtOffset(obj, getOffset());
    }
  }

  private long get64Bits(Object obj) {
    if (isStatic()) {
      return Statics.getSlotContentsAsLong(getOffset());
    } else {
      return Magic.getLongAtOffset(obj, getOffset());
    }
  }

  /**
   * assign one object ref from heap using RVM object model, GC safe.
   * @param obj the object whose field is to be modified, or null if the field is static.
   * @param ref the object reference to be assigned.
   */
  public void setObjectValueUnchecked(Object obj, Object ref) {
    if (isStatic()) {
      if (NEEDS_OBJECT_PUTSTATIC_BARRIER && !isUntraced()) {
        Barriers.objectStaticWrite(ref, getOffset(), getId());
      } else {
        Statics.setSlotContents(getOffset(), ref);
      }
    } else {
      if (NEEDS_OBJECT_PUTFIELD_BARRIER && !isUntraced()) {
        Barriers.objectFieldWrite(obj, ref, getOffset(), getId());
      } else {
        Magic.setObjectAtOffset(obj, getOffset(), ref);
      }
    }
  }

  /**
   * assign one object ref from heap using RVM object model, GC safe.
   * @param obj the object whose field is to be modified, or null if the field is static.
   * @param ref the object reference to be assigned.
   */
  public void setWordValueUnchecked(Object obj, Word ref) {
    if (isStatic()) {
      Statics.setSlotContents(getOffset(), ref);
    } else {
      if (Barriers.NEEDS_WORD_PUTFIELD_BARRIER) {
        Barriers.wordFieldWrite(obj, ref, getOffset(), getId());
      } else {
        Magic.setWordAtOffset(obj, getOffset(), ref);
      }
    }
  }

  public void setAddressValueUnchecked(Object obj, Address ref) {
    if (isStatic()) {
      Statics.setSlotContents(getOffset(), ref);
    } else {
      if (Barriers.NEEDS_ADDRESS_PUTFIELD_BARRIER) {
        Barriers.addressFieldWrite(obj, ref, getOffset(), getId());
      } else {
        Magic.setAddressAtOffset(obj, getOffset(), ref);
      }
    }
  }

  public void setExtentValueUnchecked(Object obj, Extent ref) {
    if (isStatic()) {
      Statics.setSlotContents(getOffset(), ref);
    } else {
      if (Barriers.NEEDS_EXTENT_PUTFIELD_BARRIER) {
        Barriers.extentFieldWrite(obj, ref, getOffset(), getId());
      } else {
        Magic.setExtentAtOffset(obj, getOffset(), ref);
      }
    }
  }

  public void setOffsetValueUnchecked(Object obj, Offset ref) {
    if (isStatic()) {
      Statics.setSlotContents(getOffset(), ref);
    } else {
      if (Barriers.NEEDS_OFFSET_PUTFIELD_BARRIER) {
        Barriers.offsetFieldWrite(obj, ref, getOffset(), getId());
      } else {
        Magic.setOffsetAtOffset(obj, getOffset(), ref);
      }
    }
  }

  public void setBooleanValueUnchecked(Object obj, boolean b) {
    if (isStatic()) {
      Statics.setSlotContents(getOffset(), b ? 1 : 0);
    } else {
      if (Barriers.NEEDS_BOOLEAN_PUTFIELD_BARRIER) {
        Barriers.booleanFieldWrite(obj, b, getOffset(), getId());
      } else {
        Magic.setBooleanAtOffset(obj, getOffset(), b);
      }
    }
  }

  public void setByteValueUnchecked(Object obj, byte b) {
    if (isStatic()) {
      Statics.setSlotContents(getOffset(), b);
    } else {
      if (Barriers.NEEDS_BYTE_PUTFIELD_BARRIER) {
        Barriers.byteFieldWrite(obj, b, getOffset(), getId());
      } else {
        Magic.setByteAtOffset(obj, getOffset(), b);
      }
    }
  }

  public void setCharValueUnchecked(Object obj, char c) {
    if (isStatic()) {
      Statics.setSlotContents(getOffset(), c);
    } else {
      if (Barriers.NEEDS_CHAR_PUTFIELD_BARRIER) {
        Barriers.charFieldWrite(obj, c, getOffset(), getId());
      } else {
        Magic.setCharAtOffset(obj, getOffset(), c);
      }
    }
  }

  public void setShortValueUnchecked(Object obj, short i) {
    if (isStatic()) {
      Statics.setSlotContents(getOffset(), i);
    } else {
      if (Barriers.NEEDS_SHORT_PUTFIELD_BARRIER) {
        Barriers.shortFieldWrite(obj, i, getOffset(), getId());
      } else {
        Magic.setShortAtOffset(obj, getOffset(), i);
      }
    }
  }

  public void setIntValueUnchecked(Object obj, int i) {
    if (isStatic()) {
      Statics.setSlotContents(getOffset(), i);
    } else {
      if (Barriers.NEEDS_INT_PUTFIELD_BARRIER) {
        Barriers.intFieldWrite(obj, i, getOffset(), getId());
      } else {
        Magic.setIntAtOffset(obj, getOffset(), i);
      }
    }
  }

  public void setFloatValueUnchecked(Object obj, float f) {
    if (isStatic()) {
      Statics.setSlotContents(getOffset(), f);
    } else {
      if (Barriers.NEEDS_FLOAT_PUTFIELD_BARRIER) {
        Barriers.floatFieldWrite(obj, f, getOffset(), getId());
      } else {
        Magic.setFloatAtOffset(obj, getOffset(), f);
      }
    }
  }

  public void setLongValueUnchecked(Object obj, long l) {
    if (isStatic()) {
      Statics.setSlotContents(getOffset(), l);
    } else {
      if (Barriers.NEEDS_LONG_PUTFIELD_BARRIER) {
        Barriers.longFieldWrite(obj, l, getOffset(), getId());
      } else {
        Magic.setLongAtOffset(obj, getOffset(), l);
      }
    }
  }

  public void setDoubleValueUnchecked(Object obj, double d) {
    if (isStatic()) {
      Statics.setSlotContents(getOffset(), d);
    } else {
      if (Barriers.NEEDS_DOUBLE_PUTFIELD_BARRIER) {
        Barriers.doubleFieldWrite(obj, d, getOffset(), getId());
      } else {
        Magic.setDoubleAtOffset(obj, getOffset(), d);
      }
    }
  }
}
