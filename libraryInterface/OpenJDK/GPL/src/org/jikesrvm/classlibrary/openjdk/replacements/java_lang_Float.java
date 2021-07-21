package org.jikesrvm.classlibrary.openjdk.replacements;

import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.ReplaceClass;
import org.vmmagic.pragma.ReplaceMember;

@ReplaceClass(className = "java.lang.Float")
public class java_lang_Float
{
  @ReplaceMember
  static int floatToIntBits(float value)
  {
    // Check for NaN and return canonical NaN value
    if (value != value)
      return 0x7fc00000;
    else
      return Magic.floatAsIntBits(value);
  }

  @ReplaceMember
  static int floatToRawIntBits(float value)
  {
    return Magic.floatAsIntBits(value);
  }

}
