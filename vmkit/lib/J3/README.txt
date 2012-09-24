GNU Classpath
Current version 0.97.2. Use ecj to compile Classpath.

  a - unzip classpath
      tar -xzf classpath-0.97.2
  b - compile classpath
      cd classpath-0.97.2
      ./configure --disable-plugin --disable-examples && make

IMPORTANT: for local use
cd classpath-x.y/lib;
ln -s ../native/jni/gtk-peer/.libs/libgtkpeer.so;
ln -s ../native/jni/gconf-peer/.libs/libgconfpeer.so;
ln -s ../native/jni/java-io/.libs/libjavaio.so;
ln -s ../native/jni/java-lang/.libs/libjavalangreflect.so;
ln -s ../native/jni/java-lang/.libs/libjavalang.so;
ln -s ../native/jni/java-net/.libs/libjavanet.so;
ln -s ../native/jni/java-nio/.libs/libjavanio.so;
ln -s ../native/jni/java-util/.libs/libjavautil.so;

Or for Darwin
ln -s ../native/jni/gtk-peer/.libs/libgtkpeer.dylib;
ln -s ../native/jni/gconf-peer/.libs/libgconfpeer.dylib;
ln -s ../native/jni/java-io/.libs/libjavaio.dylib;
ln -s ../native/jni/java-lang/.libs/libjavalangreflect.dylib;
ln -s ../native/jni/java-lang/.libs/libjavalang.dylib;
ln -s ../native/jni/java-net/.libs/libjavanet.dylib;
ln -s ../native/jni/java-nio/.libs/libjavanio.dylib;
ln -s ../native/jni/java-util/.libs/libjavautil.dylib;
