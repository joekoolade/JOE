package java.util.zip;

interface Checksum {
  void update(byte[] buf, int index, int len);
  void reset();
  void reset(long init);
  long getValue();
  Checksum copy();
}
