package org.jikesrvm.runtime;

import org.jam.driver.serial.PcBootSerialPort;
import org.jikesrvm.VM;

@org.vmmagic.pragma.Uninterruptible
public final class SysCallImpl extends org.jikesrvm.runtime.SysCall {

  @java.lang.Override
  public void sysConsoleWriteChar(char v) {
      PcBootSerialPort.putChar(v);
  }

  private final static char digitBuffer[] = new char[21];
  private final static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd','e', 'f' };
  
  @java.lang.Override
  public void sysConsoleWriteInteger(int value, int hexToo) {
      if(hexToo > 0) {
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
              PcBootSerialPort.putChar(hexDigits[(int)value & mask]);
              return;
          }

          // Encode into buffer
//          char[] buffer = new char[size];
          int i = size + 2;   // +2 for '0x' lead
          do
            {
              digitBuffer[--i] = hexDigits[(int) value & mask];
              value >>>= 4;
            }
          while (value != 0);
          digitBuffer[0] = '0';
          digitBuffer[1] = 'x';
          
          for(i=0; i<size+2; i++) {
              PcBootSerialPort.putChar(digitBuffer[i]);
          }
      } else {
          int i;
          int val=value;
          for(i=0; i<11; i++) {
              digitBuffer[i] = ' ';
          }
          digitBuffer[10] = '0';
          if(val < 0)
          {
            val = -val;
          }
          for(i=10; val > 0; i--) {
              digitBuffer[i] = hexDigits[(int)(val%10)];
            val/=10;
          }
          if((value & 0x80000000) != 0) {
              digitBuffer[i] = '-';
          }
          for(; i<11; i++) {
              PcBootSerialPort.putChar(digitBuffer[i]);
          }
      }
}

  @java.lang.Override
  public void sysConsoleWriteLong(long value, int hexToo) {
      if(hexToo > 0) {
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
              PcBootSerialPort.putChar(hexDigits[(int)value & mask]);
              return;
          }

          // Encode into buffer
//          char[] buffer = new char[size];
          int i = size + 2;   // +2 for '0x' lead
          do
            {
              digitBuffer[--i] = hexDigits[(int) value & mask];
              value >>>= 4;
            }
          while (value != 0);
          digitBuffer[0] = '0';
          digitBuffer[1] = 'x';
          
          for(i=0; i<size+2; i++) {
              PcBootSerialPort.putChar(digitBuffer[i]);
          }
      } else {
          int i;
          long val=value;
          for(i=0; i<21; i++) {
              digitBuffer[i] = ' ';
          }
          digitBuffer[20] = '0';
          if(val < 0)
          {
            val = -val;
          }
          for(i=20; val > 0; i--) {
              digitBuffer[i] = hexDigits[(int)(val%10)];
            val/=10;
          }
          if((value & 0x8000000000000000L) != 0) {
              digitBuffer[i] = '-';
          }
          for(; i<21; i++) {
              PcBootSerialPort.putChar(digitBuffer[i]);
          }
      }
  }

  @java.lang.Override
  public void sysConsoleWriteDouble(double value, int postDecimalDigits) {
  }

  @java.lang.Override
  public void sysConsoleFlushErrorAndTrace() {
  }

  @java.lang.Override
  public void sysExit(int value) {
      VM.sysWriteln("EXIT ", value);
  }

  @java.lang.Override
  public int sysArg(int argno, byte[] buf, int buflen) {
      VM.sysWriteln("ARG ", argno);
    return 0;
  }

  @java.lang.Override
  public int sysGetenv(byte[] varName, byte[] buf, int limit) {
      VM.sysWriteln("GETENV ", varName[0]);
    return 0;
  }

  @java.lang.Override
  public void sysCopy(org.vmmagic.unboxed.Address dst, org.vmmagic.unboxed.Address src, org.vmmagic.unboxed.Extent cnt) {
      VM.sysWriteln("COPY ", dst);
  }

  @java.lang.Override
  public void sysMemmove(org.vmmagic.unboxed.Address dst, org.vmmagic.unboxed.Address src, org.vmmagic.unboxed.Extent cnt) {
      VM.sysWriteln("MEMMOVE ", dst);
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysMalloc(int length) {
      VM.sysWriteln("MALLOC ", length);
    return null;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysCalloc(int length) {
      VM.sysWriteln("CALLOC ", length);
    return null;
  }

  @java.lang.Override
  public void sysFree(org.vmmagic.unboxed.Address location) {
      VM.sysWriteln("FREE ", location);
  }

  @java.lang.Override
  public void sysZeroNT(org.vmmagic.unboxed.Address dst, org.vmmagic.unboxed.Extent cnt) {
      VM.sysWriteln("ZERONT ", dst);
  }

  @java.lang.Override
  public void sysZero(org.vmmagic.unboxed.Address dst, org.vmmagic.unboxed.Extent cnt) {
      VM.sysWriteln("ZERO ", dst);
  }

  @java.lang.Override
  public void sysZeroPages(org.vmmagic.unboxed.Address dst, int cnt) {
      VM.sysWriteln("ZEROPAGES ", dst);
  }

  @java.lang.Override
  public void sysSyncCache(org.vmmagic.unboxed.Address address, int size) {
      VM.sysWriteln("SYNCACHE ", address);
  }

  @java.lang.Override
  public int sysPerfEventInit(int events) {
    return 0;
  }

  @java.lang.Override
  public int sysPerfEventCreate(int id, byte[] name) {
    return 0;
  }

  @java.lang.Override
  public void sysPerfEventEnable() {
  }

  @java.lang.Override
  public void sysPerfEventDisable() {
  }

  @java.lang.Override
  public int sysPerfEventRead(int id, long[] values) {
    return 0;
  }

  @java.lang.Override
  public int sysReadByte(int fd) {
    return 0;
  }

  @java.lang.Override
  public int sysWriteByte(int fd, int data) {
    return 0;
  }

  @java.lang.Override
  public int sysReadBytes(int fd, org.vmmagic.unboxed.Address buf, int cnt) {
    return 0;
  }

  @java.lang.Override
  public int sysWriteBytes(int fd, org.vmmagic.unboxed.Address buf, int cnt) {
    return 0;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysMMap(org.vmmagic.unboxed.Address start, org.vmmagic.unboxed.Extent length, int protection, int flags, int fd, org.vmmagic.unboxed.Offset offset) {
    return null;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysMMapErrno(org.vmmagic.unboxed.Address start, org.vmmagic.unboxed.Extent length, int protection, int flags, int fd, org.vmmagic.unboxed.Offset offset) {
    return null;
  }

  @java.lang.Override
  public int sysMProtect(org.vmmagic.unboxed.Address start, org.vmmagic.unboxed.Extent length, int prot) {
    return 0;
  }

  @java.lang.Override
  public int sysNumProcessors() {
    return 0;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Word sysThreadCreate(org.vmmagic.unboxed.Address ip, org.vmmagic.unboxed.Address fp, org.vmmagic.unboxed.Address tr, org.vmmagic.unboxed.Address jtoc) {
    return null;
  }

  @java.lang.Override
  public int sysThreadBindSupported() {
    return 0;
  }

  @java.lang.Override
  public void sysThreadBind(int cpuId) {
  }

  @java.lang.Override
  public void sysThreadYield() {
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Word sysGetThreadId() {
    return null;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Word sysGetThreadPriorityHandle() {
    return null;
  }

  @java.lang.Override
  public int sysGetThreadPriority(org.vmmagic.unboxed.Word thread, org.vmmagic.unboxed.Word handle) {
    return 0;
  }

  @java.lang.Override
  public int sysSetThreadPriority(org.vmmagic.unboxed.Word thread, org.vmmagic.unboxed.Word handle, int priority) {
    return 0;
  }

  @java.lang.Override
  public int sysStashVMThread(org.jikesrvm.scheduler.RVMThread vmThread) {
    return 0;
  }

  @java.lang.Override
  public void sysThreadTerminate() {
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Word sysMonitorCreate() {
    return null;
  }

  @java.lang.Override
  public void sysMonitorDestroy(org.vmmagic.unboxed.Word monitor) {
  }

  @java.lang.Override
  public int sysMonitorEnter(org.vmmagic.unboxed.Word monitor) {
      return 0;
  }

  @java.lang.Override
  public int sysMonitorExit(org.vmmagic.unboxed.Word monitor) {
    return 0;
  }

  @java.lang.Override
  public void sysMonitorTimedWaitAbsolute(org.vmmagic.unboxed.Word monitor, long whenWakeupNanos) {
  }

  @java.lang.Override
  public void sysMonitorWait(org.vmmagic.unboxed.Word monitor) {
  }

  @java.lang.Override
  public void sysMonitorBroadcast(org.vmmagic.unboxed.Word monitor) {
  }

  @java.lang.Override
  public long sysLongDivide(long x, long y) {
    return 0;
  }

  @java.lang.Override
  public long sysLongRemainder(long x, long y) {
    return 0;
  }

  @java.lang.Override
  public float sysLongToFloat(long x) {
    return 0;
  }

  @java.lang.Override
  public double sysLongToDouble(long x) {
    return 0.0;
  }

  @java.lang.Override
  public int sysFloatToInt(float x) {
    return 0;
  }

  @java.lang.Override
  public int sysDoubleToInt(double x) {
    return 0;
  }

  @java.lang.Override
  public long sysFloatToLong(float x) {
    return 0;
  }

  @java.lang.Override
  public long sysDoubleToLong(double x) {
    return 0;
  }

  @java.lang.Override
  public double sysDoubleRemainder(double x, double y) {
    return 0.0;
  }

  @java.lang.Override
  public float sysPrimitiveParseFloat(byte[] buf) {
    return 0;
  }

  @java.lang.Override
  public int sysPrimitiveParseInt(byte[] buf) {
    return 0;
  }

  @java.lang.Override
  public long sysPrimitiveParseLong(byte[] buf) {
    return 0;
  }

  @java.lang.Override
  public long sysParseMemorySize(byte[] sizeName, byte[] sizeFlag, byte[] defaultFactor, int roundTo, byte[] argToken, byte[] subArg) {
    return 0;
  }

  @java.lang.Override
  public long sysCurrentTimeMillis() {
    return 0;
  }

  @java.lang.Override
  public long sysNanoTime() {
    return 0;
  }

  @java.lang.Override
  public void sysNanoSleep(long howLongNanos) {
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysDlopen(byte[] libname) {
    return null;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysDlsym(org.vmmagic.unboxed.Address libHandler, byte[] symbolName) {
    return null;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address sysVaCopy(org.vmmagic.unboxed.Address va_list) {
    return null;
  }

  @java.lang.Override
  public void sysVaEnd(org.vmmagic.unboxed.Address va_list) {
  }

  @java.lang.Override
  public boolean sysVaArgJboolean(org.vmmagic.unboxed.Address va_list) {
    return false;
  }

  @java.lang.Override
  public byte sysVaArgJbyte(org.vmmagic.unboxed.Address va_list) {
    return 0;
  }

  @java.lang.Override
  public char sysVaArgJchar(org.vmmagic.unboxed.Address va_list) {
    return 0;
  }

  @java.lang.Override
  public short sysVaArgJshort(org.vmmagic.unboxed.Address va_list) {
    return 0;
  }

  @java.lang.Override
  public int sysVaArgJint(org.vmmagic.unboxed.Address va_list) {
    return 0;
  }

  @java.lang.Override
  public long sysVaArgJlong(org.vmmagic.unboxed.Address va_list) {
    return 0;
  }

  @java.lang.Override
  public float sysVaArgJfloat(org.vmmagic.unboxed.Address va_list) {
    return 0;
  }

  @java.lang.Override
  public double sysVaArgJdouble(org.vmmagic.unboxed.Address va_list) {
    return 0.0;
  }

  @java.lang.Override
  public int sysVaArgJobject(org.vmmagic.unboxed.Address va_list) {
    return 0;
  }

  @java.lang.Override
  public void sysEnableAlignmentChecking() {
  }

  @java.lang.Override
  public void sysDisableAlignmentChecking() {
  }

  @java.lang.Override
  public void sysReportAlignmentChecking() {
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address gcspyDriverAddStream(org.vmmagic.unboxed.Address driver, int id) {
    return null;
  }

  @java.lang.Override
  public void gcspyDriverEndOutput(org.vmmagic.unboxed.Address driver) {
  }

  @java.lang.Override
  public void gcspyDriverInit(org.vmmagic.unboxed.Address driver, int id, org.vmmagic.unboxed.Address serverName, org.vmmagic.unboxed.Address driverName, org.vmmagic.unboxed.Address title, org.vmmagic.unboxed.Address blockInfo, int tileNum, org.vmmagic.unboxed.Address unused, int mainSpace) {
  }

  @java.lang.Override
  public void gcspyDriverInitOutput(org.vmmagic.unboxed.Address driver) {
  }

  @java.lang.Override
  public void gcspyDriverResize(org.vmmagic.unboxed.Address driver, int size) {
  }

  @java.lang.Override
  public void gcspyDriverSetTileNameRange(org.vmmagic.unboxed.Address driver, int i, org.vmmagic.unboxed.Address start, org.vmmagic.unboxed.Address end) {
  }

  @java.lang.Override
  public void gcspyDriverSetTileName(org.vmmagic.unboxed.Address driver, int i, org.vmmagic.unboxed.Address start, long value) {
  }

  @java.lang.Override
  public void gcspyDriverSpaceInfo(org.vmmagic.unboxed.Address driver, org.vmmagic.unboxed.Address info) {
  }

  @java.lang.Override
  public void gcspyDriverStartComm(org.vmmagic.unboxed.Address driver) {
  }

  @java.lang.Override
  public void gcspyDriverStream(org.vmmagic.unboxed.Address driver, int id, int len) {
  }

  @java.lang.Override
  public void gcspyDriverStreamByteValue(org.vmmagic.unboxed.Address driver, byte value) {
  }

  @java.lang.Override
  public void gcspyDriverStreamShortValue(org.vmmagic.unboxed.Address driver, short value) {
  }

  @java.lang.Override
  public void gcspyDriverStreamIntValue(org.vmmagic.unboxed.Address driver, int value) {
  }

  @java.lang.Override
  public void gcspyDriverSummary(org.vmmagic.unboxed.Address driver, int id, int len) {
  }

  @java.lang.Override
  public void gcspyDriverSummaryValue(org.vmmagic.unboxed.Address driver, int value) {
  }

  @java.lang.Override
  public void gcspyIntWriteControl(org.vmmagic.unboxed.Address driver, int id, int tileNum) {
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address gcspyMainServerAddDriver(org.vmmagic.unboxed.Address addr) {
    return null;
  }

  @java.lang.Override
  public void gcspyMainServerAddEvent(org.vmmagic.unboxed.Address server, int event, org.vmmagic.unboxed.Address name) {
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address gcspyMainServerInit(int port, int len, org.vmmagic.unboxed.Address name, int verbose) {
    return null;
  }

  @java.lang.Override
  public int gcspyMainServerIsConnected(org.vmmagic.unboxed.Address server, int event) {
    return 0;
  }

  @java.lang.Override
  public org.vmmagic.unboxed.Address gcspyMainServerOuterLoop() {
    return null;
  }

  @java.lang.Override
  public void gcspyMainServerSafepoint(org.vmmagic.unboxed.Address server, int event) {
  }

  @java.lang.Override
  public void gcspyMainServerSetGeneralInfo(org.vmmagic.unboxed.Address server, org.vmmagic.unboxed.Address info) {
  }

  @java.lang.Override
  public void gcspyMainServerStartCompensationTimer(org.vmmagic.unboxed.Address server) {
  }

  @java.lang.Override
  public void gcspyMainServerStopCompensationTimer(org.vmmagic.unboxed.Address server) {
  }

  @java.lang.Override
  public void gcspyStartserver(org.vmmagic.unboxed.Address server, int wait, org.vmmagic.unboxed.Address serverOuterLoop) {
  }

  @java.lang.Override
  public void gcspyStreamInit(org.vmmagic.unboxed.Address stream, int id, int dataType, org.vmmagic.unboxed.Address name, int minValue, int maxValue, int zeroValue, int defaultValue, org.vmmagic.unboxed.Address pre, org.vmmagic.unboxed.Address post, int presentation, int paintStyle, int maxStreamIndex, int red, int green, int blue) {
  }

  @java.lang.Override
  public void gcspyFormatSize(org.vmmagic.unboxed.Address buffer, int size) {
  }

  @java.lang.Override
  public int gcspySprintf(org.vmmagic.unboxed.Address str, org.vmmagic.unboxed.Address format, org.vmmagic.unboxed.Address value) {
    return 0;
  }

  @java.lang.Override
  public void sysStackAlignmentTest() {
  }

  @java.lang.Override
  public void sysArgumentPassingTest(long firstLong, long secondLong, long thirdLong, long fourthLong, long fifthLong, long sixthLong, long seventhLong, long eightLong, double firstDouble, double secondDouble, double thirdDouble, double fourthDouble, double fifthDouble, double sixthDouble, double seventhDouble, double eightDouble, int firstInt, long ninthLong, byte[] firstByteArray, double ninthDouble, org.vmmagic.unboxed.Address firstAddress) {
  }

  @java.lang.Override
  public void sysArgumentPassingSeveralLongsAndSeveralDoubles(long firstLong, long secondLong, long thirdLong, long fourthLong, long fifthLong, long sixthLong, long seventhLong, long eightLong, double firstDouble, double secondDouble, double thirdDouble, double fourthDouble, double fifthDouble, double sixthDouble, double seventhDouble, double eightDouble) {
  }

  @java.lang.Override
  public void sysArgumentPassingSeveralFloatsAndSeveralInts(float firstFloat, float secondFloat, float thirdFloat, float fourthFloat, float fifthFloat, float sixthFloat, float seventhFloat, float eightFloat, int firstInt, int secondInt, int thirdInt, int fourthInt, int fifthInt, int sixthInt, int seventhInt, int eightInt) {
  }
}