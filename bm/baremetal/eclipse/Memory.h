#pragma interface

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
};
