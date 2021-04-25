package org.jikesrvm.runtime;

import org.jam.cpu.intel.Tsc;
import org.jam.driver.serial.PcBootSerialPort;
import org.jam.math.Math;
import org.jikesrvm.VM;
import org.vmmagic.unboxed.Offset;

@org.vmmagic.pragma.Uninterruptible
public final class SysCallImpl extends org.jikesrvm.runtime.SysCall
{

  @java.lang.Override
  public void sysConsoleWriteChar(char v)
  {
    PcBootSerialPort.putChar(v);
  }

  private final static char digitBuffer[] = new char[21];
  private final static char hexDigits[]   = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
      'f' };

  @java.lang.Override
  public void sysConsoleWriteInteger(int value, int hexToo)
  {
    if (hexToo > 0)
    {
      int size = 1;
      long copy = value >>> 4;
      int mask = (1 << 4) - 1;
      while (copy != 0)
      {
        size++;
        copy >>>= 4;
      }
      // Quick path for single character strings
      if (size == 1)
      {
        PcBootSerialPort.putChar('0');
        PcBootSerialPort.putChar('x');
        PcBootSerialPort.putChar(hexDigits[(int) value & mask]);
        return;
      }

      // Encode into buffer
      // char[] buffer = new char[size];
      int i = size + 2; // +2 for '0x' lead
      do
      {
        digitBuffer[--i] = hexDigits[(int) value & mask];
        value >>>= 4;
      } while (value != 0);
      digitBuffer[0] = '0';
      digitBuffer[1] = 'x';

      for (i = 0; i < size + 2; i++)
      {
        PcBootSerialPort.putChar(digitBuffer[i]);
      }
    } else
    {
      int i;
      int val = value;
      for (i = 0; i < 11; i++)
      {
        digitBuffer[i] = ' ';
      }
      digitBuffer[10] = '0';
      if (val < 0)
      {
        val = -val;
      }
      for (i = 10; val > 0; i--)
      {
        digitBuffer[i] = hexDigits[(int) (val % 10)];
        val /= 10;
      }
      if ((value & 0x80000000) != 0)
      {
        digitBuffer[i] = '-';
      }
      for (; i < 11; i++)
      {
        PcBootSerialPort.putChar(digitBuffer[i]);
      }
    }
  }

  @java.lang.Override
  public void sysConsoleWriteLong(long value, int hexToo)
  {
    if (hexToo > 0)
    {
      int size = 1;
      long copy = value >>> 4;
      int mask = (1 << 4) - 1;
      while (copy != 0)
      {
        size++;
        copy >>>= 4;
      }
      // Quick path for single character strings
      if (size == 1)
      {
        PcBootSerialPort.putChar('0');
        PcBootSerialPort.putChar('x');
        PcBootSerialPort.putChar(hexDigits[(int) value & mask]);
        return;
      }

      // Encode into buffer
      // char[] buffer = new char[size];
      int i = size + 2; // +2 for '0x' lead
      do
      {
        digitBuffer[--i] = hexDigits[(int) value & mask];
        value >>>= 4;
      } while (value != 0);
      digitBuffer[0] = '0';
      digitBuffer[1] = 'x';

      for (i = 0; i < size + 2; i++)
      {
        PcBootSerialPort.putChar(digitBuffer[i]);
      }
    } else
    {
      int i;
      long val = value;
      for (i = 0; i < 21; i++)
      {
        digitBuffer[i] = ' ';
      }
      digitBuffer[20] = '0';
      if (val < 0)
      {
        val = -val;
      }
      for (i = 20; val > 0; i--)
      {
        digitBuffer[i] = hexDigits[(int) (val % 10)];
        val /= 10;
      }
      if ((value & 0x8000000000000000L) != 0)
      {
        digitBuffer[i] = '-';
      }
      for (; i < 21; i++)
      {
        PcBootSerialPort.putChar(digitBuffer[i]);
      }
    }
  }

  @java.lang.Override
  public void sysConsoleWriteDouble(double value, int postDecimalDigits)
  {
  }

  @java.lang.Override
  public void sysConsoleFlushErrorAndTrace()
  {
  }

  @java.lang.Override
  public void sysExit(int value)
  {
    VM.sysWriteln("Halting ", value);
    Magic.halt();
  }

  @java.lang.Override
  public int sysArg(int argno, byte[] buf, int buflen)
  {
    VM.sysWriteln("ARG ", argno);
    return 0;
  }

  @java.lang.Override
  public int sysGetenv(byte[] varName, byte[] buf, int limit)
  {
    VM.sysWriteln("GETENV ", varName[0]);
    return 0;
  }

  @java.lang.Override
  public void sysCopy(org.vmmagic.unboxed.Address dst, org.vmmagic.unboxed.Address src, org.vmmagic.unboxed.Extent cnt)
  {
    VM.sysWriteln("COPY ", dst);
  }

  @java.lang.Override
  public void sysMemmove(org.vmmagic.unboxed.Address dst, org.vmmagic.unboxed.Address src,
  org.vmmagic.unboxed.Extent cnt)
  {
    VM.sysWriteln("MEMMOVE ", dst);
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysMalloc(int length)
  {
    VM.sysWriteln("MALLOC ", length);
    return null;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysCalloc(int length)
  {
    VM.sysWriteln("CALLOC ", length);
    return null;
  }

  @java.lang.Override
  public void sysFree(org.vmmagic.unboxed.Address location)
  {
    VM.sysWriteln("FREE ", location);
  }

  @java.lang.Override
  public void sysZeroNT(org.vmmagic.unboxed.Address dst, org.vmmagic.unboxed.Extent cnt)
  {
    VM.sysWrite("ZERONT ", dst);
    VM.sysWriteln(" ", cnt.toInt());
  }

  @java.lang.Override
  public void sysZero(org.vmmagic.unboxed.Address dst, org.vmmagic.unboxed.Extent cnt)
  {
    for(int i=0; i < cnt.toInt(); i++)
    {
      dst.store((byte) 0, Offset.zero().plus(i));
    }
  }

  @java.lang.Override
  public void sysZeroPages(org.vmmagic.unboxed.Address dst, int cnt)
  {
    VM.sysWriteln("ZEROPAGES ", dst);
  }

  @java.lang.Override
  public void sysSyncCache(org.vmmagic.unboxed.Address address, int size)
  {
    VM.sysWriteln("SYNCACHE ", address);
  }

  @java.lang.Override
  public int sysPerfEventInit(int events)
  {
    VM.sysWriteln("PERFEVENT INIT ", events);
    return 0;
  }

  @java.lang.Override
  public int sysPerfEventCreate(int id, byte[] name)
  {
    VM.sysWriteln("PERFEVENT CREATE ", id);
    return 0;
  }

  @java.lang.Override
  public void sysPerfEventEnable()
  {
    VM.sysWriteln("PERFEVENT ENABLE");
  }

  @java.lang.Override
  public void sysPerfEventDisable()
  {
    VM.sysWriteln("PERFEVENT DISABLE");
  }

  @java.lang.Override
  public int sysPerfEventRead(int id, long[] values)
  {
    VM.sysWriteln("PERFEVENT READ ", values.length);
    return 0;
  }

  @java.lang.Override
  public int sysReadByte(int fd)
  {
    VM.sysWriteln("READ BYTE ", fd);
    return 0;
  }

  @java.lang.Override
  public int sysWriteByte(int fd, int data)
  {
    VM.sysWriteln("WRITE BYTE ", fd);
    return 0;
  }

  @java.lang.Override
  public int sysReadBytes(int fd, org.vmmagic.unboxed.Address buf, int cnt)
  {
    VM.sysWriteln("READ BYTES ", buf);
    return 0;
  }

  @java.lang.Override
  public int sysWriteBytes(int fd, org.vmmagic.unboxed.Address buf, int cnt)
  {
    VM.sysWriteln("WRITE BYTES ", buf);
    return 0;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysMMap(org.vmmagic.unboxed.Address start, org.vmmagic.unboxed.Extent length,
  int protection, int flags, int fd, org.vmmagic.unboxed.Offset offset)
  {
    VM.sysWriteln("MMAP ", start);
    VM.sysWrite(" ", length.toInt());
    VM.sysWriteln(" ", offset.toInt());

    return start;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysMMapErrno(org.vmmagic.unboxed.Address start, org.vmmagic.unboxed.Extent length,
  int protection, int flags, int fd, org.vmmagic.unboxed.Offset offset)
  {
    VM.sysWrite("MMAP ERRNO ", start);
    VM.sysWrite(" ", length.toInt());
    VM.sysWriteln(" ", offset.toInt());

    return start;
  }

  @java.lang.Override
  public int sysMProtect(org.vmmagic.unboxed.Address start, org.vmmagic.unboxed.Extent length, int prot)
  {
    VM.sysWriteln("MPROTECT ", start);
    return 0;
  }

  @java.lang.Override
  public int sysNumProcessors()
  {
    return 1;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Word sysThreadCreate(org.vmmagic.unboxed.Address ip, org.vmmagic.unboxed.Address fp,
  org.vmmagic.unboxed.Address tr, org.vmmagic.unboxed.Address jtoc)
  {
    VM.sysWriteln("THREAD CREATE ", tr);
    return null;
  }

  @java.lang.Override
  public int sysThreadBindSupported()
  {
    VM.sysWriteln("THREAD BIND");
    return 0;
  }

  @java.lang.Override
  public void sysThreadBind(int cpuId)
  {
    VM.sysWriteln("TRHEAD BIND ", cpuId);
  }

  @java.lang.Override
  public void sysThreadYield()
  {
    VM.sysWriteln("THREAD YIELD");
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Word sysGetThreadId()
  {
    VM.sysWriteln("GETTHREAD ID");
    return null;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Word sysGetThreadPriorityHandle()
  {
    VM.sysWriteln("GETTHREAD PRIORITY HANDLE");
    return null;
  }

  @java.lang.Override
  public int sysGetThreadPriority(org.vmmagic.unboxed.Word thread, org.vmmagic.unboxed.Word handle)
  {
    VM.sysWriteln("GETTHREAD PRIORITY");
    return 0;
  }

  @java.lang.Override
  public int sysSetThreadPriority(org.vmmagic.unboxed.Word thread, org.vmmagic.unboxed.Word handle, int priority)
  {
    VM.sysWriteln("SETTHREAD PRIORITY ", thread);
    return 0;
  }

  @java.lang.Override
  public int sysStashVMThread(org.jikesrvm.scheduler.RVMThread vmThread)
  {
    VM.sysWriteln("STASH VMTHREAD ", Magic.objectAsAddress(vmThread));
    return 0;
  }

  @java.lang.Override
  public void sysThreadTerminate()
  {
    VM.sysWriteln("THREAD TERMINATE");
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Word sysMonitorCreate()
  {
    VM.sysWriteln("MONITOR CREATE");
    return null;
  }

  @java.lang.Override
  public void sysMonitorDestroy(org.vmmagic.unboxed.Word monitor)
  {
    VM.sysWriteln("MONITOR DESTROY");
  }

  @java.lang.Override
  public int sysMonitorEnter(org.vmmagic.unboxed.Word monitor)
  {
    VM.sysWriteln("MONITOR ENTER ", monitor);
    return 0;
  }

  @java.lang.Override
  public int sysMonitorExit(org.vmmagic.unboxed.Word monitor)
  {
    VM.sysWriteln("MONITOR EXIT ", monitor);
    return 0;
  }

  @java.lang.Override
  public void sysMonitorTimedWaitAbsolute(org.vmmagic.unboxed.Word monitor, long whenWakeupNanos)
  {
    VM.sysWriteln("MONITOR TIMED WAIT ABSOLUTE ", monitor);
  }

  @java.lang.Override
  public void sysMonitorWait(org.vmmagic.unboxed.Word monitor)
  {
    VM.sysWriteln("MONITOR WAIT ", monitor);
  }

  @java.lang.Override
  public void sysMonitorBroadcast(org.vmmagic.unboxed.Word monitor)
  {
    VM.sysWriteln("MONITOR BROADCAST ", monitor);
  }

  @java.lang.Override
  public long sysLongDivide(long x, long y)
  {
    return Math.div64(x, y);
  }

  @java.lang.Override
  public long sysLongRemainder(long x, long y)
  {
    return Math.mod64(x, y);
  }

  @java.lang.Override
  public float sysLongToFloat(long x)
  {
    VM.sysWriteln("LONGTOFLOAT ", x);
    return 0;
  }

  @java.lang.Override
  public double sysLongToDouble(long x)
  {
    VM.sysWriteln("LONGTODOUBLE ", x);
    return 0.0;
  }

  @java.lang.Override
  public int sysFloatToInt(float x)
  {
    VM.sysWriteln("FLOATTOINT ", x);
    return 0;
  }

  @java.lang.Override
  public int sysDoubleToInt(double x)
  {
    VM.sysWriteln("DOUBLETOINT ", x);
    return 0;
  }

  @java.lang.Override
  public long sysFloatToLong(float x)
  {
    VM.sysWriteln("FLOATTOLONG ", x);
    return 0;
  }

  @java.lang.Override
  public long sysDoubleToLong(double x)
  {
    VM.sysWriteln("DOUBLETOLONG ", x);
    return 0;
  }

  @java.lang.Override
  public double sysDoubleRemainder(double x, double y)
  {
    VM.sysWriteln("DOUBLE REMAINDER ", x);
    return 0.0;
  }

  @java.lang.Override
  public float sysPrimitiveParseFloat(byte[] buf)
  {
    VM.sysWriteln("PARSE FLOAT ");
    for (int i = 0; i < buf.length; i++)
      VM.sysWrite(buf[i]);
    VM.sysWriteln();
    return 0;
  }

  @java.lang.Override
  public int sysPrimitiveParseInt(byte[] buf)
  {
    VM.sysWriteln("PARSE INT ");
    for (int i = 0; i < buf.length; i++)
      VM.sysWrite(buf[i]);
    VM.sysWriteln();
    return 0;
  }

  @java.lang.Override
  public long sysPrimitiveParseLong(byte[] buf)
  {
    VM.sysWriteln("PARSE LONG ");
    for (int i = 0; i < buf.length; i++)
      VM.sysWrite(buf[i]);
    VM.sysWriteln();
    return 0;
  }

  @java.lang.Override
  public long sysParseMemorySize(byte[] sizeName, byte[] sizeFlag, byte[] defaultFactor, int roundTo, byte[] argToken,
  byte[] subArg)
  {
    VM.sysWrite("PARSE MEMORY SIZE ");
    for (int i = 0; i < sizeName.length; i++)
      VM.sysWrite(sizeName[i]);
    VM.sysWriteln();
    return 0;
  }

  @java.lang.Override
  public long sysCurrentTimeMillis()
  {
    VM.sysWriteln("CURRENT TIME MILLIS ");
    return 0;
  }

  @java.lang.Override
  public long sysNanoTime()
  {
    // VM.sysWriteln("NANOTIME");
    return (Magic.getTimeBase() / Tsc.cyclesPer1000Ns) * 1000;
  }

  @java.lang.Override
  public void sysNanoSleep(long howLongNanos)
  {
    VM.sysWriteln("NANOSLEEP ", howLongNanos);
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysDlopen(byte[] libname)
  {
    VM.sysWrite("DLOPEN ");
    for (int i = 0; i < libname.length; i++)
      VM.sysWrite(libname[i]);
    VM.sysWriteln();
    return null;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysDlsym(org.vmmagic.unboxed.Address libHandler, byte[] symbolName)
  {
    VM.sysWrite("DLSYM ", libHandler);
    for (int i = 0; i < symbolName.length; i++)
      VM.sysWrite(symbolName[i]);
    VM.sysWriteln();
    return null;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysVaCopy(org.vmmagic.unboxed.Address va_list)
  {
    VM.sysWriteln("VACOPY ", va_list);
    return null;
  }

  @java.lang.Override
  public void sysVaEnd(org.vmmagic.unboxed.Address va_list)
  {
    VM.sysWriteln("VAEND ", va_list);
  }

  @java.lang.Override
  public boolean sysVaArgJboolean(org.vmmagic.unboxed.Address va_list)
  {
    VM.sysWriteln("VAARG JBOOLEAN ", va_list);
    return false;
  }

  @java.lang.Override
  public byte sysVaArgJbyte(org.vmmagic.unboxed.Address va_list)
  {
    VM.sysWriteln("VAARG JBYTE ", va_list);
    return 0;
  }

  @java.lang.Override
  public char sysVaArgJchar(org.vmmagic.unboxed.Address va_list)
  {
    VM.sysWriteln("VAARG JCHAR ", va_list);
    return 0;
  }

  @java.lang.Override
  public short sysVaArgJshort(org.vmmagic.unboxed.Address va_list)
  {
    VM.sysWriteln("VAARG JSHORT ", va_list);
    return 0;
  }

  @java.lang.Override
  public int sysVaArgJint(org.vmmagic.unboxed.Address va_list)
  {
    VM.sysWriteln("VAARG JINT ", va_list);
    return 0;
  }

  @java.lang.Override
  public long sysVaArgJlong(org.vmmagic.unboxed.Address va_list)
  {
    VM.sysWriteln("VAARG JLONG ", va_list);
    return 0;
  }

  @java.lang.Override
  public float sysVaArgJfloat(org.vmmagic.unboxed.Address va_list)
  {
    VM.sysWriteln("VAARG JFLOAT ", va_list);
    return 0;
  }

  @java.lang.Override
  public double sysVaArgJdouble(org.vmmagic.unboxed.Address va_list)
  {
    VM.sysWriteln("VAARG JDOUBLE ", va_list);
    return 0.0;
  }

  @java.lang.Override
  public int sysVaArgJobject(org.vmmagic.unboxed.Address va_list)
  {
    VM.sysWriteln("VAARG JOBJECT ", va_list);
    return 0;
  }

  @java.lang.Override
  public void sysEnableAlignmentChecking()
  {
    VM.sysWriteln("ENABLE ALIGNMENT CHECK");
  }

  @java.lang.Override
  public void sysDisableAlignmentChecking()
  {
    VM.sysWriteln("DISABLE ALIGNMENT CHECK");
  }

  @java.lang.Override
  public void sysReportAlignmentChecking()
  {
    VM.sysWriteln("REPORT ALIGNMENT CHECK");
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address gcspyDriverAddStream(org.vmmagic.unboxed.Address driver, int id)
  {
    return null;
  }

  @java.lang.Override
  public void gcspyDriverEndOutput(org.vmmagic.unboxed.Address driver)
  {
  }

  @java.lang.Override
  public void gcspyDriverInit(org.vmmagic.unboxed.Address driver, int id, org.vmmagic.unboxed.Address serverName,
  org.vmmagic.unboxed.Address driverName, org.vmmagic.unboxed.Address title, org.vmmagic.unboxed.Address blockInfo,
  int tileNum, org.vmmagic.unboxed.Address unused, int mainSpace)
  {
  }

  @java.lang.Override
  public void gcspyDriverInitOutput(org.vmmagic.unboxed.Address driver)
  {
  }

  @java.lang.Override
  public void gcspyDriverResize(org.vmmagic.unboxed.Address driver, int size)
  {
  }

  @java.lang.Override
  public void gcspyDriverSetTileNameRange(org.vmmagic.unboxed.Address driver, int i, org.vmmagic.unboxed.Address start,
  org.vmmagic.unboxed.Address end)
  {
  }

  @java.lang.Override
  public void gcspyDriverSetTileName(org.vmmagic.unboxed.Address driver, int i, org.vmmagic.unboxed.Address start,
  long value)
  {
  }

  @java.lang.Override
  public void gcspyDriverSpaceInfo(org.vmmagic.unboxed.Address driver, org.vmmagic.unboxed.Address info)
  {
  }

  @java.lang.Override
  public void gcspyDriverStartComm(org.vmmagic.unboxed.Address driver)
  {
  }

  @java.lang.Override
  public void gcspyDriverStream(org.vmmagic.unboxed.Address driver, int id, int len)
  {
  }

  @java.lang.Override
  public void gcspyDriverStreamByteValue(org.vmmagic.unboxed.Address driver, byte value)
  {
  }

  @java.lang.Override
  public void gcspyDriverStreamShortValue(org.vmmagic.unboxed.Address driver, short value)
  {
  }

  @java.lang.Override
  public void gcspyDriverStreamIntValue(org.vmmagic.unboxed.Address driver, int value)
  {
  }

  @java.lang.Override
  public void gcspyDriverSummary(org.vmmagic.unboxed.Address driver, int id, int len)
  {
  }

  @java.lang.Override
  public void gcspyDriverSummaryValue(org.vmmagic.unboxed.Address driver, int value)
  {
  }

  @java.lang.Override
  public void gcspyIntWriteControl(org.vmmagic.unboxed.Address driver, int id, int tileNum)
  {
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address gcspyMainServerAddDriver(org.vmmagic.unboxed.Address addr)
  {
    return null;
  }

  @java.lang.Override
  public void gcspyMainServerAddEvent(org.vmmagic.unboxed.Address server, int event, org.vmmagic.unboxed.Address name)
  {
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address gcspyMainServerInit(int port, int len, org.vmmagic.unboxed.Address name,
  int verbose)
  {
    return null;
  }

  @java.lang.Override
  public int gcspyMainServerIsConnected(org.vmmagic.unboxed.Address server, int event)
  {
    return 0;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address gcspyMainServerOuterLoop()
  {
    return null;
  }

  @java.lang.Override
  public void gcspyMainServerSafepoint(org.vmmagic.unboxed.Address server, int event)
  {
  }

  @java.lang.Override
  public void gcspyMainServerSetGeneralInfo(org.vmmagic.unboxed.Address server, org.vmmagic.unboxed.Address info)
  {
  }

  @java.lang.Override
  public void gcspyMainServerStartCompensationTimer(org.vmmagic.unboxed.Address server)
  {
  }

  @java.lang.Override
  public void gcspyMainServerStopCompensationTimer(org.vmmagic.unboxed.Address server)
  {
  }

  @java.lang.Override
  public void gcspyStartserver(org.vmmagic.unboxed.Address server, int wait,
  org.vmmagic.unboxed.Address serverOuterLoop)
  {
  }

  @java.lang.Override
  public void gcspyStreamInit(org.vmmagic.unboxed.Address stream, int id, int dataType,
  org.vmmagic.unboxed.Address name, int minValue, int maxValue, int zeroValue, int defaultValue,
  org.vmmagic.unboxed.Address pre, org.vmmagic.unboxed.Address post, int presentation, int paintStyle,
  int maxStreamIndex, int red, int green, int blue)
  {
  }

  @java.lang.Override
  public void gcspyFormatSize(org.vmmagic.unboxed.Address buffer, int size)
  {
  }

  @java.lang.Override
  public int gcspySprintf(org.vmmagic.unboxed.Address str, org.vmmagic.unboxed.Address format,
  org.vmmagic.unboxed.Address value)
  {
    return 0;
  }

  @java.lang.Override
  public void sysStackAlignmentTest()
  {
    VM.sysWriteln("STACK ALIGNMENT TEST");
  }

  @java.lang.Override
  public void sysArgumentPassingTest(long firstLong, long secondLong, long thirdLong, long fourthLong, long fifthLong,
  long sixthLong, long seventhLong, long eightLong, double firstDouble, double secondDouble, double thirdDouble,
  double fourthDouble, double fifthDouble, double sixthDouble, double seventhDouble, double eightDouble, int firstInt,
  long ninthLong, byte[] firstByteArray, double ninthDouble, org.vmmagic.unboxed.Address firstAddress)
  {
    VM.sysWriteln("ARG PASSING TEST");
  }

  @java.lang.Override
  public void sysArgumentPassingSeveralLongsAndSeveralDoubles(long firstLong, long secondLong, long thirdLong,
  long fourthLong, long fifthLong, long sixthLong, long seventhLong, long eightLong, double firstDouble,
  double secondDouble, double thirdDouble, double fourthDouble, double fifthDouble, double sixthDouble,
  double seventhDouble, double eightDouble)
  {
    VM.sysWriteln("ARG PASSING LONGS DOUBLES");
  }

  @java.lang.Override
  public void sysArgumentPassingSeveralFloatsAndSeveralInts(float firstFloat, float secondFloat, float thirdFloat,
  float fourthFloat, float fifthFloat, float sixthFloat, float seventhFloat, float eightFloat, int firstInt,
  int secondInt, int thirdInt, int fourthInt, int fifthInt, int sixthInt, int seventhInt, int eightInt)
  {
    VM.sysWriteln("ARG PASSING FLOATS INTS");
  }
}