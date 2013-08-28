VPATH=build
GCJ=$(HOME)/local/bin/gcj
GCJFLAGS=-c $(CLASSPATH) -Wno-deprecated
CFLAGS=$(INCLUDES) -fno-rtti -fnon-call-exceptions -fdollars-in-identifiers -ffloat-store
CPPFLAGS=
CLASSPATH=--bootclasspath=build --classpath=classpath:.:build:kernel:platforms/x86:processors/x86
CLASSBUILD=build
BUILD=build
INCLUDES=-isystem . -Iclasspath -Ibuild -I../gcc/gcc

%.class : %.java
	$(GCJ) -C $(CLASSPATH) -Wno-deprecated -d build $<

%.o : %.java
	$(GCJ) $(GCJFLAGS) -o $(BUILD)/$(<:.java=.o) $<

.SUFFIXES:
.SUFFIXES: .class .o .java .cc .h

TESTS=baremetal/tests/ExceptionTests.class \
	baremetal/tests/HashtableTests.class

KERNEL=kernel/baremetal/kernel/Debug.class \
	kernel/baremetal/kernel/Heap.class \
	kernel/baremetal/kernel/JVM.class \
	kernel/baremetal/kernel/Memory.class \
	kernel/baremetal/kernel/Monitor.class \
	kernel/baremetal/kernel/Utils.class \
	kernel/baremetal/kernel/InterruptManager.class \
	kernel/baremetal/kernel/IrqHandler.class \
	kernel/baremetal/kernel/Scheduler.class \
	kernel/baremetal/kernel/ObjectFactory.class

VM=kernel/baremetal/vm/VMClassLoader.class \
	kernel/baremetal/vm/VMInt.class \
	kernel/baremetal/vm/VMChar.class \
	kernel/baremetal/vm/VMBoolean.class \
	kernel/baremetal/vm/VMLong.class \
	kernel/baremetal/vm/VMShort.class \
	kernel/baremetal/vm/VMByte.class \
	kernel/baremetal/vm/VMFloat.class \
	kernel/baremetal/vm/VMDouble.class \
	kernel/baremetal/vm/VMIntArray.class \
	kernel/baremetal/vm/VMCharArray.class \
	kernel/baremetal/vm/VMBooleanArray.class \
	kernel/baremetal/vm/VMLongArray.class \
	kernel/baremetal/vm/VMShortArray.class \
	kernel/baremetal/vm/VMByteArray.class \
	kernel/baremetal/vm/VMFloatArray.class \
	kernel/baremetal/vm/VMDoubleArray.class \
	kernel/baremetal/vm/VMVoid.class \
	kernel/baremetal/vm/VMObject.class \
	kernel/baremetal/vm/VMSecurityManager.class \
	kernel/baremetal/vm/Runtime.class \
	kernel/baremetal/vm/Thread.class \
	kernel/baremetal/vm/System.class \
	kernel/baremetal/vm/VMThrowable.class

PLATFORM=platforms/x86/baremetal/platform/BIOS.class \
	platforms/x86/baremetal/platform/Boot.class \
	platforms/x86/baremetal/platform/Console.class \
	platforms/x86/baremetal/platform/ConsoleOutputStream.class \
	platforms/x86/baremetal/platform/ConsoleErrStream.class \
	platforms/x86/baremetal/platform/SystemTimer.class \
	platforms/x86/baremetal/platform/Platform.class \
	platforms/x86/baremetal/platform/Multiboot.class

PROCESSORS = processors/x86/baremetal/processor/Pentium.class \
	processors/x86/baremetal/processor/Idt.class

DEVICES = baremetal/devices/MC146818.class \
	baremetal/devices/I8259A.class \
	baremetal/devices/I82c54.class

BAREMETAL_RUNTIME = baremetal/runtime/Class.class \
	baremetal/runtime/Utf8.class \
	baremetal/runtime/Thrower.class \
	baremetal/runtime/CommonInfo.class \
	baremetal/runtime/FrameDesc.class \
	baremetal/runtime/Utils.class \
	baremetal/runtime/ExceptionTable.class \
	baremetal/runtime/String.class \
	baremetal/runtime/Method.class \
	baremetal/runtime/VTable.class \
	baremetal/runtime/Idt.class \
	baremetal/runtime/Array.class \
	baremetal/runtime/Iface.class \
	baremetal/runtime/CatchClass.class \
	baremetal/runtime/ConstantPool.class \
	baremetal/runtime/Mutex.class \
	baremetal/runtime/Monitor.class \
	baremetal/runtime/ArrayInterface.class

PROCESSORS_DIR=processors/x86/baremetal/processor
OBJS= 	gcj.o exception.o Memory.o $(PROCESSORS_DIR)/Pentium.o

JAVA_BEANS=java/beans/AppletInitializer.class \
	java/beans/BeanDescriptor.class \
	java/beans/BeanInfo.class \
	java/beans/Beans.class \
	java/beans/Customizer.class \
	java/beans/DesignMode.class \
	java/beans/EventSetDescriptor.class \
	java/beans/ExceptionListener.class \
	java/beans/FeatureDescriptor.class \
	java/beans/IndexedPropertyDescriptor.class \
	java/beans/IntrospectionException.class \
	java/beans/MethodDescriptor.class \
	java/beans/ParameterDescriptor.class \
	java/beans/PropertyChangeEvent.class \
	java/beans/PropertyChangeListener.class \
	java/beans/PropertyChangeListenerProxy.class \
	java/beans/PropertyChangeSupport.class \
	java/beans/PropertyDescriptor.class \
	java/beans/PropertyEditor.class \
	java/beans/PropertyEditorSupport.class \
	java/beans/PropertyVetoException.class \
	java/beans/VetoableChangeListener.class \
	java/beans/VetoableChangeListenerProxy.class \
	java/beans/VetoableChangeSupport.class \
	java/beans/Visibility.class
#	java/beans/PropertyEditorManager.class \
#	java/beans/Introspector.class \
#	java/beans/SimpleBeanInfo.class \

JAVA_BEANS_BEANCONTEXT=java/beans/beancontext/BeanContext.class \
	java/beans/beancontext/BeanContextChild.class \
	java/beans/beancontext/BeanContextChildComponentProxy.class \
	java/beans/beancontext/BeanContextChildSupport.class \
	java/beans/beancontext/BeanContextContainerProxy.class \
	java/beans/beancontext/BeanContextEvent.class \
	java/beans/beancontext/BeanContextMembershipEvent.class \
	java/beans/beancontext/BeanContextMembershipListener.class \
	java/beans/beancontext/BeanContextProxy.class \
	java/beans/beancontext/BeanContextServiceAvailableEvent.class \
	java/beans/beancontext/BeanContextServiceProvider.class \
	java/beans/beancontext/BeanContextServiceProviderBeanInfo.class \
	java/beans/beancontext/BeanContextServiceRevokedEvent.class \
	java/beans/beancontext/BeanContextServiceRevokedListener.class \
	java/beans/beancontext/BeanContextServices.class \
	java/beans/beancontext/BeanContextServicesListener.class \
	java/beans/beancontext/BeanContextServicesSupport.class \
	java/beans/beancontext/BeanContextSupport.class 

JAVA_LANG=java/lang/AbstractMethodError.class \
	java/lang/ArithmeticException.class \
	java/lang/ArrayIndexOutOfBoundsException.class \
	java/lang/ArrayStoreException.class \
	java/lang/AssertionError.class \
	java/lang/Boolean.class \
	java/lang/Byte.class \
	java/lang/CharSequence.class \
	java/lang/Character.class \
	java/lang/Class.class \
	java/lang/ClassCastException.class \
	java/lang/ClassCircularityError.class \
	java/lang/ClassFormatError.class \
	java/lang/ClassLoader.class \
	java/lang/ClassNotFoundException.class \
	java/lang/CloneNotSupportedException.class \
	java/lang/Cloneable.class \
	java/lang/Comparable.class \
	java/lang/Compiler.class \
	java/lang/Double.class \
	java/lang/Error.class \
	java/lang/Exception.class \
	java/lang/ExceptionInInitializerError.class \
	java/lang/Float.class \
	java/lang/IllegalAccessError.class \
	java/lang/IllegalAccessException.class \
	java/lang/IllegalArgumentException.class \
	java/lang/IllegalMonitorStateException.class \
	java/lang/IllegalStateException.class \
	java/lang/IllegalThreadStateException.class \
	java/lang/IncompatibleClassChangeError.class \
	java/lang/IndexOutOfBoundsException.class \
	java/lang/InheritableThreadLocal.class \
	java/lang/InstantiationError.class \
	java/lang/InstantiationException.class \
	java/lang/Integer.class \
	java/lang/InternalError.class \
	java/lang/InterruptedException.class \
	java/lang/LinkageError.class \
	java/lang/Long.class \
	java/lang/Math.class \
	java/lang/NegativeArraySizeException.class \
	java/lang/NoClassDefFoundError.class \
	java/lang/NoSuchFieldError.class \
	java/lang/NoSuchFieldException.class \
	java/lang/NoSuchMethodError.class \
	java/lang/NoSuchMethodException.class \
	java/lang/NullPointerException.class \
	java/lang/Number.class \
	java/lang/NumberFormatException.class \
	java/lang/Object.class \
	java/lang/OutOfMemoryError.class \
	java/lang/Package.class \
	java/lang/Process.class \
	java/lang/Runnable.class \
	java/lang/Runtime.class \
	java/lang/RuntimeException.class \
	java/lang/RuntimePermission.class \
	java/lang/SecurityException.class \
	java/lang/SecurityManager.class \
	java/lang/Short.class \
	java/lang/StackOverflowError.class \
	java/lang/StackTraceElement.class \
	java/lang/StrictMath.class \
	java/lang/String.class \
	java/lang/StringBuffer.class \
	java/lang/StringIndexOutOfBoundsException.class \
	java/lang/System.class \
	java/lang/Thread.class \
	java/lang/ThreadDeath.class \
	java/lang/ThreadGroup.class \
	java/lang/ThreadLocal.class \
	java/lang/Throwable.class \
	java/lang/UnknownError.class \
	java/lang/UnsatisfiedLinkError.class \
	java/lang/UnsupportedClassVersionError.class \
	java/lang/UnsupportedOperationException.class \
	java/lang/VMClassLoader.class \
	java/lang/VMSecurityManager.class \
	java/lang/VMThrowable.class \
	java/lang/VerifyError.class \
	java/lang/VirtualMachineError.class \
	java/lang/Void.class

JAVA_LANG_REF= \
	java/lang/ref/PhantomReference.class \
	java/lang/ref/Reference.class \
	java/lang/ref/ReferenceQueue.class \
	java/lang/ref/SoftReference.class \
	java/lang/ref/WeakReference.class

JAVA_LANG_REFLECT=java/lang/reflect/AccessibleObject.class \
	java/lang/reflect/Array.class \
	java/lang/reflect/Constructor.class \
	java/lang/reflect/Field.class \
	java/lang/reflect/InvocationHandler.class \
	java/lang/reflect/InvocationTargetException.class \
	java/lang/reflect/Member.class \
	java/lang/reflect/Method.class \
	java/lang/reflect/Modifier.class \
	java/lang/reflect/Proxy.class \
	java/lang/reflect/ReflectPermission.class \
	java/lang/reflect/UndeclaredThrowableException.class 


JAVA_NET=java/net/Authenticator.class \
	java/net/BindException.class \
	java/net/ConnectException.class \
	java/net/ContentHandler.class \
	java/net/ContentHandlerFactory.class \
	java/net/DatagramPacket.class \
	java/net/DatagramSocket.class \
	java/net/DatagramSocketImpl.class \
	java/net/DatagramSocketImplFactory.class \
	java/net/FileNameMap.class \
	java/net/HttpURLConnection.class \
	java/net/Inet4Address.class \
	java/net/Inet6Address.class \
	java/net/InetAddress.class \
	java/net/InetSocketAddress.class \
	java/net/JarURLConnection.class \
	java/net/MalformedURLException.class \
	java/net/MulticastSocket.class \
	java/net/NetPermission.class \
	java/net/NetworkInterface.class \
	java/net/NoRouteToHostException.class \
	java/net/PasswordAuthentication.class \
	java/net/PortUnreachableException.class \
	java/net/ProtocolException.class \
	java/net/ServerSocket.class \
	java/net/Socket.class \
	java/net/SocketAddress.class \
	java/net/SocketException.class \
	java/net/SocketImpl.class \
	java/net/SocketImplFactory.class \
	java/net/SocketOptions.class \
	java/net/SocketPermission.class \
	java/net/SocketTimeoutException.class \
	java/net/URI.class \
	java/net/URISyntaxException.class \
	java/net/URL.class \
	java/net/URLClassLoader.class \
	java/net/URLConnection.class \
	java/net/URLDecoder.class \
	java/net/URLEncoder.class \
	java/net/URLStreamHandler.class \
	java/net/URLStreamHandlerFactory.class \
	java/net/UnknownHostException.class \
	java/net/UnknownServiceException.class 

JAVA_UTIL=java/util/AbstractCollection.class \
	java/util/AbstractList.class \
	java/util/AbstractMap.class \
	java/util/AbstractSequentialList.class \
	java/util/AbstractSet.class \
	java/util/ArrayList.class \
	java/util/Arrays.class \
	java/util/BitSet.class \
	java/util/Calendar.class \
	java/util/Collection.class \
	java/util/Collections.class \
	java/util/Comparator.class \
	java/util/ConcurrentModificationException.class \
	java/util/Currency.class \
	java/util/Date.class \
	java/util/Dictionary.class \
	java/util/EmptyStackException.class \
	java/util/Enumeration.class \
	java/util/EventListener.class \
	java/util/EventListenerProxy.class \
	java/util/EventObject.class \
	java/util/GregorianCalendar.class \
	java/util/HashMap.class \
	java/util/HashSet.class \
	java/util/Hashtable.class \
	java/util/IdentityHashMap.class \
	java/util/Iterator.class \
	java/util/LinkedHashMap.class \
	java/util/LinkedHashSet.class \
	java/util/LinkedList.class \
	java/util/List.class \
	java/util/ListIterator.class \
	java/util/ListResourceBundle.class \
	java/util/Locale.class \
	java/util/Map.class \
	java/util/MissingResourceException.class \
	java/util/NoSuchElementException.class \
	java/util/Observable.class \
	java/util/Observer.class \
	java/util/Properties.class \
	java/util/PropertyPermission.class \
	java/util/PropertyPermissionCollection.class \
	java/util/PropertyResourceBundle.class \
	java/util/Random.class \
	java/util/RandomAccess.class \
	java/util/ResourceBundle.class \
	java/util/Set.class \
	java/util/SimpleTimeZone.class \
	java/util/SortedMap.class \
	java/util/SortedSet.class \
	java/util/Stack.class \
	java/util/StringTokenizer.class \
	java/util/TimeZone.class \
	java/util/Timer.class \
	java/util/TimerTask.class \
	java/util/TooManyListenersException.class \
	java/util/TreeMap.class \
	java/util/TreeSet.class \
	java/util/Vector.class \
	java/util/WeakHashMap.class 

JAVA_IO=java/io/BufferedInputStream.class \
	java/io/BufferedOutputStream.class \
	java/io/BufferedReader.class \
	java/io/BufferedWriter.class \
	java/io/ByteArrayInputStream.class \
	java/io/ByteArrayOutputStream.class \
	java/io/CharArrayReader.class \
	java/io/CharArrayWriter.class \
	java/io/CharConversionException.class \
	java/io/DataInput.class \
	java/io/DataInputStream.class \
	java/io/DataOutput.class \
	java/io/DataOutputStream.class \
	java/io/EOFException.class \
	java/io/Externalizable.class \
	java/io/File.class \
	java/io/FileDescriptor.class \
	java/io/FileFilter.class \
	java/io/FileInputStream.class \
	java/io/FileNotFoundException.class \
	java/io/FileOutputStream.class \
	java/io/FilePermission.class \
	java/io/FileReader.class \
	java/io/FileWriter.class \
	java/io/FilenameFilter.class \
	java/io/FilterInputStream.class \
	java/io/FilterOutputStream.class \
	java/io/FilterReader.class \
	java/io/FilterWriter.class \
	java/io/IOException.class \
	java/io/InputStream.class \
	java/io/InputStreamReader.class \
	java/io/InterruptedIOException.class \
	java/io/InvalidClassException.class \
	java/io/InvalidObjectException.class \
	java/io/LineNumberInputStream.class \
	java/io/LineNumberReader.class \
	java/io/NotActiveException.class \
	java/io/NotSerializableException.class \
	java/io/ObjectInput.class \
	java/io/ObjectInputStream.class \
	java/io/ObjectInputValidation.class \
	java/io/ObjectOutput.class \
	java/io/ObjectOutputStream.class \
	java/io/ObjectStreamClass.class \
	java/io/ObjectStreamConstants.class \
	java/io/ObjectStreamException.class \
	java/io/ObjectStreamField.class \
	java/io/OptionalDataException.class \
	java/io/OutputStream.class \
	java/io/OutputStreamWriter.class \
	java/io/PipedInputStream.class \
	java/io/PipedOutputStream.class \
	java/io/PipedReader.class \
	java/io/PipedWriter.class \
	java/io/PrintStream.class \
	java/io/PrintWriter.class \
	java/io/PushbackInputStream.class \
	java/io/PushbackReader.class \
	java/io/RandomAccessFile.class \
	java/io/Reader.class \
	java/io/SequenceInputStream.class \
	java/io/Serializable.class \
	java/io/SerializablePermission.class \
	java/io/StreamCorruptedException.class \
	java/io/StreamTokenizer.class \
	java/io/StringBufferInputStream.class \
	java/io/StringReader.class \
	java/io/StringWriter.class \
	java/io/SyncFailedException.class \
	java/io/UTFDataFormatException.class \
	java/io/UnsupportedEncodingException.class \
	java/io/VMObjectStreamClass.class \
	java/io/WriteAbortedException.class \
	java/io/Writer.class

JAVA_MATH=java/math/BigDecimal.class \
	java/math/BigInteger.class 

JAVA_NIO=java/nio/Buffer.class \
	java/nio/BufferOverflowException.class \
	java/nio/BufferUnderflowException.class \
	java/nio/ByteBuffer.class \
	java/nio/ByteBufferHelper.class \
	java/nio/ByteBufferImpl.class \
	java/nio/ByteOrder.class \
	java/nio/CharBufferImpl.class \
	java/nio/CharBuffer.class \
	java/nio/CharViewBufferImpl.class \
	java/nio/DirectByteBufferImpl.class \
	java/nio/DoubleBufferImpl.class \
	java/nio/DoubleBuffer.class \
	java/nio/DoubleViewBufferImpl.class \
	java/nio/FloatBufferImpl.class \
	java/nio/FloatBuffer.class \
	java/nio/FloatViewBufferImpl.class \
	java/nio/IntBufferImpl.class \
	java/nio/IntBuffer.class \
	java/nio/IntViewBufferImpl.class \
	java/nio/InvalidMarkException.class \
	java/nio/LongBufferImpl.class \
	java/nio/LongBuffer.class \
	java/nio/LongViewBufferImpl.class \
	java/nio/MappedByteBufferImpl.class \
	java/nio/MappedByteBuffer.class \
	java/nio/ReadOnlyBufferException.class \
	java/nio/ShortBufferImpl.class \
	java/nio/ShortBuffer.class \
	java/nio/ShortViewBufferImpl.class

JAVA_NIO_CHANNELS = java/nio/channels/AlreadyConnectedException.class \
	java/nio/channels/AsynchronousCloseException.class \
	java/nio/channels/ByteChannel.class \
	java/nio/channels/CancelledKeyException.class \
	java/nio/channels/Channel.class \
	java/nio/channels/Channels.class \
	java/nio/channels/ClosedByInterruptException.class \
	java/nio/channels/ClosedChannelException.class \
	java/nio/channels/ClosedSelectorException.class \
	java/nio/channels/ConnectionPendingException.class \
	java/nio/channels/DatagramChannel.class \
	java/nio/channels/FileChannelImpl.class \
	java/nio/channels/FileChannel.class \
	java/nio/channels/FileLockInterruptionException.class \
	java/nio/channels/FileLock.class \
	java/nio/channels/GatheringByteChannel.class \
	java/nio/channels/IllegalBlockingModeException.class \
	java/nio/channels/IllegalSelectorException.class \
	java/nio/channels/InterruptibleChannel.class \
	java/nio/channels/NoConnectionPendingException.class \
	java/nio/channels/NonReadableChannelException.class \
	java/nio/channels/NonWritableChannelException.class \
	java/nio/channels/NotYetBoundException.class \
	java/nio/channels/NotYetConnectedException.class \
	java/nio/channels/OverlappingFileLockException.class \
	java/nio/channels/Pipe.class \
	java/nio/channels/ReadableByteChannel.class \
	java/nio/channels/ScatteringByteChannel.class \
	java/nio/channels/SelectableChannel.class \
	java/nio/channels/SelectionKey.class \
	java/nio/channels/Selector.class \
	java/nio/channels/ServerSocketChannel.class \
	java/nio/channels/SocketChannel.class \
	java/nio/channels/UnresolvedAddressException.class \
	java/nio/channels/UnsupportedAddressTypeException.class \
	java/nio/channels/WritableByteChannel.class

JAVA_NIO_CHANNELS_SPI= java/nio/channels/spi/AbstractInterruptibleChannel.class \
	java/nio/channels/spi/AbstractSelectableChannel.class \
	java/nio/channels/spi/AbstractSelectionKey.class \
	java/nio/channels/spi/AbstractSelector.class \
	java/nio/channels/spi/SelectorProvider.class

JAVA_NIO_CHARSET= java/nio/charset/CharacterCodingException.class \
	java/nio/charset/Charset.class \
	java/nio/charset/CharsetDecoder.class \
	java/nio/charset/CharsetEncoder.class \
	java/nio/charset/CoderMalfunctionError.class \
	java/nio/charset/CoderResult.class \
	java/nio/charset/CodingErrorAction.class \
	java/nio/charset/IllegalCharsetNameException.class \
	java/nio/charset/MalformedInputException.class \
	java/nio/charset/UnmappableCharacterException.class \
	java/nio/charset/UnsupportedCharsetException.class

JAVA_NIO_CHARSET_SPI= 	java/nio/charset/spi/CharsetProvider.class

JAVA_SECURITY=java/security/AccessControlContext.class \
	java/security/AccessControlException.class \
	java/security/AccessController.class \
	java/security/AlgorithmParameterGenerator.class \
	java/security/AlgorithmParameterGeneratorSpi.class \
	java/security/AlgorithmParameters.class \
	java/security/AlgorithmParametersSpi.class \
	java/security/AllPermission.class \
	java/security/BasicPermission.class \
	java/security/Certificate.class \
	java/security/CodeSource.class \
	java/security/DigestException.class \
	java/security/DigestInputStream.class \
	java/security/DigestOutputStream.class \
	java/security/DomainCombiner.class \
	java/security/DummyKeyPairGenerator.class \
	java/security/DummyMessageDigest.class \
	java/security/DummySignature.class \
	java/security/GeneralSecurityException.class \
	java/security/Guard.class \
	java/security/GuardedObject.class \
	java/security/Identity.class \
	java/security/IdentityScope.class \
	java/security/InvalidAlgorithmParameterException.class \
	java/security/InvalidKeyException.class \
	java/security/InvalidParameterException.class \
	java/security/Key.class \
	java/security/KeyException.class \
	java/security/KeyFactory.class \
	java/security/KeyFactorySpi.class \
	java/security/KeyManagementException.class \
	java/security/KeyPair.class \
	java/security/KeyPairGenerator.class \
	java/security/KeyPairGeneratorSpi.class \
	java/security/KeyStore.class \
	java/security/KeyStoreException.class \
	java/security/KeyStoreSpi.class \
	java/security/MessageDigest.class \
	java/security/MessageDigestSpi.class \
	java/security/NoSuchAlgorithmException.class \
	java/security/NoSuchProviderException.class \
	java/security/Permission.class \
	java/security/PermissionCollection.class \
	java/security/Permissions.class \
	java/security/Policy.class \
	java/security/Principal.class \
	java/security/PrivateKey.class \
	java/security/PrivilegedAction.class \
	java/security/PrivilegedActionException.class \
	java/security/PrivilegedExceptionAction.class \
	java/security/ProtectionDomain.class \
	java/security/Provider.class \
	java/security/ProviderException.class \
	java/security/PublicKey.class \
	java/security/SecureClassLoader.class \
	java/security/SecureRandom.class \
	java/security/SecureRandomSpi.class \
	java/security/Security.class \
	java/security/SecurityPermission.class \
	java/security/Signature.class \
	java/security/SignatureException.class \
	java/security/SignatureSpi.class \
	java/security/SignedObject.class \
	java/security/Signer.class \
	java/security/UnrecoverableKeyException.class \
	java/security/UnresolvedPermission.class 

JAVA_SECURITY_ACL=java/security/acl/Acl.class \
	java/security/acl/AclEntry.class \
	java/security/acl/AclNotFoundException.class \
	java/security/acl/Group.class \
	java/security/acl/LastOwnerException.class \
	java/security/acl/NotOwnerException.class \
	java/security/acl/Owner.class \
	java/security/acl/Permission.class 

JAVA_SECURITY_CERT=java/security/cert/CRL.class \
	java/security/cert/CRLException.class \
	java/security/cert/CRLSelector.class \
	java/security/cert/CertPath.class \
	java/security/cert/CertPathBuilder.class \
	java/security/cert/CertPathBuilderException.class \
	java/security/cert/CertPathBuilderResult.class \
	java/security/cert/CertPathBuilderSpi.class \
	java/security/cert/CertPathParameters.class \
	java/security/cert/CertPathValidator.class \
	java/security/cert/CertPathValidatorException.class \
	java/security/cert/CertPathValidatorResult.class \
	java/security/cert/CertPathValidatorSpi.class \
	java/security/cert/CertSelector.class \
	java/security/cert/CertStore.class \
	java/security/cert/CertStoreException.class \
	java/security/cert/CertStoreParameters.class \
	java/security/cert/CertStoreSpi.class \
	java/security/cert/Certificate.class \
	java/security/cert/CertificateEncodingException.class \
	java/security/cert/CertificateException.class \
	java/security/cert/CertificateExpiredException.class \
	java/security/cert/CertificateFactory.class \
	java/security/cert/CertificateFactorySpi.class \
	java/security/cert/CertificateNotYetValidException.class \
	java/security/cert/CertificateParsingException.class \
	java/security/cert/CollectionCertStoreParameters.class \
	java/security/cert/LDAPCertStoreParameters.class \
	java/security/cert/PKIXBuilderParameters.class \
	java/security/cert/PKIXCertPathBuilderResult.class \
	java/security/cert/PKIXCertPathChecker.class \
	java/security/cert/PKIXCertPathValidatorResult.class \
	java/security/cert/PKIXParameters.class \
	java/security/cert/PolicyNode.class \
	java/security/cert/PolicyQualifierInfo.class \
	java/security/cert/TrustAnchor.class \
	java/security/cert/X509CRL.class \
	java/security/cert/X509CRLEntry.class \
	java/security/cert/X509Certificate.class \
	java/security/cert/X509Extension.class 

JAVA_SECURITY_INTERFACES=	java/security/interfaces/DSAKey.class \
	java/security/interfaces/DSAKeyPairGenerator.class \
	java/security/interfaces/DSAParams.class \
	java/security/interfaces/DSAPrivateKey.class \
	java/security/interfaces/DSAPublicKey.class \
	java/security/interfaces/RSAKey.class \
	java/security/interfaces/RSAMultiPrimePrivateCrtKey.class \
	java/security/interfaces/RSAPrivateCrtKey.class \
	java/security/interfaces/RSAPrivateKey.class \
	java/security/interfaces/RSAPublicKey.class 

JAVA_SECURITY_SPEC=java/security/spec/AlgorithmParameterSpec.class \
	java/security/spec/DSAParameterSpec.class \
	java/security/spec/DSAPrivateKeySpec.class \
	java/security/spec/DSAPublicKeySpec.class \
	java/security/spec/EncodedKeySpec.class \
	java/security/spec/InvalidKeySpecException.class \
	java/security/spec/InvalidParameterSpecException.class \
	java/security/spec/KeySpec.class \
	java/security/spec/PKCS8EncodedKeySpec.class \
	java/security/spec/PSSParameterSpec.class \
	java/security/spec/RSAKeyGenParameterSpec.class \
	java/security/spec/RSAMultiPrimePrivateCrtKeySpec.class \
	java/security/spec/RSAOtherPrimeInfo.class \
	java/security/spec/RSAPrivateCrtKeySpec.class \
	java/security/spec/RSAPrivateKeySpec.class \
	java/security/spec/RSAPublicKeySpec.class \
	java/security/spec/X509EncodedKeySpec.class 

JAVA_SQL=java/sql/Array.class \
	java/sql/BatchUpdateException.class \
	java/sql/Blob.class \
	java/sql/CallableStatement.class \
	java/sql/Clob.class \
	java/sql/Connection.class \
	java/sql/DataTruncation.class \
	java/sql/DatabaseMetaData.class \
	java/sql/Date.class \
	java/sql/Driver.class \
	java/sql/DriverManager.class \
	java/sql/DriverPropertyInfo.class \
	java/sql/ParameterMetaData.class \
	java/sql/PreparedStatement.class \
	java/sql/Ref.class \
	java/sql/ResultSet.class \
	java/sql/ResultSetMetaData.class \
	java/sql/SQLData.class \
	java/sql/SQLException.class \
	java/sql/SQLInput.class \
	java/sql/SQLOutput.class \
	java/sql/SQLPermission.class \
	java/sql/SQLWarning.class \
	java/sql/Savepoint.class \
	java/sql/Statement.class \
	java/sql/Struct.class \
	java/sql/Time.class \
	java/sql/Timestamp.class \
	java/sql/Types.class 

JAVA_TEXT=java/text/Annotation.class \
	java/text/AttributedCharacterIterator.class \
	java/text/AttributedString.class \
	java/text/AttributedStringIterator.class \
	java/text/BreakIterator.class \
	java/text/CharacterIterator.class \
	java/text/ChoiceFormat.class \
	java/text/CollationElementIterator.class \
	java/text/CollationKey.class \
	java/text/Collator.class \
	java/text/DateFormat.class \
	java/text/DateFormatSymbols.class \
	java/text/DecimalFormat.class \
	java/text/DecimalFormatSymbols.class \
	java/text/FieldPosition.class \
	java/text/Format.class \
	java/text/FormatCharacterIterator.class \
	java/text/MessageFormat.class \
	java/text/NumberFormat.class \
	java/text/ParseException.class \
	java/text/ParsePosition.class \
	java/text/RuleBasedCollator.class \
	java/text/SimpleDateFormat.class \
	java/text/StringCharacterIterator.class 

JAVA_UTIL_JAR=java/util/jar/Attributes.class \
	java/util/jar/JarEntry.class \
	java/util/jar/JarException.class \
	java/util/jar/JarFile.class \
	java/util/jar/JarInputStream.class \
	java/util/jar/JarOutputStream.class \
	java/util/jar/Manifest.class 

JAVA_UTIL_LOGGING=java/util/logging/ConsoleHandler.class \
	java/util/logging/ErrorManager.class \
	java/util/logging/FileHandler.class \
	java/util/logging/Filter.class \
	java/util/logging/Formatter.class \
	java/util/logging/Handler.class \
	java/util/logging/Level.class \
	java/util/logging/LogManager.class \
	java/util/logging/LogRecord.class \
	java/util/logging/Logger.class \
	java/util/logging/LoggingPermission.class \
	java/util/logging/MemoryHandler.class \
	java/util/logging/SimpleFormatter.class \
	java/util/logging/SocketHandler.class \
	java/util/logging/StreamHandler.class \
	java/util/logging/XMLFormatter.class 

JAVA_UTIL_PREFS=java/util/prefs/AbstractPreferences.class \
	java/util/prefs/BackingStoreException.class \
	java/util/prefs/InvalidPreferencesFormatException.class \
	java/util/prefs/NodeChangeEvent.class \
	java/util/prefs/NodeChangeListener.class \
	java/util/prefs/PreferenceChangeEvent.class \
	java/util/prefs/PreferenceChangeListener.class \
	java/util/prefs/Preferences.class \
	java/util/prefs/PreferencesFactory.class 

JAVA_UTIL_REGEX=java/util/regex/Matcher.class \
	java/util/regex/Pattern.class \
	java/util/regex/PatternSyntaxException.class 

JAVA_UTIL_ZIP=java/util/zip/Adler32.class \
	java/util/zip/CRC32.class \
	java/util/zip/CheckedInputStream.class \
	java/util/zip/CheckedOutputStream.class \
	java/util/zip/Checksum.class \
	java/util/zip/DataFormatException.class \
	java/util/zip/Deflater.class \
	java/util/zip/DeflaterOutputStream.class \
	java/util/zip/GZIPInputStream.class \
	java/util/zip/GZIPOutputStream.class \
	java/util/zip/Inflater.class \
	java/util/zip/InflaterInputStream.class \
	java/util/zip/ZipConstants.class \
	java/util/zip/ZipEntry.class \
	java/util/zip/ZipException.class \
	java/util/zip/ZipFile.class \
	java/util/zip/ZipInputStream.class \
	java/util/zip/ZipOutputStream.class \

JAVAX_SECURITY_AUTH_X500=javax/security/auth/x500/X500Principal.class

JAVAX_SQL=javax/sql/ConnectionEvent.class \
	javax/sql/ConnectionEventListener.class \
	javax/sql/ConnectionPoolDataSource.class \
	javax/sql/DataSource.class \
	javax/sql/PooledConnection.class \
	javax/sql/RowSet.class \
	javax/sql/RowSetEvent.class \
	javax/sql/RowSetInternal.class \
	javax/sql/RowSetListener.class \
	javax/sql/RowSetMetaData.class \
	javax/sql/RowSetReader.class \
	javax/sql/RowSetWriter.class \
	javax/sql/XAConnection.class \
	javax/sql/XADataSource.class

GNU_JAVA_BEANS=gnu/java/beans/BeanInfoEmbryo.class \
	gnu/java/beans/EmptyBeanInfo.class
#	gnu/java/beans/ExplicitBeanInfo.class
#	gnu/java/beans/IntrospectionIncubator.class

GNU_JAVA_BEANS_INFO= \
	gnu/java/beans/info/ComponentBeanInfo.class

GNU_JAVA_LANG=gnu/java/lang/ArrayHelper.class \
	gnu/java/lang/CharData.class \
	gnu/java/lang/ClassHelper.class \

GNU_JAVA_LANG_REFLECT=gnu/java/lang/reflect/TypeSignature.class

GNU_JAVA_LOCALE=gnu/java/locale/Calendar.class \
	gnu/java/locale/Calendar_de.class \
	gnu/java/locale/Calendar_en.class \
	gnu/java/locale/Calendar_nl.class \
	gnu/java/locale/LocaleInformation.class \
	gnu/java/locale/LocaleInformation_af_ZA.class \
	gnu/java/locale/LocaleInformation_ar_AE.class \
	gnu/java/locale/LocaleInformation_ar_BH.class \
	gnu/java/locale/LocaleInformation_ar_DZ.class \
	gnu/java/locale/LocaleInformation_ar_EG.class \
	gnu/java/locale/LocaleInformation_ar_IN.class \
	gnu/java/locale/LocaleInformation_ar_IQ.class \
	gnu/java/locale/LocaleInformation_ar_JO.class \
	gnu/java/locale/LocaleInformation_ar_KW.class \
	gnu/java/locale/LocaleInformation_ar_LB.class \
	gnu/java/locale/LocaleInformation_ar_LY.class \
	gnu/java/locale/LocaleInformation_ar_MA.class \
	gnu/java/locale/LocaleInformation_ar_OM.class \
	gnu/java/locale/LocaleInformation_ar_QA.class \
	gnu/java/locale/LocaleInformation_ar_SD.class \
	gnu/java/locale/LocaleInformation_ar_SY.class \
	gnu/java/locale/LocaleInformation_ar_TN.class \
	gnu/java/locale/LocaleInformation_ar_YE.class \
	gnu/java/locale/LocaleInformation_be_BY.class \
	gnu/java/locale/LocaleInformation_bn_IN.class \
	gnu/java/locale/LocaleInformation_br_FR.class \
	gnu/java/locale/LocaleInformation_bs_BA.class \
	gnu/java/locale/LocaleInformation_ca_ES.class \
	gnu/java/locale/LocaleInformation_cs_CZ.class \
	gnu/java/locale/LocaleInformation_cy_GB.class \
	gnu/java/locale/LocaleInformation_da_DK.class \
	gnu/java/locale/LocaleInformation_de.class \
	gnu/java/locale/LocaleInformation_de_AT.class \
	gnu/java/locale/LocaleInformation_de_BE.class \
	gnu/java/locale/LocaleInformation_de_CH.class \
	gnu/java/locale/LocaleInformation_de_DE.class \
	gnu/java/locale/LocaleInformation_de_LU.class \
	gnu/java/locale/LocaleInformation_el_GR.class \
	gnu/java/locale/LocaleInformation_en.class \
	gnu/java/locale/LocaleInformation_en_AU.class \
	gnu/java/locale/LocaleInformation_en_BW.class \
	gnu/java/locale/LocaleInformation_en_CA.class \
	gnu/java/locale/LocaleInformation_en_DK.class \
	gnu/java/locale/LocaleInformation_en_GB.class \
	gnu/java/locale/LocaleInformation_en_HK.class \
	gnu/java/locale/LocaleInformation_en_IE.class \
	gnu/java/locale/LocaleInformation_en_IN.class \
	gnu/java/locale/LocaleInformation_en_NZ.class \
	gnu/java/locale/LocaleInformation_en_PH.class \
	gnu/java/locale/LocaleInformation_en_SG.class \
	gnu/java/locale/LocaleInformation_en_US.class \
	gnu/java/locale/LocaleInformation_en_ZA.class \
	gnu/java/locale/LocaleInformation_en_ZW.class \
	gnu/java/locale/LocaleInformation_es_AR.class \
	gnu/java/locale/LocaleInformation_es_BO.class \
	gnu/java/locale/LocaleInformation_es_CL.class \
	gnu/java/locale/LocaleInformation_es_CO.class \
	gnu/java/locale/LocaleInformation_es_CR.class \
	gnu/java/locale/LocaleInformation_es_DO.class \
	gnu/java/locale/LocaleInformation_es_EC.class \
	gnu/java/locale/LocaleInformation_es_ES.class \
	gnu/java/locale/LocaleInformation_es_GT.class \
	gnu/java/locale/LocaleInformation_es_HN.class \
	gnu/java/locale/LocaleInformation_es_MX.class \
	gnu/java/locale/LocaleInformation_es_NI.class \
	gnu/java/locale/LocaleInformation_es_PA.class \
	gnu/java/locale/LocaleInformation_es_PE.class \
	gnu/java/locale/LocaleInformation_es_PR.class \
	gnu/java/locale/LocaleInformation_es_PY.class \
	gnu/java/locale/LocaleInformation_es_SV.class \
	gnu/java/locale/LocaleInformation_es_US.class \
	gnu/java/locale/LocaleInformation_es_UY.class \
	gnu/java/locale/LocaleInformation_es_VE.class \
	gnu/java/locale/LocaleInformation_et_EE.class \
	gnu/java/locale/LocaleInformation_eu_ES.class \
	gnu/java/locale/LocaleInformation_fa_IR.class \
	gnu/java/locale/LocaleInformation_fi_FI.class \
	gnu/java/locale/LocaleInformation_fo_FO.class \
	gnu/java/locale/LocaleInformation_fr_BE.class \
	gnu/java/locale/LocaleInformation_fr_CA.class \
	gnu/java/locale/LocaleInformation_fr_CH.class \
	gnu/java/locale/LocaleInformation_fr_FR.class \
	gnu/java/locale/LocaleInformation_fr_LU.class \
	gnu/java/locale/LocaleInformation_ga_IE.class \
	gnu/java/locale/LocaleInformation_gd_GB.class \
	gnu/java/locale/LocaleInformation_gl_ES.class \
	gnu/java/locale/LocaleInformation_gv_GB.class \
	gnu/java/locale/LocaleInformation_he_IL.class \
	gnu/java/locale/LocaleInformation_hi_IN.class \
	gnu/java/locale/LocaleInformation_hr_HR.class \
	gnu/java/locale/LocaleInformation_hu_HU.class \
	gnu/java/locale/LocaleInformation_id_ID.class \
	gnu/java/locale/LocaleInformation_it_CH.class \
	gnu/java/locale/LocaleInformation_it_IT.class \
	gnu/java/locale/LocaleInformation_iw_IL.class \
	gnu/java/locale/LocaleInformation_ja_JP.class \
	gnu/java/locale/LocaleInformation_ka_GE.class \
	gnu/java/locale/LocaleInformation_kl_GL.class \
	gnu/java/locale/LocaleInformation_ko_KR.class \
	gnu/java/locale/LocaleInformation_kw_GB.class \
	gnu/java/locale/LocaleInformation_lt_LT.class \
	gnu/java/locale/LocaleInformation_lv_LV.class \
	gnu/java/locale/LocaleInformation_mi_NZ.class \
	gnu/java/locale/LocaleInformation_mk_MK.class \
	gnu/java/locale/LocaleInformation_mr_IN.class \
	gnu/java/locale/LocaleInformation_mt_MT.class \
	gnu/java/locale/LocaleInformation_nl.class \
	gnu/java/locale/LocaleInformation_nl_BE.class \
	gnu/java/locale/LocaleInformation_nl_NL.class \
	gnu/java/locale/LocaleInformation_nn_NO.class \
	gnu/java/locale/LocaleInformation_no_NO.class \
	gnu/java/locale/LocaleInformation_oc_FR.class \
	gnu/java/locale/LocaleInformation_pl_PL.class \
	gnu/java/locale/LocaleInformation_pt_BR.class \
	gnu/java/locale/LocaleInformation_pt_PT.class \
	gnu/java/locale/LocaleInformation_ro_RO.class \
	gnu/java/locale/LocaleInformation_ru_RU.class \
	gnu/java/locale/LocaleInformation_ru_UA.class \
	gnu/java/locale/LocaleInformation_se_NO.class \
	gnu/java/locale/LocaleInformation_sk_SK.class \
	gnu/java/locale/LocaleInformation_sl_SI.class \
	gnu/java/locale/LocaleInformation_sq_AL.class \
	gnu/java/locale/LocaleInformation_sr_YU.class \
	gnu/java/locale/LocaleInformation_sv_FI.class \
	gnu/java/locale/LocaleInformation_sv_SE.class \
	gnu/java/locale/LocaleInformation_ta_IN.class \
	gnu/java/locale/LocaleInformation_te_IN.class \
	gnu/java/locale/LocaleInformation_tg_TJ.class \
	gnu/java/locale/LocaleInformation_tl_PH.class \
	gnu/java/locale/LocaleInformation_tr_TR.class \
	gnu/java/locale/LocaleInformation_uk_UA.class \
	gnu/java/locale/LocaleInformation_ur_PK.class \
	gnu/java/locale/LocaleInformation_uz_UZ.class \
	gnu/java/locale/LocaleInformation_vi_VN.class \
	gnu/java/locale/LocaleInformation_yi_US.class \
	gnu/java/locale/LocaleInformation_zh_CN.class \
	gnu/java/locale/LocaleInformation_zh_HK.class \
	gnu/java/locale/LocaleInformation_zh_SG.class \
	gnu/java/locale/LocaleInformation_zh_TW.class \

GNU_JAVA_MATH=gnu/java/math/MPN.class \

GNU_JAVA_NET=$(wildcard gnu/java/net/*.class)

GNU_JAVA_SECURITY_DER=gnu/java/security/der/BitString.class \
	gnu/java/security/der/DER.class \
	gnu/java/security/der/DEREncodingException.class \
	gnu/java/security/der/DERReader.class \
	gnu/java/security/der/DERValue.class \
	gnu/java/security/der/DERWriter.class \

GNU_JAVA_SECURITY_PROVIDER=gnu/java/security/provider/DSAKeyFactory.class \
	gnu/java/security/provider/DSAKeyPairGenerator.class \
	gnu/java/security/provider/DSAParameterGenerator.class \
	gnu/java/security/provider/DSAParameters.class \
	gnu/java/security/provider/DSASignature.class \
	gnu/java/security/provider/DefaultPolicy.class \
	gnu/java/security/provider/Gnu.class \
	gnu/java/security/provider/GnuDSAPrivateKey.class \
	gnu/java/security/provider/GnuDSAPublicKey.class \
	gnu/java/security/provider/MD5.class \
	gnu/java/security/provider/SHA.class \
	gnu/java/security/provider/SHA1PRNG.class \
	gnu/java/security/provider/X509CertificateFactory.class \

GNU_JAVA_SECURITY_UTIL= gnu/java/security/util/Prime.class

GNU_JAVA_SECURITY=gnu/java/security/Engine.class \
	gnu/java/security/OID.class

GNU_JAVA_SECURITY_X509=gnu/java/security/x509/X500DistinguishedName.class \
	gnu/java/security/x509/X509CRL.class \
	gnu/java/security/x509/X509CRLEntry.class \
	gnu/java/security/x509/X509Certificate.class \

GNU_JAVA_TEXT= gnu/java/text/BaseBreakIterator.class \
	gnu/java/text/CharacterBreakIterator.class \
	gnu/java/text/LineBreakIterator.class \
	gnu/java/text/SentenceBreakIterator.class \
	gnu/java/text/WordBreakIterator.class

GNU_JAVA_UTIL=gnu/java/util/DoubleEnumeration.class \
	gnu/java/util/EmptyEnumeration.class

GNU_JAVA_UTIL_PREFS=gnu/java/util/prefs/FileBasedFactory.class \
	gnu/java/util/prefs/MemoryBasedFactory.class \
	gnu/java/util/prefs/MemoryBasedPreferences.class \
	gnu/java/util/prefs/NodeReader.class \
	gnu/java/util/prefs/NodeWriter.class

GNU_JAVA_RUNTIME=gnu/gcj/runtime/StringBuffer.class

GNU_JAVA_IO=gnu/java/io/ASN1ParsingException.class \
	gnu/java/io/Base64InputStream.class \
	gnu/java/io/ClassLoaderObjectInputStream.class \
	gnu/java/io/EncodingManager.class \
	gnu/java/io/NullOutputStream.class \
	gnu/java/io/ObjectIdentityWrapper.class

GNU_JAVA_IO_DECODE = \
	gnu/java/io/decode/Decoder8859_1.class \
	gnu/java/io/decode/Decoder8859_2.class \
	gnu/java/io/decode/Decoder8859_3.class \
	gnu/java/io/decode/Decoder8859_4.class \
	gnu/java/io/decode/Decoder8859_5.class \
	gnu/java/io/decode/DecoderEightBitLookup.class \
	gnu/java/io/decode/Decoder.class \
	gnu/java/io/decode/DecoderUTF8.class

GNU_JAVA_IO_ENCODE = \
	gnu/java/io/encode/Encoder8859_1.class \
	gnu/java/io/encode/Encoder8859_2.class \
	gnu/java/io/encode/Encoder8859_3.class \
	gnu/java/io/encode/Encoder8859_4.class \
	gnu/java/io/encode/Encoder8859_5.class \
	gnu/java/io/encode/EncoderEightBitLookup.class \
	gnu/java/io/encode/Encoder.class \
	gnu/java/io/encode/EncoderUTF8.class


GNU_GCJ= gnu/gcj/RawData.class \
	gnu/gcj/Core.class

GNU_GCJ_CONVERT=gnu/gcj/convert/BytesToUnicode.class \
	gnu/gcj/convert/Convert.class \
	gnu/gcj/convert/IOConverter.class \
	gnu/gcj/convert/Input_8859_1.class \
	gnu/gcj/convert/Input_ASCII.class \
	gnu/gcj/convert/Input_EUCJIS.class \
	gnu/gcj/convert/Input_JavaSrc.class \
	gnu/gcj/convert/Input_SJIS.class \
	gnu/gcj/convert/Input_UTF8.class \
	gnu/gcj/convert/Input_iconv.class \
	gnu/gcj/convert/Output_8859_1.class \
	gnu/gcj/convert/Output_ASCII.class \
	gnu/gcj/convert/Output_EUCJIS.class \
	gnu/gcj/convert/Output_JavaSrc.class \
	gnu/gcj/convert/Output_SJIS.class \
	gnu/gcj/convert/Output_UTF8.class \
	gnu/gcj/convert/Output_iconv.class \
	gnu/gcj/convert/UnicodeToBytes.class

GNU_GCJ_IO=gnu/gcj/io/DefaultMimeTypes.class \
	gnu/gcj/io/MimeTypes.class \
	gnu/gcj/io/SimpleSHSStream.class

GNU_GCJ_RUNTIME= \
	gnu/gcj/runtime/FileDeleter.class \
	gnu/gcj/runtime/FinalizerThread.class \
	gnu/gcj/runtime/FirstThread.class \
	gnu/gcj/runtime/JNIWeakRef.class \
	gnu/gcj/runtime/MethodRef.class \
	gnu/gcj/runtime/NameFinder.class \
	gnu/gcj/runtime/SharedLibHelper.class \
	gnu/gcj/runtime/SharedLibLoader.class \
	gnu/gcj/runtime/StackTrace.class \
	gnu/gcj/runtime/StringBuffer.class \
	gnu/gcj/runtime/VMClassLoader.class


GNU_JAVA_NIO= \
	gnu/java/nio/ChannelInputStream.class \
	gnu/java/nio/ChannelOutputStream.class \
	gnu/java/nio/DatagramChannelImpl.class \
	gnu/java/nio/DatagramChannelSelectionKey.class \
	gnu/java/nio/FileLockImpl.class \
	gnu/java/nio/InputStreamChannel.class \
	gnu/java/nio/NIOConstants.class \
	gnu/java/nio/NIODatagramSocket.class \
	gnu/java/nio/NIOServerSocket.class \
	gnu/java/nio/NIOSocket.class \
	gnu/java/nio/OutputStreamChannel.class \
	gnu/java/nio/PipeImpl.class \
	gnu/java/nio/SelectionKeyImpl.class \
	gnu/java/nio/SelectorImpl.class \
	gnu/java/nio/SelectorProviderImpl.class \
	gnu/java/nio/ServerSocketChannelImpl.class \
	gnu/java/nio/ServerSocketChannelSelectionKey.class \
	gnu/java/nio/SocketChannelImpl.class \
	gnu/java/nio/SocketChannelSelectionKey.class

GNU_JAVA_NIO_CHARSET= gnu/java/nio/charset/ISO_8859_1.class \
	gnu/java/nio/charset/Provider.class \
	gnu/java/nio/charset/US_ASCII.class \
	gnu/java/nio/charset/UTF_16.class \
	gnu/java/nio/charset/UTF_16BE.class \
	gnu/java/nio/charset/UTF_16Decoder.class \
	gnu/java/nio/charset/UTF_16Encoder.class \
	gnu/java/nio/charset/UTF_16LE.class \
	gnu/java/nio/charset/UTF_8.class

GNU_JAVA_NET= \
	gnu/java/net/HeaderFieldHelper.class \
	gnu/java/net/PlainDatagramSocketImpl.class \
	gnu/java/net/PlainSocketImpl.class \
	gnu/java/net/URLParseError.class

GNU_JAVA_NET_PROTOCOL_CORE= \
	gnu/java/net/protocol/core/Connection.class \
	gnu/java/net/protocol/core/CoreInputStream.class \
	gnu/java/net/protocol/core/Handler.class

GNU_JAVA_NET_PROTOCOL_FILE= \
	gnu/java/net/protocol/file/Connection.class \
	gnu/java/net/protocol/file/Handler.class

GNU_JAVA_NET_PROTOCOL_GCJLIB= \
	gnu/java/net/protocol/gcjlib/Connection.class \
	gnu/java/net/protocol/gcjlib/Handler.class

GNU_JAVA_NET_PROTOCOL_HTTP= \
	gnu/java/net/protocol/http/Connection.class \
	gnu/java/net/protocol/http/Handler.class

GNU_JAVA_NET_PROTOCOL_JAR= \
	gnu/java/net/protocol/jar/Connection.class \
	gnu/java/net/protocol/jar/Handler.class

GNU_CLASSPATH=gnu/classpath/Configuration.class

VPATH=build:classpath:.
all: dirs $(KERNEL) $(VM) $(PLATFORM) $(PROCESSORS) $(DEVICES) $(BAREMETAL_RUNTIME) \
	$(TESTS) \
	$(JAVA_LANG) $(JAVA_NET) $(JAVA_UTIL) $(JAVA_IO) $(JAVA_MATH) \
	$(JAVA_SECURITY) $(JAVA_SECURITY_ACL) $(JAVA_SECURITY_CERT) \
	$(JAVA_SECURITY_INTERFACES) $(JAVA_SECURITY_SPEC) $(JAVA_TEXT) \
	$(JAVA_UTIL_JAR) $(JAVA_UTIL_LOGGING) $(JAVA_UTIL_PREFS) \
	$(JAVA_UTIL_REGEX) $(JAVA_UTIL_ZIP) $(JAVA_LANG_REF) \
	$(JAVA_LANG_REFLECT) $(GNU_JAVA_RUNTIME) $(GNU_JAVA_IO) $(GNU_JAVA_IO_ENCODE) \
	$(GNU_JAVA_IO_DECODE) $(GNU_JAVA_LANG) $(GNU_JAVA_LANG_REFLECT) $(GNU_JAVA_LOCALE) \
	$(GNU_JAVA_MATH) $(GNU_JAVA_SECURITY_DER) $(GNU_JAVA_SECURITY_PROVIDER) \
	$(GNU_JAVA_SECURITY_UTIL) $(GNU_JAVA_TEXT) $(GNU_JAVA_UTIL) $(GNU_JAVA_UTIL_PREFS) \
	$(JAVA_NIO) $(JAVA_NIO_CHANNELS) $(JAVA_NIO_CHANNELS_SPI) $(JAVA_NIO_CHARSET) \
	$(JAVA_NIO_CHARSET_SPI) $(GNU_JAVA_NIO) $(GNU_JAVA_NIO_CHARSET) \
	$(GNU_GCJ) $(GNU_JAVA_NET) $(GNU_JAVA_NET_PROTOCOL_CORE) \
	$(GNU_JAVA_NET_PROTOCOL_FILE) $(GNU_JAVA_NET_PROTOCOL_acdpsGCJLIB) \
	$(GNU_JAVA_NET_PROTOCOL_HTTP) $(GNU_GCJ_RUNTIME) $(GNU_JAVA_SECURITY) \
	$(GNU_JAVA_SECURITY_X509) $(GNU_GCJ_IO) $(JAVA_BEANS_BEANCONTEXT) \
	$(JAVA_BEANS) $(GNU_GCJ_CONVERT) $(JAVA_SQL) $(JAVAX_SQL) \
	$(JAVAX_SECURITY_AUTH_X500) hs $(OBJS) lib


gcj.o:	gcj.cc
	$(HOME)/local/bin/g++ $(CFLAGS) -c -o gcj.o gcj.cc

exception.o:	exception.cc
	$(HOME)/local/bin/g++ $(CFLAGS) -c -o exception.o exception.cc

Memory.o:	Memory.cc
	$(HOME)/local/bin/g++ $(CFLAGS) -c -o Memory.o Memory.cc

$(PROCESSORS_DIR)/Pentium.o : $(PROCESSORS_DIR)/Pentium.cc
	$(HOME)/local/bin/g++ $(CFLAGS) -O2 -c -o $(PROCESSORS_DIR)/Pentium.o $(PROCESSORS_DIR)/Pentium.cc

clean:
	rm -rf build

.PHONY: clean lib

dirs: build/kernel  build/platforms/x86 build/processors/x86

build/kernel:
	mkdir -p build/kernel
	cd build/kernel; ln -s ../baremetal .

build/platforms/x86:
	mkdir -p build/platforms/x86
	cd build/platforms/x86; ln -s ../../baremetal .

build/processors/x86:
	mkdir -p build/processors/x86
	cd build/processors/x86; ln -s ../../baremetal .

zip:
	cd build; echo java/lang/Class.class | zip ../metal -@
	cd build; echo java/lang/Object.class | zip ../metal -@
	cd build; find -name \*.class -print | zip ../metal -@

lib:
	ar rucs libbm.a $(OBJS)
	cp libbm.a /home/jom/local/lib

hs:
	$(HOME)/local/bin/gcjh $(basename $(wildcard build/baremetal/kernel/*.class))
	$(HOME)/local/bin/gcjh $(basename $(wildcard build/baremetal/vm/*.class))
	$(HOME)/local/bin/gcjh $(basename $(wildcard build/baremetal/runtime/*.class))
	$(HOME)/local/bin/gcjh $(basename $(wildcard build/baremetal/platform/*.class))
	$(HOME)/local/bin/gcjh $(basename $(wildcard build/baremetal/processor/*.class))
	$(HOME)/local/bin/gcjh $(basename $(wildcard build/baremetal/devices/*.class))
	$(HOME)/local/bin/gcjh build/java/lang/System
	$(HOME)/local/bin/gcjh build/java/lang/Runtime
	$(HOME)/local/bin/gcjh build/java/lang/String
	$(HOME)/local/bin/gcjh build/java/lang/reflect/Modifier
