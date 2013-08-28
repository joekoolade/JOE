#include <gcj/cni.h>

class baremetal::kernel::Memory : public java::lang::Object
{
public:
  static jint readWord(jint);
  static jint readByte(jint);
  static jint readHalfWord(jint);
  static jlong readDoubleWord(jint);
  static void writeWord(jint, jint);
  static void writeByte(jint, jint);
  static void writeHalfWord(jint, jint);
  static void writeDoubleWord(jint, jlong);
  static jint getAddress(jobject);
  static void bcopy(jint, jint, jint);
  static void memset(jint, jint, jint);
  static jint ioRead8(jint);
  static jint ioRead16(jint);
  static jint ioRead32(jint);
  static void ioWrite8(jint,jint);
  static void ioWrite16(jint,jint);
  static void ioWrite32(jint,jint);
};

#pragma implementation

jint
baremetal::kernel::Memory::ioRead8(jint addr) {
  char data;
  short addr0 = (short)addr;
  asm("inb %[addr],%[data]" : [data] "=a" (data) : [addr] "d" (addr0));
  return data;
}

jint
baremetal::kernel::Memory::ioRead16(jint addr) {
  short data;
  short addr0 = (short)addr;
  asm("inw %[addr],%[data]" : [data] "=a" (data) : [addr] "d" (addr0));
  return data;
}

jint
baremetal::kernel::Memory::ioRead32(jint addr) {
  jint data;
  short addr0 = (short)addr;
  asm("inl %[addr],%[data]" : [data] "=a" (data) : [addr] "d" (addr0));
  return data;
}

void
baremetal::kernel::Memory::ioWrite8(jint addr, jint data) {
  short addr0 = (short)addr;
  char data0 = (char)data;
  asm("outb %[data], %[addr]" :: [data] "a" (data0), [addr] "d" (addr0));
}

void
baremetal::kernel::Memory::ioWrite16(jint addr, jint data) {
  short addr0 = (short)addr;
  short data0 = (short)data;
  asm("outw %[data], %[addr]" :: [data] "a" (data0), [addr] "d" (addr0));
}

void
baremetal::kernel::Memory::ioWrite32(jint addr, jint data) {
  short addr0 = (short)addr;
  asm("outl %[data], %[addr]" :: [data] "a" (data), [addr] "d" (addr0));
}


void
baremetal::kernel::Memory::bcopy(jint src, jint dest, jint size) {
  ::memcpy((void*)dest, (void*)src, size);
}

void
baremetal::kernel::Memory::memset(jint dest, jint value, jint size) {
  ::memset((void*)dest, value, size);
}

jint
baremetal::kernel::Memory::getAddress(jobject o) {
  return (jint)o;
}

jint
baremetal::kernel::Memory::readWord(jint address){
  return *(jint*)address;
}

jint
baremetal::kernel::Memory::readByte(jint address) {
  return *(jbyte*)address;
}

jint
baremetal::kernel::Memory::readHalfWord(jint address) {
  return *(jshort*)address;
}

jlong
baremetal::kernel::Memory::readDoubleWord(jint address) {
  return *(jlong*)address;
}

void
baremetal::kernel::Memory::writeWord(jint address, jint value) {
  *(jint*)address = value;
}

void
baremetal::kernel::Memory::writeByte(jint address, jint value) {
  *(jbyte*)address = value;
}

void
baremetal::kernel::Memory::writeHalfWord(jint address, jint value) {
  *(jshort*)address = value;
}

void
baremetal::kernel::Memory::writeDoubleWord(jint address, jlong value) {
  *(jlong*)address = value;
}

