/******************************************************************************
  Copyright (c) 2014 Ryan Juckett
  http://www.ryanjuckett.com/
 
  This software is provided 'as-is', without any express or implied
  warranty. In no event will the authors be held liable for any damages
  arising from the use of this software.
 
  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:
 
  1. The origin of this software must not be misrepresented; you must not
     claim that you wrote the original software. If you use this software
     in a product, an acknowledgment in the product documentation would be
     appreciated but is not required.
 
  2. Altered source versions must be plainly marked as such, and must not be
     misrepresented as being the original software.
 
  3. This notice may not be removed or altered from any source
     distribution.
******************************************************************************/
/**
 * Created on Jan 28, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.utilities;

import java.math.BigInteger;

import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;

/**
 * @author Joe Kulig
 *
 */
//******************************************************************************
//This is an implementation the Dragon4 algorithm to convert a binary number
//in floating point format to a decimal number in string format. The function
//returns the number of digits written to the output buffer and the output is
//not NUL terminated.
//
//The floating point input value is (mantissa * 2^exponent).
//
//See the following papers for more information on the algorithm:
//"How to Print Floating-Point Numbers Accurately"
// Steele and White
// http://kurtstephens.com/files/p372-steele.pdf
//"Printing Floating-Point Numbers Quickly and Accurately"
// Burger and Dybvig
// http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.72.4656&rep=rep1&type=pdf
//******************************************************************************
public class Dragon4 {
  private static BigInteger two;
  private static int precision= -1;

  private final static int getExponent(int v)
  {
    return ((v >> 23) & 0xff);
  }
  
  private final static int getMantissa(int v)
  {
    return v & 0x7FFFFF;
  }
  
  private final static boolean isNegative(int v)
  {
    return (v >> 31) != 0;
  }
  
  private final static long getMantissa(long v)
  {
    return v & 0xFFFFFFFFFFFFFL;
  }
  
  private final static int getExponent(long v)
  {
    return (int) ((v >> 52) & 0x7FF);
  }
  
  private final static boolean isNegative(long v)
  {
    return (v >> 63) != 0;
  }
  
  private static final int BUFFER_SIZE = 64;
  
  public final static String floatToString(float value)
  {
    char outBuffer[] = new char[BUFFER_SIZE];
    int currentDigit = 0;
    int bufferSize = BUFFER_SIZE;
    
    int hexFloat = Magic.floatAsIntBits(value);
    int floatExponent = getExponent(hexFloat);
    int floatMantissa = getMantissa(hexFloat);
    int prefixLength = 0;
    int mantissa;
    int exponent;
    int mantissaHighBitIdx;
    boolean hasUnequalMargins;
    CutOffMode cutoffMode = CutOffMode.Unique;
    int cutoffNumber = 0;
    int outExponent[] = new int[2];
    
    if(isNegative(hexFloat))
    {
      outBuffer[currentDigit] = '-';
      currentDigit++;
      bufferSize--;
      prefixLength++;
    }
    
    if(floatExponent == 0xFF)
    {
      return stringInfNan(outBuffer, currentDigit, floatMantissa);
    }
    else
    {
      if(floatExponent != 0)
      {
        // normalized
        // The floating point equation is:
        //  value = (1 + mantissa/2^23) * 2 ^ (exponent-127)
        // We convert the integer equation by factoring a 2^23 out of the exponent
        //  value = (1 + mantissa/2^23) * 2^23 * 2 ^ (exponent-127-23)
        //  value = (2^23 + mantissa) * 2 ^ (exponent-127-23)
        // Because of the implied 1 in front of the mantissa we have 24 bits of precision.
        //   m = (2^23 + mantissa)
        //   e = (exponent-127-23)
        mantissa            = (1 << 23) | floatMantissa;
        exponent            = floatExponent - 127 - 23;
        mantissaHighBitIdx  = 23;
        hasUnequalMargins   = (floatExponent != 1) && (floatMantissa == 0);
      }
      else
      {
          // denormalized
          // The floating point equation is:
          //  value = (mantissa/2^23) * 2 ^ (1-127)
          // We convert the integer equation by factoring a 2^23 out of the exponent
          //  value = (mantissa/2^23) * 2^23 * 2 ^ (1-127-23)
          //  value = mantissa * 2 ^ (1-127-23)
          // We have up to 23 bits of precision.
          //   m = (mantissa)
          //   e = (1-127-23)
          mantissa           = floatMantissa;
          exponent           = 1 - 127 - 23;
          mantissaHighBitIdx = logBase2(mantissa);
          hasUnequalMargins   = false;
      }
    }
    if(precision < 0)
    {
      convertToString(mantissa, exponent, mantissaHighBitIdx, hasUnequalMargins, cutoffMode, cutoffNumber, outBuffer, outExponent);
    }
    else
    {
      convertToString(mantissa, exponent, mantissaHighBitIdx, hasUnequalMargins, cutoffMode, precision, outBuffer, outExponent);
    }
    
    int len;
    if(outExponent[1] > 12)
    {
      len = printScientific(outBuffer, outExponent[1], outExponent[0]);
    }
    else
    {
      len = printPositional(outBuffer, outExponent[1], outExponent[0]);
    }
    
    return new String(outBuffer, 0, len);
  }
  public final static String doubleToString(double value)
  {
    String doubleString;
    int bufferSize = BUFFER_SIZE;
    char outBuffer[] = new char[BUFFER_SIZE];
    int currentDigit = 0;
    
    long hexFloat = Magic.doubleAsLongBits(value);
    int floatExponent = getExponent(hexFloat);
    long floatMantissa = getMantissa(hexFloat);
    int prefixLength = 0;
    long mantissa;
    int exponent;
    int mantissaHighBitIdx;
    boolean hasUnequalMargins;
    CutOffMode cutoffMode = CutOffMode.Unique;
    int cutoffNumber = 0;
    int outExponent[] = new int[2];
    
    // output the sign
    if(isNegative(hexFloat))
    {
      outBuffer[currentDigit] = '-';
      currentDigit++;
      bufferSize--;
      prefixLength++;
    }
    
    if(floatExponent == 0x7FF)
    {
      return stringInfNan(outBuffer, currentDigit, floatMantissa);
    }
    else
    {
      
      if(floatExponent != 0)
      {
        // normal
        // The floating point equation is:
        //  value = (1 + mantissa/2^52) * 2 ^ (exponent-1023)
        // We convert the integer equation by factoring a 2^52 out of the exponent
        //  value = (1 + mantissa/2^52) * 2^52 * 2 ^ (exponent-1023-52)
        //  value = (2^52 + mantissa) * 2 ^ (exponent-1023-52)
        // Because of the implied 1 in front of the mantissa we have 53 bits of precision.
        //   m = (2^52 + mantissa)
        //   e = (exponent-1023+1-53)
        mantissa            = (1L << 52) | floatMantissa;
        exponent            = floatExponent - 1023 - 52;
        mantissaHighBitIdx  = 52;
        hasUnequalMargins   = (floatExponent != 1) && (floatMantissa == 0);
      }
      else
      {
        // subnormal
        // The floating point equation is:
        //  value = (mantissa/2^52) * 2 ^ (1-1023)
        // We convert the integer equation by factoring a 2^52 out of the exponent
        //  value = (mantissa/2^52) * 2^52 * 2 ^ (1-1023-52)
        //  value = mantissa * 2 ^ (1-1023-52)
        // We have up to 52 bits of precision.
        //   m = (mantissa)
        //   e = (1-1023-52)
        mantissa            = floatMantissa;
        exponent            = 1 - 1023 - 52;
        mantissaHighBitIdx  = logBase2((int)mantissa);
        hasUnequalMargins   = false;
      }
    }
    
    if(precision < 0)
    {
      convertToString(mantissa, exponent, mantissaHighBitIdx, hasUnequalMargins, cutoffMode, cutoffNumber, outBuffer, outExponent);
    }
    else
    {
      convertToString(mantissa, exponent, mantissaHighBitIdx, hasUnequalMargins, cutoffMode, precision, outBuffer, outExponent);
    }
    
    int len;
    if(outExponent[1] > 12)
    {
      len = printScientific(outBuffer, outExponent[1], outExponent[0]);
    }
    else
    {
      len = printPositional(outBuffer, outExponent[1], outExponent[0]);
    }
    
    return new String(outBuffer, 0, len);
  }
  /**
   * @param outBuffer
   * @param i
   * @param j
   */
  private static int printPositional(char[] outBuffer, int numPrintDigits, int printExponent)
  {
    int numFractionDigits = 0;    // track number of digits past the decimal that have been printed
    int maxPrintLen = outBuffer.length-1;
    
    // Output has a whole number
    if(printExponent >= 0)
    {
      /*
       * Leave the whole number at the start of the buffer
       */
      int numWholeDigits = printExponent+1;
      if(numPrintDigits < numWholeDigits)
      {
        // Check for an overflow
        if(numWholeDigits > maxPrintLen)
        {
          numWholeDigits = maxPrintLen;
        }
        
        // Add trailing zeros 
        for ( ; numPrintDigits < numWholeDigits; ++numPrintDigits )
        {
          outBuffer[numPrintDigits] = '0';
        }
      }
      // insert the decimal point prior to the fraction
      else if (numPrintDigits > numWholeDigits)
      {
          numFractionDigits = numPrintDigits - numWholeDigits;
          int maxFractionDigits = maxPrintLen - numWholeDigits - 1;
          if (numFractionDigits > maxFractionDigits)
          {
              numFractionDigits = maxFractionDigits;
          }

          //memmove(pOutBuffer + numWholeDigits + 1, pOutBuffer + numWholeDigits, numFractionDigits);
          for(int digit=numFractionDigits-1; digit >= 0; digit--)
          {
            outBuffer[numWholeDigits+1+digit] = outBuffer[numWholeDigits+digit];
          }
          outBuffer[numWholeDigits] = '.';
          numPrintDigits = numWholeDigits + 1 + numFractionDigits;
      }
    }
    else
    {
        // shift out the fraction to make room for the leading zeros
        if (maxPrintLen > 2)
        {
            int numFractionZeros = -printExponent - 1;
            int maxFractionZeros = maxPrintLen - 2;
            if (numFractionZeros > maxFractionZeros)
            {
                numFractionZeros = maxFractionZeros;
            }
 
            int digitsStartIdx = 2 + numFractionZeros;
                     
            // shift the significant digits right such that there is room for leading zeros
            numFractionDigits = numPrintDigits;
            int maxFractionDigits = maxPrintLen - digitsStartIdx;
            if (numFractionDigits > maxFractionDigits)
            {
                numFractionDigits = maxFractionDigits;
            }
            
            //memmove(pOutBuffer + digitsStartIdx, pOutBuffer, numFractionDigits);
            for(int digit=numFractionDigits-1; digit >=0; digit--)
            {
              outBuffer[digitsStartIdx+digit] = outBuffer[digit];
            }
            // insert the leading zeros
            for (int i = 2; i < digitsStartIdx; ++i)
                outBuffer[i] = '0';
 
            // update the counts
            numFractionDigits += numFractionZeros;
            numPrintDigits = numFractionDigits;
        }
 
        // add the decimal point
        if (maxPrintLen > 1)
        {
            outBuffer[1] = '.';
            numPrintDigits += 1;
        }
 
        // add the initial zero
        if (maxPrintLen > 0)
        {
            outBuffer[0] = '0';
            numPrintDigits += 1;
        }
    }
    // add trailing zeros up to precision length
    if (precision > numFractionDigits && numPrintDigits < maxPrintLen)
    {
        // add a decimal point if this is the first fractional digit we are printing
        if (numFractionDigits == 0)
        {
            outBuffer[numPrintDigits++] = '.';
        }
 
        // compute the number of trailing zeros needed
        int totalDigits = numPrintDigits + (precision - numFractionDigits);
        if (totalDigits > maxPrintLen)
            totalDigits = maxPrintLen;
 
        for ( ; numPrintDigits < totalDigits; ++numPrintDigits )
        {
            outBuffer[numPrintDigits] = '0';
        }
    }
 
    // terminate the buffer
 //   RJ_ASSERT( numPrintDigits <= maxPrintLen );
    outBuffer[numPrintDigits] = '\0';
    return numPrintDigits;
  }

  /**
   * @param outBuffer
   * @param i
   * @param j
   * @return 
   */
  private static int printScientific(char[] outBuffer, int numPrintDigits, int printExponent)
  {
    int currentDigit = 0;
    int bufferSize = outBuffer.length;
    
    // keep the whole number as the first digit
    if (bufferSize > 1)
    {
        currentDigit += 1;
        bufferSize -= 1;
    }
 
    // insert the decimal point prior to the fractional number
    int numFractionDigits = numPrintDigits-1;
    if (numFractionDigits > 0 && bufferSize > 1)
    {
        int maxFractionDigits = bufferSize-2;
        if (numFractionDigits > maxFractionDigits)
            numFractionDigits =  maxFractionDigits;
 
        //memmove(pCurOut + 1, pCurOut, numFractionDigits);
        for(int digit=numFractionDigits-1; digit >= 0; digit--)
        {
          outBuffer[digit+1] = outBuffer[digit];
        }
        outBuffer[currentDigit] = '.';
        currentDigit += (1 + numFractionDigits);
        bufferSize -= (1 + numFractionDigits);
    }
 
    // add trailing zeros up to precision length
    if (precision > numFractionDigits && bufferSize > 1)
    {
        // add a decimal point if this is the first fractional digit we are printing
        if (numFractionDigits == 0)
        {
            outBuffer[currentDigit] = '.';
            ++currentDigit;
            --bufferSize;
        }
 
        // compute the number of trailing zeros needed
        int numZeros = (precision - numFractionDigits);
        if (numZeros > bufferSize-1)
            numZeros = bufferSize-1;
 
        for (int end = currentDigit + numZeros; currentDigit < end; ++currentDigit )
        {
            outBuffer[currentDigit] = '0';
        }
    }
 
    // print the exponent into a local buffer and copy into output buffer
    if (bufferSize > 1)
    {
        char exponentBuffer[] = new char[5];
        exponentBuffer[0] = 'e';
        if (printExponent >= 0)
        {
            exponentBuffer[1] = '+';
        }
        else
        {
            exponentBuffer[1] = '-';
            printExponent = -printExponent;
        }
 
        //RJ_ASSERT(printExponent < 1000);
        int hundredsPlace  = printExponent / 100;
        int tensPlace      = (printExponent - hundredsPlace*100) / 10;
        int onesPlace      = (printExponent - hundredsPlace*100 - tensPlace*10);
 
        exponentBuffer[2] = (char)('0' + hundredsPlace);
        exponentBuffer[3] = (char)('0' + tensPlace);
        exponentBuffer[4] = (char)('0' + onesPlace);
 
        // copy the exponent buffer into the output
        int maxExponentSize = bufferSize-1;
        int exponentSize = (5 < maxExponentSize) ? 5 : maxExponentSize;
        //memcpy( pCurOut, exponentBuffer, exponentSize );
        int digit;
        for(digit=0; digit < exponentSize; currentDigit++, digit++);
        {
          outBuffer[currentDigit] = exponentBuffer[digit];
        }
 
        bufferSize -= exponentSize;
    }
 
    //RJ_ASSERT( bufferSize > 0 );
    outBuffer[currentDigit] = '\n';
 
    return currentDigit;
  }

  /**
   * @param mantissa
   * @return
   */
  private final static int logBase2(int val)
  {
    int temp;
    
    temp = val >> 24;
    if (temp > 0)
        return 24 + logTable[temp];
 
    temp = val >> 16;
    if (temp > 0)
        return 16 + logTable[temp];
 
    temp = val >> 8;
    if (temp > 0)
        return 8 + logTable[temp];
 
    return logTable[val];
  }

  /**
   * @param outBuffer
   * @param floatMantissa
   * @return
   */
  private static String stringInfNan(char[] outBuffer, int currentDigit, long mantissa)
  {
    if(mantissa == 0)
    {
      outBuffer[currentDigit++] = 'I';
      outBuffer[currentDigit++] = 'n';
      outBuffer[currentDigit++] = 'f';
    }
    else
    {
      outBuffer[currentDigit++] = 'N';
      outBuffer[currentDigit++] = 'a';
      outBuffer[currentDigit++] = 'N';
      
    }
    return new String(outBuffer, 0, currentDigit);
  }

  final static void convertToString
  (
      long          mantissa,           // value significand
      int           exponent,           // value exponent in base 2
      int           mantissaHighBitIdx, // index of the highest set mantissa bit
      boolean       hasUnequalMargins,  // is the high margin twice as large as the low margin
      CutOffMode    cutoffMode,         // how to determine output length
      int           cutoffNumber,       // parameter to the selected cutoffMode
      char[]        outBuffer,         // buffer to output into
      int[]         outExponent        // the base 10 exponent of the first digit
  )
  {
    int currentDigit = 0;
    two = new BigInteger("2");
    
    if(mantissa==0)
    {
      outBuffer[currentDigit] = '0';
      outExponent[0] = 0;
      return;
    }
    
    // compute the initial state in integral form such that 
    //  value     = scaledValue / scale
    //  marginLow = scaledMarginLow / scale
    BigInteger scale;              // positive scale applied to value and margin such that they can be
                                //  represented as whole numbers
    BigInteger scaledValue;        // scale * mantissa
    BigInteger scaledMarginLow;    // scale * 0.5 * (distance between this floating-point number and its
                                //  immediate lower value)
 
    // For normalized IEEE floating point values, each time the exponent is incremented the margin also
    // doubles. That creates a subset of transition numbers where the high margin is twice the size of
    // the low margin.
    BigInteger scaledMarginHigh;
    BigInteger optionalMarginHigh;
 
//    VM.sysWrite("exponent: ", exponent); VM.sysWriteln(" mantissa: ", mantissa);
    if ( hasUnequalMargins )
    {
        // if we have no fractional component
        if (exponent > 0)
        {
            // 1) Expand the input value by multiplying out the mantissa and exponent. This represents
            //    the input value in its whole number representation.
            // 2) Apply an additional scale of 2 such that later comparisons against the margin values
            //    are simplified.
            // 3) Set the margin value to the lowest mantissa bit's scale.
             
            // scaledValue      = 2 * 2 * mantissa*2^exponent
            scaledValue = BigInteger.valueOf(4 * mantissa);
            scaledValue = scaledValue.shiftLeft(exponent);
             
            // scale            = 2 * 2 * 1
            scale = BigInteger.valueOf( 4 );
             
            // scaledMarginLow  = 2 * 2^(exponent-1)
            scaledMarginLow = two.shiftLeft(exponent-1);
 
            // scaledMarginHigh = 2 * 2 * 2^(exponent-1)
            optionalMarginHigh = BigInteger.valueOf(4);
            optionalMarginHigh = optionalMarginHigh.shiftLeft(exponent-1);
        }
        // else we have a fractional exponent
        else
        {
            // In order to track the mantissa data as an integer, we store it as is with a large scale
             
            // scaledValue      = 2 * 2 * mantissa
            scaledValue = BigInteger.valueOf(4 * mantissa);
             
            // scale            = 2 * 2 * 2^(-exponent)
            scale = BigInteger.valueOf(4);
            scale = scale.shiftLeft(-exponent);
             
            // scaledMarginLow  = 2 * 2^(-1)
            scaledMarginLow = BigInteger.ONE;
             
            // scaledMarginHigh = 2 * 2 * 2^(-1)
            optionalMarginHigh = BigInteger.valueOf(2);
        }
 
        // the high and low margins are different
        scaledMarginHigh = optionalMarginHigh;
    }
    else
    {
        // if we have no fractional component
        if (exponent > 0)
        {
            // 1) Expand the input value by multiplying out the mantissa and exponent. This represents
            //    the input value in its whole number representation.
            // 2) Apply an additional scale of 2 such that later comparisons against the margin values
            //    are simplified.
            // 3) Set the margin value to the lowest mantissa bit's scale.
             
            // scaledValue     = 2 * mantissa*2^exponent
            scaledValue = BigInteger.valueOf( 2 * mantissa );
            scaledValue = scaledValue.shiftLeft(exponent);
             
            // scale           = 2 * 1
            scale = two;
 
            // scaledMarginLow = 2 * 2^(exponent-1)
            scaledMarginLow = two.shiftLeft(exponent-1);
        }
        // else we have a fractional exponent
        else
        {
            // In order to track the mantissa data as an integer, we store it as is with a large scale
 
            // scaledValue     = 2 * mantissa
            scaledValue = BigInteger.valueOf(2 * mantissa);
             
            // scale           = 2 * 2^(-exponent)
            scale = two.shiftLeft(-exponent);
 
            // scaledMarginLow = 2 * 2^(-1)
            scaledMarginLow = BigInteger.ONE;
        }
     
        // the high and low margins are equal
        scaledMarginHigh = scaledMarginLow;
    }
    // Compute an estimate for digitExponent that will be correct or undershoot by one.
    // This optimization is based on the paper "Printing Floating-Point Numbers Quickly and Accurately"
    // by Burger and Dybvig http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.72.4656&rep=rep1&type=pdf
    // We perform an additional subtraction of 0.69 to increase the frequency of a failed estimate
    // because that lets us take a faster branch in the code. 0.69 is chosen because 0.69 + log10(2) is
    // less than one by a reasonable epsilon that will account for any floating point error.
    //
    // We want to set digitExponent to floor(log10(v)) + 1
    //  v = mantissa*2^exponent
    //  log2(v) = log2(mantissa) + exponent;
    //  log10(v) = log2(v) * log10(2)
    //  floor(log2(v)) = mantissaHighBitIdx + exponent;
    //  log10(v) - log10(2) < (mantissaHighBitIdx + exponent) * log10(2) <= log10(v)
    //  log10(v) < (mantissaHighBitIdx + exponent) * log10(2) + log10(2) <= log10(v) + log10(2)
    //  floor( log10(v) ) < ceil( (mantissaHighBitIdx + exponent) * log10(2) ) <= floor( log10(v) ) + 1
    double log10_2 = 0.30102999566398119521373889472449;
    int digitExponent = (int)(Math.ceil(((int)mantissaHighBitIdx + exponent) * log10_2 - 0.69));
 
    // if the digit exponent is smaller than the smallest desired digit for fractional cutoff,
    // pull the digit back into legal range at which point we will round to the appropriate value.
    // Note that while our value for digitExponent is still an estimate, this is safe because it
    // only increases the number. This will either correct digitExponent to an accurate value or it
    // will clamp it above the accurate value.
    if (cutoffMode == CutOffMode.FractionLength && digitExponent <= -cutoffNumber)
    {
        digitExponent = -cutoffNumber + 1;
    }
 
    // Divide value by 10^digitExponent. 
    if (digitExponent > 0)
    {
        // The exponent is positive creating a division so we multiply up the scale.
        BigInteger temp = BigInteger.TEN;
        // BigInt_MultiplyPow10( &temp, scale, digitExponent );
        temp = temp.pow(digitExponent);
        scale = temp.multiply(scale);
    }
    else if (digitExponent < 0)
    {
        // The exponent is negative creating a multiplication so we multiply up the scaledValue,
        // scaledMarginLow and scaledMarginHigh.
        BigInteger pow10 = BigInteger.TEN;
        //BigInt_Pow10( &pow10, -digitExponent);
        pow10 = pow10.pow(-digitExponent);
 
        //BigInt_Multiply( &temp, scaledValue, pow10);
        scaledValue = scaledValue.multiply(pow10);
 
        //BigInt_Multiply( &temp, scaledMarginLow, pow10);
        scaledMarginLow = scaledMarginLow.multiply(pow10);
         
        if (scaledMarginHigh.compareTo(scaledMarginLow) != 0)
        {
            //BigInt_Multiply2( pScaledMarginHigh, scaledMarginLow );
          scaledMarginHigh = scaledMarginLow.multiply(two);
        }
    }
    
    // If (value >= 1), our estimate for digitExponent was too low
    if( scaledValue.compareTo(scale) >= 0 ) // BigInt_Compare(scaledValue,scale) >= 0 )
    {
        // The exponent estimate was incorrect.
        // Increment the exponent and don't perform the premultiply needed
        // for the first loop iteration.
        digitExponent = digitExponent + 1;
    }
    else
    {
      // The exponent estimate was correct.
      // Multiply larger by the output base to prepare for the first loop iteration.
      //BigInt_Multiply10( &scaledValue );
      scaledValue = scaledValue.multiply(BigInteger.TEN);
      //  BigInt_Multiply10( &scaledMarginLow );
      scaledMarginLow = scaledMarginLow.multiply(BigInteger.TEN);
      if (scaledMarginHigh.compareTo(scaledMarginLow) != 0) //pScaledMarginHigh != &scaledMarginLow)
      {
        //BigInt_Multiply2( pScaledMarginHigh, scaledMarginLow );
        scaledMarginHigh = scaledMarginLow.multiply(two);
      }
    }
     
    // Compute the cutoff exponent (the exponent of the final digit to print).
    // Default to the maximum size of the output buffer.
    int cutoffExponent = digitExponent - outBuffer.length;
    switch(cutoffMode)
    {
    // print digits until we pass the accuracy margin limits or buffer size
    case Unique:
        break;
 
    // print cutoffNumber of digits or until we reach the buffer size
    case TotalLength:
        {
            int desiredCutoffExponent = digitExponent - cutoffNumber;
            if (desiredCutoffExponent > cutoffExponent)
                cutoffExponent = desiredCutoffExponent;
        }
        break;
 
    // print cutoffNumber digits past the decimal point or until we reach the buffer size
    case FractionLength:
        {
            int desiredCutoffExponent = -cutoffNumber;
            if (desiredCutoffExponent > cutoffExponent)
                cutoffExponent = desiredCutoffExponent;
        }
        break;
    }
 
    // Output the exponent of the first digit we will print
    outExponent[0] = digitExponent-1;
 
    // In preparation for calling BigInt_DivideWithRemainder_MaxQuotient9(), 
    // we need to scale up our values such that the highest block of the denominator
    // is greater than or equal to 8. We also need to guarantee that the numerator
    // can never have a length greater than the denominator after each loop iteration.
    // This requires the highest block of the denominator to be less than or equal to
    // 429496729 which is the highest number that can be multiplied by 10 without
    // overflowing to a new block.
//    RJ_ASSERT( scale.GetLength() > 0 );
//    tU32 hiBlock = scale.GetBlock( scale.GetLength() - 1 );
//    if (hiBlock < 8 || hiBlock > 429496729)
//    {
//        // Perform a bit shift on all values to get the highest block of the denominator into
//        // the range [8,429496729]. We are more likely to make accurate quotient estimations
//        // in BigInt_DivideWithRemainder_MaxQuotient9() with higher denominator values so
//        // we shift the denominator to place the highest bit at index 27 of the highest block.
//        // This is safe because (2^28 - 1) = 268435455 which is less than 429496729. This means
//        // that all values with a highest bit at index 27 are within range.         
//        tU32 hiBlockLog2 = LogBase2(hiBlock);
//        RJ_ASSERT(hiBlockLog2 < 3 || hiBlockLog2 > 27);
//        tU32 shift = (32 + 27 - hiBlockLog2) % 32;
// 
//        BigInt_ShiftLeft( &scale, shift );
//        BigInt_ShiftLeft( &scaledValue, shift);
//        BigInt_ShiftLeft( &scaledMarginLow, shift);
//        if (pScaledMarginHigh != &scaledMarginLow)
//            BigInt_Multiply2( pScaledMarginHigh, scaledMarginLow );
//    }
 
    // These values are used to inspect why the print loop terminated so we can properly
    // round the final digit.
    boolean      low;            // did the value get within marginLow distance from zero
    boolean      high;           // did the value get within marginHigh distance from one
    BigInteger   outputDigit;    // current digit being output
     
    if (cutoffMode == CutOffMode.Unique)
    {
        // For the unique cutoff mode, we will try to print until we have reached a level of
        // precision that uniquely distinguishes this value from its neighbors. If we run
        // out of space in the output buffer, we terminate early.
        //VM.sysWrite("cts: ", scaledValue.toString()); VM.sysWriteln(" ", scale.toString());
        for (;;)
        {
            digitExponent = digitExponent-1;
 
            // divide out the scale to extract the digit
            // outputDigit = BigInt_DivideWithRemainder_MaxQuotient9(&scaledValue, scale);
            BigInteger[] qr = scaledValue.divideAndRemainder(scale);
            outputDigit = qr[0];  // quotient
            scaledValue = qr[1];  // remainder
            //RJ_ASSERT( outputDigit < 10 );
 
            // update the high end of the value
            BigInteger scaledValueHigh;
            //BigInt_Add( &scaledValueHigh, scaledValue, *pScaledMarginHigh );
            scaledValueHigh = scaledValue.add(scaledMarginHigh);
 
            // stop looping if we are far enough away from our neighboring values
            // or if we have reached the cutoff digit
            // low = BigInt_Compare(scaledValue, scaledMarginLow) < 0;
            low = (scaledValue.compareTo(scaledMarginLow) < 0);
            // high = BigInt_Compare(scaledValueHigh, scale) > 0;
            high = (scaledValueHigh.compareTo(scale) > 0);
            
            if (low | high | (digitExponent == cutoffExponent))
                break;
             
            // store the output digit
            
//            *pCurDigit = (tC8)('0' + outputDigit);
//            ++pCurDigit;
            outBuffer[currentDigit] = (char) ('0' + outputDigit.intValue());
            currentDigit++;
            // multiply larger by the output base
            //BigInt_Multiply10( &scaledValue );
            scaledValue = scaledValue.multiply(BigInteger.TEN);
            //BigInt_Multiply10( &scaledMarginLow );
            scaledMarginLow = scaledMarginLow.multiply(BigInteger.TEN);
//            if (pScaledMarginHigh != &scaledMarginLow)
//                BigInt_Multiply2( pScaledMarginHigh, scaledMarginLow );                 
            if(scaledMarginHigh.compareTo(scaledMarginLow) != 0)
            {
              scaledMarginHigh = scaledMarginLow.multiply(two);
            }
        }
    }
    else
    {
        // For length based cutoff modes, we will try to print until we
        // have exhausted all precision (i.e. all remaining digits are zeros) or
        // until we reach the desired cutoff digit.
        low = false;
        high = false;
 
        for (;;)
        {
            digitExponent = digitExponent-1;
 
            // divide out the scale to extract the digit
            // outputDigit = BigInt_DivideWithRemainder_MaxQuotient9(&scaledValue, scale);
            BigInteger[] qr = scaledValue.divideAndRemainder(scale);
            outputDigit = qr[0];  // quotient
            scaledValue = qr[1];  // remainder
            // RJ_ASSERT( outputDigit < 10 );
 
            if ( scaledValue.compareTo(BigInteger.ZERO) == 0 || (digitExponent == cutoffExponent) )
                break;
 
            // store the output digit
//            *pCurDigit = (tC8)('0' + outputDigit);
//            ++pCurDigit;
            outBuffer[currentDigit] = (char) ('0' + outputDigit.intValue());
            currentDigit++;
 
            // multiply larger by the output base
            //BigInt_Multiply10(&scaledValue);
            scaledValue = scaledValue.multiply(BigInteger.TEN);
        }
    }
 
    // round off the final digit
    // default to rounding down if value got too close to 0
    boolean roundDown = low;
     
    // if it is legal to round up and down
    if (low == high)
    {
        // round to the closest digit by comparing value with 0.5. To do this we need to convert
        // the inequality to large integer values.
        //  compare( value, 0.5 )
        //  compare( scale * value, scale * 0.5 )
        //  compare( 2 * scale * value, scale )
        // BigInt_Multiply2(&scaledValue);
        scaledValue = scaledValue.multiply(two);
        // tS32 compare = BigInt_Compare(scaledValue, scale);
        int compare = scaledValue.compareTo(scale);
        roundDown = (compare < 0);
         
        // if we are directly in the middle, round towards the even digit (i.e. IEEE rouding rules)
        if (compare == 0)
        {
            roundDown = ((outputDigit.intValue() & 1) == 0);             
        }
    }
 
    // print the rounded digit
    if (roundDown)
    {
//        *pCurDigit = (tC8)('0' + outputDigit);
//        ++pCurDigit;
        outBuffer[currentDigit] = (char) ('0' + outputDigit.intValue());
        currentDigit++;
    }
    else
    {
        // handle rounding up
        if (outputDigit.intValue() == 9)
        {
            // find the first non-nine prior digit
            for (;;)
            {
                // if we are at the first digit
//                if (pCurDigit == pOutBuffer)
//                {
//                    // output 1 at the next highest exponent
//                    *pCurDigit = '1';
//                    ++pCurDigit;
//                    *pOutExponent += 1;
//                    break;
//                }
              if(currentDigit == 0)
              {
                outBuffer[0] = '1';
                currentDigit++;
                outExponent[0] += 1;
              }
              
//                --pCurDigit;
//                if (*pCurDigit != '9')
//                {
//                    // increment the digit
//                    *pCurDigit += 1;
//                    ++pCurDigit;
//                    break;
//                }
              currentDigit--;
              if(outBuffer[currentDigit] != '9')
              {
                outBuffer[currentDigit] = (char) (outBuffer[currentDigit] + 1);
                currentDigit++;
                break;
              }
            }
        }
        else
        {
            // values in the range [0,8] can perform a simple round up
//            *pCurDigit = (tC8)('0' + outputDigit + 1);
//            ++pCurDigit;
          outBuffer[currentDigit] = (char) ('0' + outputDigit.intValue());
          currentDigit++;
        }
    }
 
    // return the number of digits output
    outExponent[1] = currentDigit;
    // RJ_ASSERT(outputLen <= bufferSize);
    return;
    
  }

  private final static byte logTable[] = 
  {
      0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
      5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
      6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
      6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
      6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
      6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
  };
}
