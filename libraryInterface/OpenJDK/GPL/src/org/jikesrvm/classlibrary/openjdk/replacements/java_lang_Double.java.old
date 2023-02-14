package org.jikesrvm.classlibrary.openjdk.replacements;

import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.ReplaceClass;
import org.vmmagic.pragma.ReplaceMember;

@ReplaceClass(className = "java.lang.Double")
public class java_lang_Double
{
  @ReplaceMember
  static long doubleToLongBits(double value)
  {
    // Check for NaN and return canonical NaN value
    if (value != value)
      return 0x7ff8000000000000L;
    else
      return Magic.doubleAsLongBits(value);
  }

  @ReplaceMember
  static long doubleToRawLongBits(double value)
  {
    return Magic.doubleAsLongBits(value);
  }

  @ReplaceMember
  static double longBitsToDouble(long bits)
  {
    return Magic.longBitsAsDouble(bits);
  }

}
