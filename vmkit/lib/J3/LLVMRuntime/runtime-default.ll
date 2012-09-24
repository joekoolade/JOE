;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;; Type definitions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; A virtual table is an array of function pointers.
%VT = type [0 x i32 (...)*]

;;; The root of all Java Objects: a VT and a lock.
%JavaObject = type { %VT*, i8* }

;;; Types for Java arrays. A size of 0 means an undefined size.
%JavaArray = type { %JavaObject, i8* }
%ArrayDouble = type { %JavaObject, i8*, [0 x double] }
%ArrayFloat = type { %JavaObject, i8*, [0 x float] }
%ArrayLong = type { %JavaObject, i8*, [0 x i64] }
%ArrayObject = type { %JavaObject, i8*, [0 x %JavaObject*] }
%ArraySInt16 = type { %JavaObject, i8*, [0 x i16] }
%ArraySInt32 = type { %JavaObject, i8*, [0 x i32] }
%ArraySInt8 = type { %JavaObject, i8*, [0 x i8] }
%ArrayUInt16 = type { %JavaObject, i8*, [0 x i16] }
%ArrayUInt32 = type { %JavaObject, i8*, [0 x i32] }
%ArrayUInt8 = type { %JavaObject, i8*, [0 x i8] }

;;; The task class mirror.
;;; Field 1: The class state
;;; Field 2: The initialization state
;;; Field 3: The static instance
%TaskClassMirror = type { i8, i1, i8* }

%CircularBase = type { %VT*, %CircularBase*, %CircularBase* }

;;; Field 0:  the parent (circular base)
;;; Field 1:  size_t IsolateID
;;; Field 2:  void*  MyVM
;;; Field 3:  void*  baseSP
;;; Field 4:  bool   doYield
;;; Field 5:  bool   inRV
;;; Field 6:  bool   joinedRV
;;; Field 7:  void*  lastSP
;;; Field 8:  void*  internalThreadID
;;; field 9:  void*  routine
;;; field 10: void*  lastKnownFrame
;;; field 11: void*  lastExceptionBuffer
%Thread = type { %CircularBase, i8*, i8*, i8*, i1, i1, i1, i8*, i8*, i8*, i8*, i8* }

%JavaThread = type { %MutatorThread, i8*, %JavaObject* }

%JavaConstantPool = type { %JavaClass*, i32, i8*, i32*, i8** }

%Attribut = type { %UTF8*, i32, i32 }

%UTF8 = type { i8*, [0 x i16] }


%JavaField = type { i8*, i16, %UTF8*, %UTF8*, %Attribut*, i16, %JavaClass*, i32,
                    i16 }

%JavaMethod = type { i8*, i16, %Attribut*, i16, %JavaClass*,
                     %UTF8*, %UTF8*, i8, i8*, i32 }

%JavaClassPrimitive = type { %JavaCommonClass, i32 }
%JavaClassArray = type { %JavaCommonClass, %JavaCommonClass* }

%J3DenseMap = type { i32, i8*, i32, i32, i1 }

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;; Make sure all named types are emitted ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

declare void @listAllTypes(%JavaObject,
                           %JavaArray,
                           %JavaCommonClass,
                           %JavaClassPrimitive,
                           %JavaClassArray,
                           %JavaClass,
                           %ClassBytes,
                           %JavaConstantPool,
                           %ArrayUInt8,
                           %ArraySInt8,
                           %ArrayUInt16,
                           %ArraySInt16,
                           %ArrayUInt32,
                           %ArraySInt32,
                           %ArrayLong,
                           %ArrayDouble,
                           %ArrayFloat,
                           %ArrayObject,
                           %JavaField,
                           %JavaMethod,
                           %UTF8,
                           %Attribut,
                           %JavaThread,
                           %MutatorThread,
                           %J3DenseMap);

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;; Constant calls for J3 runtime internal objects field accesses ;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; j3RuntimeInitialiseClass - Initialises the class.
declare %JavaClass* @j3RuntimeInitialiseClass(%JavaClass*)

;;; arrayLength - Get the length of an array.
declare i32 @arrayLength(%JavaObject*) readnone 

;;; getVT - Get the VT of the object.
declare %VT* @getVT(%JavaObject*) readnone 

;;; getIMT - Get the IMT of the VT.
declare %VT* @getIMT(%VT*) readnone 

;;; getClass - Get the class of an object.
declare %JavaCommonClass* @getClass(%JavaObject*) readnone 

;;; getLock - Get the lock of an object.
declare i8* @getLock(%JavaObject*)

;;; getVTFromCommonClass - Get the VT of a class from its runtime
;;; representation.
declare %VT* @getVTFromCommonClass(%JavaCommonClass*) readnone 

;;; getVTFromClass - Get the VT of a class from its runtime representation.
declare %VT* @getVTFromClass(%JavaClass*) readnone 

;;; getVTFromClassArray - Get the VT of an array class from its runtime
;;; representation.
declare %VT* @getVTFromClassArray(%JavaClassArray*) readnone 

;;; getObjectSizeFromClass - Get the size of a class from its runtime
;;; representation.
declare i32 @getObjectSizeFromClass(%JavaClass*) readnone 

;;; getBaseClassVTFromVT - Get the VT of the base class of an array, or the
;;; VT of the array class of a regular class.
declare %VT* @getBaseClassVTFromVT(%VT*) readnone

;;; getDisplay - Get the display array of this VT.
declare %VT** @getDisplay(%VT*) readnone 

;;; getVTInDisplay - Get the super class at the given offset.
declare %VT* @getVTInDisplay(%VT**, i32) readnone 

;;; getDepth - Get the depth of the VT.
declare i32 @getDepth(%VT*) readnone 

;;; getStaticInstance - Get the static instance of this class.
declare i8* @getStaticInstance(%JavaClass*) readnone 


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;; Generic Runtime methods ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; j3InterfaceLookup - Used for interface calls.
declare i8* @j3InterfaceLookup(%JavaClass*, i32, ...)

;;; j3MultiCallNew - Allocate multi-dimensional arrays. This will go to
;;; allocation specific methods.
declare %JavaObject* @j3MultiCallNew(%JavaCommonClass*, i32, ...)

;;; initialisationCheck - Checks if the class has been initialized and 
;;; initializes if not. This is used for initialization barriers in an isolate
;;; environment, and in some specific scenario in a single environment.
declare %JavaClass* @initialisationCheck(%JavaClass*) readnone 

;;; forceInitialisationCheck - Force to check initialization. The difference
;;; between this function and the initialisationCheck function is that the
;;; latter is readnone and can thus be removed. This function is removed
;;; by J3 after the GVN pass, therefore it does not have an actual
;;; implementation.
declare void @forceInitialisationCheck(%JavaClass*)

;;; forceLoadedCheck - Force to check if the class was loaded. Since we do
;;; not want to run Java code in a callback, we have to make sure the class
;;; of the method that we want to compile is loaded. This is used for
;;; the invokespecial bytecode.
declare void @forceLoadedCheck(%JavaCommonClass*)

;;; getConstantPoolAt - Get the value in the constant pool of this class.
;;; This function is removed by J3's LLVM pass, therefore it does
;;; not have an actual implementation.
declare i8* @getConstantPoolAt(i8* (%JavaClass*, i32, ...)*, i8**,
                               %JavaClass*, i32, ...)

;;; j3VirtualTableLookup - Look up the offset in a virtual table of a
;;; specific function.
declare i32 @j3VirtualTableLookup(%JavaClass*, i32, i32*, %JavaObject*)

;;; j3ClassLookup - Look up a specific class. The function takes a class and
;;; an index to lookup in the constant pool and returns and stores it in the
;;; constant pool cache.
declare i8* @j3ClassLookup(%JavaClass*, i32, ...)

;;; j3VirtualFieldLookup - Look up a specific virtual field.
declare i8* @j3VirtualFieldLookup(%JavaClass*, i32, ...)

;;; j3StaticFieldLookup - Look up a specific static field.
declare i8* @j3StaticFieldLookup(%JavaClass*, i32, ...)

;;; j3StringLookup - Get a pointer on a string.
declare i8* @j3StringLookup(%JavaClass*, i32, ...) readnone

;;; j3JavaObjectAquire - This function is called when starting a synchronized
;;; block or method.
declare void @j3JavaObjectAquire(%JavaObject*)

;;; j3JavaObjectRelease - This function is called when leaving a synchronized
;;; block or method.
declare void @j3JavaObjectRelease(%JavaObject*)

;;; isAssignableFrom - Returns if a type is a subtype of another type.
declare i1 @isAssignableFrom(%VT*, %VT*) readnone

;;; isSecondaryClass - Returns if a type is a secondary super type of
;;; another type.
declare i1 @isSecondaryClass(%VT*, %VT*) readnone

;;; getClassDelegatee - Returns the java/lang/Class representation of the
;;; class. This method is lowered to the GEP to the class delegatee in
;;; the common class.
declare %JavaObject* @getClassDelegatee(%JavaCommonClass*)

;;; j3RuntimeDelegatee - Returns the java/lang/Class representation of the
;;; class. This method is called if the class delegatee has not been created
;;; yet.
declare %JavaObject* @j3RuntimeDelegatee(%JavaCommonClass*)

;;; j3GetArrayClass - Get the array user class of the user class.
declare %VT* @j3GetArrayClass(%JavaClass*, i32, %VT**) readnone

declare i8 @getFinalInt8Field(i8*) readnone
declare i16 @getFinalInt16Field(i16*) readnone
declare i32 @getFinalInt32Field(i32*) readnone
declare i64 @getFinalLongField(i64*) readnone
declare double @getFinalDoubleField(double*) readnone
declare float @getFinalFloatField(float*) readnone

declare i8* @j3ResolveVirtualStub(%JavaObject*)
declare i8* @j3ResolveSpecialStub()
declare i8* @j3ResolveStaticStub()
declare i8* @j3ResolveInterface(%JavaObject*, %JavaMethod*, i32)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Exception methods ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

declare %JavaObject* @j3NullPointerException()
declare %JavaObject* @j3ClassCastException(%JavaObject*, %JavaCommonClass*)
declare %JavaObject* @j3IndexOutOfBoundsException(%JavaObject*, i32)
declare %JavaObject* @j3NegativeArraySizeException(i32)
declare %JavaObject* @j3OutOfMemoryError(i32)
declare %JavaObject* @j3StackOverflowError()
declare %JavaObject* @j3ArrayStoreException(%VT*, %VT*)
declare %JavaObject* @j3ArithmeticException()
declare void @j3ThrowException(%JavaObject*)
declare void @j3ThrowExceptionFromJIT()

declare void @j3EndJNI(i32**)
declare void @j3StartJNI(i32*, i32**, i8*)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Debugging methods ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

declare void @j3PrintExecution(i32, i32, %JavaMethod*)
declare void @j3PrintMethodStart(%JavaMethod*)
declare void @j3PrintMethodEnd(%JavaMethod*)
