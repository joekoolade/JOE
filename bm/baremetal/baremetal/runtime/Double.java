/*
 * Created on Aug 13, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */

package baremetal.runtime;


/**
 * @author joe
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public final class Double {


  /**
   * @author joe
   * 
   * To change the template for this generated type comment go to Window -
   * Preferences - Java - Code Generation - Code and Comments
   */

  private final static int MAX_BIGNUM_WDS=32;

  public final class Bigint {

    Bigint next;
    int k, maxwds, sign, wds;
    long x[];

    Bigint() {
      x=new long[MAX_BIGNUM_WDS];
    }
  }

  /**
   * @author joe
   * 
   * To change the template for this generated type comment go to Window -
   * Preferences - Java - Code Generation - Code and Comments
   */
  private static final class Round9UpException extends Exception {

  }

  /**
   * @author joe
   * 
   * To change the template for this generated type comment go to Window -
   * Preferences - Java - Code Generation - Code and Comments
   */
  private static final class RoundupException extends Exception {

  }

  private static final class RoundOffException extends Exception {

  }

  /**
   * @author joe
   * 
   * To change the template for this generated type comment go to Window -
   * Preferences - Java - Code Generation - Code and Comments
   */
  private static final class RetException extends Exception {

  }

  /**
   * @author joe
   * 
   * To change the template for this generated type comment go to Window -
   * Preferences - Java - Code Generation - Code and Comments
   */
  private static final class Ret1Exception extends Exception {

  }

  /**
   * @author joe
   * 
   * To change the template for this generated type comment go to Window -
   * Preferences - Java - Code Generation - Code and Comments
   */
  private static final class BumpUpException extends Exception {

  }

  /**
   * @author joe
   * 
   * To change the template for this generated type comment go to Window -
   * Preferences - Java - Code Generation - Code and Comments
   */
  private static final class NoDigitException extends Exception {

  }

  /**
   * @author joe
   * 
   * To change the template for this generated type comment go to Window -
   * Preferences - Java - Code Generation - Code and Comments
   */
  private static final class OneDigitException extends Exception {

  }

  /**
   * @author joe
   * 
   * To change the template for this generated type comment go to Window -
   * Preferences - Java - Code Generation - Code and Comments
   */
  private static class FastFailedException extends Exception {
  }

  private final static FastFailedException fastFailed=new FastFailedException();
  private final static OneDigitException oneDigit=new OneDigitException();
  private final static NoDigitException noDigit=new NoDigitException();
  private final static BumpUpException bumpUp=new BumpUpException();
  private final static Ret1Exception ret1=new Ret1Exception();
  private final static RetException ret=new RetException();
  private final static RoundupException roundUp=new RoundupException();
  private final static Round9UpException round9Up=new Round9UpException();
  private final static RoundOffException roundOff=new RoundOffException();
  private static final int signBit=0x80000000;
  private static final int expMask=0x7ff00000;
  private static final int expShift1=20;
  private static final int Exp_shift=20;
  private static final int Exp_msk1=0x100000;
  private static final int Exp_msk11=0x100000;
  private static final int P=53;
  private static final int Bias=1023;
  private static final int Emin=-1022;
  private static final int Exp_1=0x3ff00000;
  private static final int Exp_11=0x3ff00000;
  private static final int Ebits=11;
  private static final int Frac_mask=0xfffff;
  private static final int Frac_mask1=0xfffff;
  private static final int Ten_pmax=22;
  private static final int Bletch=0x10;
  private static final int Bndry_mask=0xfffff;
  private static final int Bndry_mask1=0xfffff;
  private static final int LSB=1;
  private static final int Log2P=1;
  private static final int Tiny0=0;
  private static final int Tiny1=1;
  private static final int Quick_max=14;
  private static final int Int_max=14;

  private static double[] tens=new double[50];
  private static double[] bigtens=new double[50];

  public final static java.lang.String toString(double value, boolean isFloat) throws Exception {
    if (java.lang.Double.isNaN(value))
      return "NaN";

    if (value == java.lang.Double.POSITIVE_INFINITY)
      return "Infinity";

    if (value == java.lang.Double.NEGATIVE_INFINITY)
      return "-Infinity";

    char buffer[], result[]=new char[50];
    boolean[] sign=new boolean[1];
    int[] decpt=new int[1];
    buffer=dtoa(value, 0, 20, decpt, sign, isFloat);

    value=fabs(value);

    char[] s=buffer;
    char[] d=result;
    int di=0, si=0;

    if (sign[0])
      d[di++]='-';

    if (value >= 1e-3 && value < 1e7 || value == 0) {
      if (decpt[0] <= 0)
        d[di++]='0';
      else {
        for (int i=0; i < decpt[0]; i++)
          if (s[si] != 0)
            d[di++]=s[si++];
          else
            d[di++]='0';
      }

      d[di++]='.';

      if (s[si] == 0) {
        d[di++]='0';
        decpt[0]++;
      }

      while (decpt[0]++ < 0)
        d[di++]='0';

      while (s[si] != 0)
        d[di++]=s[si++];

      d[di]=0;

      return new java.lang.String(d, 0, di - 1, true);
    }

    d[di++]=s[si++];
    decpt[0]--;
    d[di++]='.';

    if (s[si] == 0)
      d[di++]='0';

    while (s[si] != 0)
      d[di++]=s[si++];

    d[di++]='E';

    if (decpt[0] < 0) {
      d[di++]='-';
      decpt[0]=-decpt[0];
    }

    {
      char exp[]=new char[4];
      int e=4;

      exp[--e]=0;
      do {
        exp[--e]=(char) ('0' + decpt[0] % 10);
        decpt[0]/=10;
      } while (decpt[0] > 0);

      while (exp[e] != 0)
        d[di++]=exp[e++];
    }

    d[di++]=0;

    return new java.lang.String(d, 0, di - 1, true);
  }

  /**
   * @param value
   * @return
   */
  private static double fabs(double value) {
    // TODO Auto-generated method stub
    return 0;
  }

  private static double strtod(byte[] data) {
    // todo
    return 0;
  }

  public final static double parseDouble(java.lang.String str) {
    int length=str.length();

    while (length > 0 && Character.isWhitespace(str.charAt(length - 1)))
      length--;

    // The String could end with a f/F/d/D which is valid but we don't need.
    if (length > 0) {
      char last=str.charAt(length - 1);
      if (last == 'f' || last == 'F' || last == 'd' || last == 'D')
        length--;
    }

    int start=0;
    while (length > 0 && Character.isWhitespace(str.charAt(start))) {
      start++;
      length--;
    }

    if (length > 0) {
      // Note that UTF can expand 3x.
      //        char *data = (char *) __builtin_alloca (3 * length + 1);
      //        jsize blength = _Jv_GetStringUTFRegion (str, start, length, data);
      //        data[blength] = 0;
      //
      //        struct _Jv_reent reent;
      //        memset (&reent, 0, sizeof reent);
      //
      //        char *endptr;
      double val=strtod(str.getBytes());
      //        if (endptr == data + blength)
      return val;
    }
    throw new NumberFormatException(str);
  }

  /*
   * dtoa for IEEE arithmetic (dmg): convert double to ASCII string.
   * 
   * Inspired by "How to Print Floating-Point Numbers Accurately" by Guy L.
   * Steele, Jr. and Jon L. White [Proc. ACM SIGPLAN '90, pp. 92-101].
   * 
   * Modifications: 1. Rather than iterating, we use a simple numeric
   * overestimate to determine k = floor(log10(d)). We scale relevant quantities
   * using O(log2(k)) rather than O(k) multiplications. 2. For some modes > 2
   * (corresponding to ecvt and fcvt), we don't try to generate digits strictly
   * left to right. Instead, we compute with fewer bits and propagate the carry
   * if necessary when rounding the final digit up. This is often faster. 3.
   * Under the assumption that input will be rounded nearest, mode 0 renders
   * 1e23 as 1e23 rather than 9.999999999999999e22. That is, we allow equality
   * in stopping tests when the round-nearest rule will give the same
   * floating-point value as would satisfaction of the stopping test with strict
   * inequality. 4. We remove common factors of powers of 2 from relevant
   * quantities. 5. When converting floating-point integers less than 1e16, we
   * use floating-point arithmetic rather than resorting to multiple-precision
   * integers. 6. When asked to produce fewer than 15 digits, we first try to
   * get by with floating-point arithmetic; we resort to multiple-precision
   * integer arithmetic only if we cannot guarantee that the floating-point
   * calculation has given the correctly rounded result. For k requested digits
   * and "uniformly" distributed input, the probability is something like
   * 10^(k-15) that we must resort to the long calculation.
   */


public final static char[] dtoa(double _d, int mode, int ndigits, int[] decpt, boolean[] sign, boolean floatType) throws Exception
 {
   /*
    * float_type == 0 for double precision, 1 for float.
    * 
    * Arguments ndigits, decpt, sign are similar to those of ecvt and fcvt;
    * trailing zeros are suppressed from the returned string. If not null, *rve
    * is set to point to the end of the return value. If d is +-Infinity or NaN,
    * then *decpt is set to 9999.
    * 
    * mode: 0 ==> shortest string that yields d when read in and rounded to
    * nearest. 1 ==> like 0, but with Steele & White stopping rule; e.g. with
    * IEEE P754 arithmetic , mode 0 gives 1e23 whereas mode 1 gives
    * 9.999999999999999e22. 2 ==> max(1,ndigits) significant digits. This gives
    * a return value similar to that of ecvt, except that trailing zeros are
    * suppressed. 3 ==> through ndigits past the decimal point. This gives a
    * return value similar to that from fcvt, except that trailing zeros are
    * suppressed, and ndigits can be negative. 4-9 should give the same return
    * values as 2-3, i.e., 4 <= mode <= 9 ==> same return as mode 2 + (mode &
    * 1). These modes are mainly for debugging; often they run slower but
    * sometimes faster than modes 2-3. 4,5,8,9 ==> left-to-right digit
    * generation. 6-9 ==> don't try fast floating-point estimate (if
    * applicable). > 16 ==> Floating-point arg is treated as single precision.
    * 
    * Values of mode other than 0-9 are treated as mode 0.
    * 
    * Sufficient space is allocated to the return value to hold the suppressed
    * trailing zeros.
    */

   int bbits = 0, b2, b5, be = 0, dig, i, ieps, ilim = 0, ilim0, ilim1 = 0, j, j1, k, k0,
     k_check, leftright, m2, m5, s2, s5, spec_case = 0, try_quick;
   int si = 0;
//   union double_union d, d2, eps;
   double d2, eps;
   long L;
// #ifndef Sudden_Underflow
   int denorm;
   long x;
// #endif
//   _Jv_Bigint *b, *b1, *delta, *mlo, *mhi, *S;
   Bigint b, b1, delta, mlo;
Bigint S;
Bigint mhi;
   double ds;
   char[] s = null, s0 = null;
   
   double d = _d;
   if ((word0 (d) & signBit)!=0)
     {
       /* set sign for everything, including 0's and NaNs */
       sign[0] = true;
       d = word0 (d) & ~signBit;  /* clear sign bit */
     }
   else
     sign[0] = false;

// #if defined(IEEE_Arith) + defined(VAX)
// #ifdef IEEE_Arith
   if ((word0 (d) & expMask) == expMask)
// #else
//   if (word0 (d) == 0x8000)
// #endif
     {
       /* Infinity or NaN */
       decpt[0] = 9999;
       s =
// #ifdef IEEE_Arith
  word1 (d)==0 && !((word0 (d) & 0xfffff)!=0) ? "Infinity".toCharArray() :
// #endif
  "NaN".toCharArray();
       return s;
     }
// #endif
// #ifdef IBM
//   d.d += 0; /* normalize */
// #endif
   if (d==0){
       decpt[0] = 1;
       s[si] = '0';
       return s;
   }

   b = d2b (d, be, bbits);
// #ifdef Sudden_Underflow
//   i = (int) (word0 (d) >> Exp_shift1 & (Exp_mask >> Exp_shift1));
// #else
   if ((i = (int) (word0 (d) >> expShift1 & (expMask >> expShift1)))!=0)
     {
// #endif
//       d2.d = d.d;
       d2 = d;
       word0(d2, word0 (d2) & Frac_mask1);
       word0(d2, word0 (d2) | Exp_11);
// #ifdef IBM
//       if (j = 11 - hi0bits (word0 (d2) & Frac_mask))
//  d2.d /= 1 << j;
// #endif

       /*
        * log(x) ~=~ log(1.5) + (x-1.5)/1.5 log10(x) = log(x) / log(10) ~=~
        * log(1.5)/log(10) + (x-1.5)/(1.5*log(10)) log10(d) =
        * (i-Bias)*log(2)/log(10) + log10(d2)
        * 
        * This suggests computing an approximation k to log10(d) by
        * 
        * k = (i - Bias)*0.301029995663981 + ( (d2-1.5)*0.289529654602168 +
        * 0.176091259055681 );
        * 
        * We want k to be too large rather than too small. The error in the
        * first-order Taylor series approximation is in our favor, so we just
        * round up the constant enough to compensate for any error in the
        * multiplication of (i - Bias) by 0.301029995663981; since |i - Bias| <=
        * 1077, and 1077 * 0.30103 * 2^-52 ~=~ 7.2e-14, adding 1e-13 to the
        * constant term more than suffices. Hence we adjust the constant term to
        * 0.1760912590558. (We could get a more accurate k by invoking log10,
        * but this is probably not worthwhile.)
        */

       i -= Bias;
// #ifdef IBM
//       i <<= 2;
//       i += j;
// #endif
// #ifndef Sudden_Underflow
       denorm = 0;
     }
   else
     {
       /* d is denormalized */

       i = bbits + be + (Bias + (P - 1) - 1);
       x = i > 32 ? word0 (d) << (64 - i) | word1 (d) >> (i - 32)
  : word1 (d) << (32 - i);
       d2 = x;
       word0(d2, word0 (d2) - 31 * Exp_msk1); /* adjust exponent */
       i -= (Bias + (P - 1) - 1) + 1;
       denorm = 1;
     }
// #endif
   ds = (d2 - 1.5) * 0.289529654602168 + 0.1760912590558 + i * 0.301029995663981;
   k = (int) ds;
   if (ds < 0. && ds != k)
     k--;     /* want k = floor(ds) */
   k_check = 1;
   if (k >= 0 && k <= Ten_pmax)
     {
       if (d < tens[k])
         k--;
       k_check = 0;
     }
   j = bbits - i - 1;
   if (j >= 0)
     {
       b2 = 0;
       s2 = j;
     }
   else
     {
       b2 = -j;
       s2 = 0;
     }
   if (k >= 0)
     {
       b5 = 0;
       s5 = k;
       s2 += k;
     }
   else
     {
       b2 -= k;
       b5 = -k;
       s5 = 0;
     }
   if (mode < 0 || mode > 9)
     mode = 0;
   try_quick = 1;
   if (mode > 5)
     {
       mode -= 4;
       try_quick = 0;
     }
   leftright = 1;
   switch (mode) {
     case 0:
     case 1:
       ilim = ilim1 = -1;
       i = 18;
       ndigits = 0;
       break;
     case 2:
       leftright = 0;
       /* no break */
     case 4:
       if (ndigits <= 0)
         ndigits = 1;
       ilim = ilim1 = i = ndigits;
       break;
     case 3:
       leftright = 0;
       /* no break */
     case 5:
       i = ndigits + k + 1;
       ilim = i;
       ilim1 = i - 1;
       if (i <= 0)
         i = 1;
   }
   j = 8; // sizeof (unsigned long);
//   for (ptr->_result_k = 0; (int) (sizeof (_Jv_Bigint) - sizeof (unsigned long))
// + j <= i;
//        j <<= 1)
//     ptr->_result_k++;
//   ptr->_result = Balloc (ptr, ptr->_result_k);
//   s = s0 = (char *) ptr->_result;

     if (ilim >= 0 && ilim <= Quick_max && try_quick!=0)
     {
       /* Try to get by with floating-point arithmetic. */

       i = 0;
       d2 = d;
       k0 = k;
       ilim0 = ilim;
       ieps = 2;      /* conservative */
       if (k > 0)
       {
         ds = tens[k & 0xf];
         j = k >> 4;
         if ((j & Bletch)!=0)
         {
           /* prevent overflows */
           j &= Bletch - 1;
           int n_bigtens = 0;
		d /= bigtens[n_bigtens - 1];
           ieps++;
         }
         for (; j>0; j >>= 1, i++)
           if ((j & 1)!=0)
           {
             ieps++;
             ds *= bigtens[i];
           }
         d /= ds;
       }
       else if ((j1 = -k)!=0)
       {
         d *= tens[j1 & 0xf];
         for (j = j1 >> 4; j>0; j >>= 1, i++)
           if ((j & 1)>0)
           {
             ieps++;
             d *= bigtens[i];
           }
       }
         if (k_check!=0 && d < 1. && ilim > 0)
         {
           if (ilim1 <= 0)
             throw fastFailed;
//      goto fast_failed;
           ilim = ilim1;
           k--;
           d *= 10.;
           ieps++; 
         }
       eps = ieps * d + 7.;
       word0(eps, word0 (eps) - (P - 1) * Exp_msk1);
//       try { // nodigits, onedigits
       try { //fastfailed
         if (ilim == 0)
         {
           S = mhi = null;
           d -= 5.;
           if (d > eps)
      // goto one_digit;
             throw oneDigit;
           Exception noDigits = null;
		if (d < -eps)
             throw noDigits;
      // goto no_digits;
           throw fastFailed;
         }
// #ifndef No_leftright
//         try { // bumpup
           if (leftright>0)
           {
             /*
              * Use Steele & White method of only generating digits needed.
              */
             eps = 0.5 / tens[ilim - 1] - eps;
             for (i = 0;;)
             {
               L = (long) d;
               d -= L;
               s[si++] = (char) ('0' + (int) L);
               if (d < eps)
                 // goto ret1;
                 throw ret1;
               if (1. - d < eps)
                 // goto bump_up;
                 throw bumpUp;
               if (++i >= ilim)
                 break;
               eps *= 10.;
               d *= 10.;
             }
           }
           else
           {
             // #endif
             /* Generate ilim digits, then fix them up. */
             eps *= tens[ilim - 1];
             for (i = 1;; i++, d *= 10.)
             {
               L = (long) d;
               d -= L;
               s[si++] = (char) ('0' + (int) L);
               if (i == ilim)
               {
                 if (d > 0.5 + eps)
                   // goto bump_up;
                   throw bumpUp;
                 else if (d < 0.5 - eps)
                 {
                   while (s[--si] == '0');
                   si++;
                   // goto ret1;
                   throw ret1;
                 }
                 break;
               }
             }
// #ifndef No_leftright
           }
// #endif
       } catch (FastFailedException e) {  
//     fast_failed:
       s = s0;
       d = d2;
       k = k0;
       ilim = ilim0;
       }

   /* Do we have a "small" integer? */

   if (be >= 0 && k <= Int_max)
     {
       /* Yes. */
       ds = tens[k];
       if (ndigits < 0 && ilim <= 0)
  {
    S = mhi = null;
    if (ilim < 0 || d <= 5 * ds)
      // goto no_digits;
      throw noDigit;
    // goto one_digit;
    throw oneDigit;
  }
       for (i = 1;; i++)
  {
    L = (long) (d / ds);
    d -= L * ds;
// #ifdef Check_FLT_ROUNDS
    /* If FLT_ROUNDS == 2, L will usually be high by 1 */
    if (d < 0)
      {
        L--;
        d += ds;
      }
// #endif
    s[si++] = (char) ('0' + (int) L);
    if (i == ilim)
      {
        d += d;
        if (d > ds || (d == ds && (L & 1)>0))
    {
//    bump_up:
      while (s[--si] == '9')
        if (s == s0)
          {
      k++;
      s[si] = '0';
      break;
          }
      ++s[si++];
    }
        break;
      }
    if ((d *= 10.)==0)
      break;
  }
       // goto ret1;
       throw ret1;
     }
//catch(BumpUpException e) {
//  while (s[--si] == '9')
//    if (s == s0) {
//      k++;
//      s[si] = '0';
//      break;
//    }
//  ++s[si++];
//  throw ret;
//}
  
   m2 = b2;
   m5 = b5;
   mhi = mlo = null;
   if (leftright>0)
     {
       if (mode < 2)
  {
    i =
// #ifndef Sudden_Underflow
      denorm!=0 ? be + (Bias + (P - 1) - 1 + 1) :
// #endif
// #ifdef IBM
//      1 + 4 * P - 3 - bbits + ((bbits + be - 1) & 3);
// #else
      1 + P - bbits;
// #endif
  }
       else
  {
    j = ilim - 1;
    if (m5 >= j)
      m5 -= j;
    else
      {
        s5 += j -= m5;
        b5 += j;
        m5 = 0;
      }
    if ((i = ilim) < 0)
      {
        m2 -= i;
        i = 0;
      }
  }
       b2 += i;
       s2 += i;
       mhi = i2b (1);
     }
   if (m2 > 0 && s2 > 0)
     {
       i = m2 < s2 ? m2 : s2;
       b2 -= i;
       m2 -= i;
       s2 -= i;
     }
   if (b5 > 0)
     {
       if (leftright>0)
  {
    if (m5 > 0)
      {
        mhi = pow5mult (mhi, m5);
        b1 = mult (mhi, b);
        Bfree (b);
        b = b1;
      }
    if ((j = b5 - m5)>0)
      b = pow5mult (b, j);
  }
       else
  b = pow5mult (b, b5);
     }
   S = i2b (1);
   if (s5 > 0)
     S = pow5mult (S, s5);

   /* Check for special case that d is a normalized power of 2. */

   if (mode < 2)
     {
       if (word1 (d)==0 && (word0 (d) & Bndry_mask)==0
// #ifndef Sudden_Underflow
    && (word0(d) & expMask)!=0
// #endif
  )
  {
    /* The special case */
    b2 += Log2P;
    s2 += Log2P;
    spec_case = 1;
  }
       else
  spec_case = 0;
     }

   /*
    * Arrange for convenient computation of quotients: shift left if necessary
    * so divisor has 4 leading 0 bits.
    * 
    * Perhaps we should just compute leading 28 bits of S once and for all and
    * pass them and a shift to quorem, so it can do shifts and ors to compute
    * the numerator for q.
    */

// #ifdef Pack_32
   if ((i = ((s5>0 ? 32 - hi0bits (S.x[S.wds - 1]) : 1) + s2) & 0x1f)>0)
     i = 32 - i;
// #else
//   if ((i = ((s5 ? 32 - hi0bits (S->_x[S->_wds - 1]) : 1) + s2) & 0xf))
//     i = 16 - i;
// #endif
   if (i > 4)
     {
       i -= 4;
       b2 += i;
       m2 += i;
       s2 += i;
     }
   else if (i < 4)
     {
       i += 28;
       b2 += i;
       m2 += i;
       s2 += i;
     }
   if (b2 > 0)
     b = lshift (b, b2);
   if (s2 > 0)
     S = lshift (S, s2);
   if (k_check!=0)
     {
       if (cmp (b, S) < 0)
  {
    k--;
    b = multadd (b, 10, 0);  /* we botched the k estimate */
    if (leftright>0)
      mhi = multadd (mhi, 10, 0);
    ilim = ilim1;
  }
     }
   if (ilim <= 0 && mode > 2)
     {
       if (ilim < 0 || cmp (b, S = multadd (S, 5, 0)) <= 0)
  {
    /* no digits, fcvt style */
//  no_digits:
    k = -1 - ndigits;
    // goto ret;
       throw ret;
  }
//     one_digit:
       s[si++] = '1';
       k++;
       // goto ret;
       throw ret;
     }
Object ptr = null;
// } catch(NoDigitException e) {
//  k = -1 - ndigits;
//  throw ret;
//} catch(OneDigitException e1) {
//  s[si++] = '1';
//  k++;
//  throw ret;
//  
//}
   if (leftright>0)
     {
       if (m2 > 0)
  mhi = lshift (mhi, m2);

       /* Single precision case, */
       if (floatType)
  mhi = lshift (mhi, 29);

       /*
        * Compute mlo -- check for special case that d is a normalized power of
        * 2.
        */

       mlo = mhi;
       if (spec_case>0)
  {
    mhi = Balloc (mhi.k);
    Bcopy (mhi, mlo);
    mhi = lshift (mhi, Log2P);
  }

       for (i = 1;; i++)
  {
    dig = quorem (b, S) + '0';
    /*
     * Do we yet have the shortest decimal string that will round to d?
     */
    j = cmp (b, mlo);
    delta = diff (ptr, S, mhi);
    j1 = delta.sign>0 ? 1 : cmp (b, delta);
    Bfree (ptr, delta);
// #ifndef ROUND_BIASED
    if (j1 == 0 && mode==0 && (word1 (d) & 1)==0)
      {
        if (dig == '9')
    // goto round_9_up;
          throw round9Up;
        if (j > 0)
    dig++;
        s[si++] = (char) dig;
        // goto ret;
        throw ret;
      }
// #endif
    if (j < 0 || (j == 0 && mode==0
// #ifndef ROUND_BIASED
        && (word1 (d) & 1)==0
// #endif
      ))
      {
        if (j1 > 0)
    {
      b = lshift (ptr, b, 1);
      j1 = cmp (b, S);
      if ((j1 > 0 || (j1 == 0 && (dig & 1)>0))
          && dig++ == '9')
        // goto round_9_up;
        throw round9Up;
    }
        s[si++] = (char) dig;
        //goto ret;
        throw ret;
      }
    if (j1 > 0)
      {
        if (dig == '9')
    {   /* possible if i == 1 */
    round_9_up:
      s[si++] = '9';
      // goto roundoff;
        throw roundOff;
    }
        s[si++] = (char) (dig + 1);
        // goto ret;
        throw ret;
      }
    s[si++] = (char) dig;
    if (i == ilim)
      break;
    b = multadd (ptr, b, 10, 0);
    if (mlo == mhi)
      mlo = mhi = multadd (ptr, mhi, 10, 0);
    else
      {
        mlo = multadd (ptr, mlo, 10, 0);
        mhi = multadd (ptr, mhi, 10, 0);
      }
  }
     }
   else
     for (i = 1;; i++)
       {
       s[si++] = (char) (dig = quorem (b, S) + '0');
  if (i >= ilim)
    break;
  b = multadd (ptr, b, 10, 0);
       }

   /* Round off last digit */

   b = lshift (ptr, b, 1);
   j = cmp (b, S);
   if (j > 0 || (j == 0 && (dig & 1)>0))
     {
     roundoff:
       while (s[--si] == '9')
  if (s == s0)
    {
      k++;
      s[si++] = '1';
      // goto ret;
      throw ret;
    }
       ++s[si++];
     }
   else
     {
       while (s[--si] == '0');
       si++;
     }
// ret:
   Bfree (ptr, S);
   if (mhi!=null)
     {
       if (mlo!=null && mlo != mhi)
  Bfree (ptr, mlo);
       Bfree (ptr, mhi);
     }
// ret1:
   Bfree (ptr, b);
     s[si] = 0;
   decpt[0] = k + 1;
   return s0;
     }
	return s0;
 }
     
     /**
 * @param ptr
 * @param mhi
 * @param i
 * @param j
 * @return
 */
private static Bigint multadd(Object ptr, Bigint mhi, int i, int j) {
	// TODO Auto-generated method stub
	return null;
}

	/**
 * @param b
 * @param i
 * @param j
 * @return
 */
private static Bigint multadd(Bigint b, int i, int j) {
	// TODO Auto-generated method stub
	return null;
}

	/**
 * @param b
 * @param b2
 * @return
 */
private static Bigint lshift(Bigint b, int b2) {
	// TODO Auto-generated method stub
	return null;
}

	/**
 * @param ptr
 * @param delta
 */
private static void Bfree(Object ptr, Bigint delta) {
	// TODO Auto-generated method stub
	
}

	/**
 * @param ptr
 * @param s
 * @param mhi
 * @return
 */
private static Bigint diff(Object ptr, Bigint s, Bigint mhi) {
	// TODO Auto-generated method stub
	return null;
}

	/**
 * @param b
 * @param s
 * @return
 */
private static char quorem(Bigint b, Bigint s) {
	// TODO Auto-generated method stub
	return 0;
}

	/**
 * @param mhi
 * @param mlo
 */
private static void Bcopy(Bigint mhi, Bigint mlo) {
	// TODO Auto-generated method stub
	
}

	/**
 * @param k
 * @return
 */
private static Bigint Balloc(int k) {
	// TODO Auto-generated method stub
	return null;
}

	/**
 * @param b
 * @param s
 * @return
 */
private static int cmp(Bigint b, Bigint s) {
	// TODO Auto-generated method stub
	return 0;
}

	/**
 * @param ptr 
	 * @param b
 * @param b2
 * @return
 */
private static Bigint lshift(Object ptr, Bigint b, int b2) {
	// TODO Auto-generated method stub
	return null;
}

	/**
 * @param l
 * @return
 */
private static int hi0bits(long l) {
	// TODO Auto-generated method stub
	return 0;
}

	/**
 * @param b
 */
private static void Bfree(Bigint b) {
	// TODO Auto-generated method stub
	
}

	/**
 * @param mhi
 * @param b
 * @return
 */
private static Bigint mult(Bigint mhi, Bigint b) {
	// TODO Auto-generated method stub
	return null;
}

	/**
 * @param mhi
 * @param m5
 * @return
 */
private static Bigint pow5mult(Bigint mhi, int m5) {
	// TODO Auto-generated method stub
	return null;
}

	/**
 * @param i
 * @return
 */
private static Bigint i2b(int i) {
	// TODO Auto-generated method stub
	return null;
}

	/**
 * @param d
 * @param be
 * @param bbits
 * @return
 */
private static Bigint d2b(double d, int be, int bbits) {
	// TODO Auto-generated method stub
	return null;
}

	private final static int word0(double d) {
    long l=(long) d;
    return (int) (l >> 32);
  }

  private final static void word0(double d, int val) {

  }

  private final static int word1(double d) {
    long l=(long) d;
    return (int) (l & 0xffffffff);
  }

  private final static void word1(double d, int val) {

  }
}