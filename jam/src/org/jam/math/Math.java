package org.jam.math;

public class Math
{
    public static long[] div64qr(long n, long d)
    {
        long qr[];
        long d0=d, n0=n;
        boolean negative = false;
        if(d==0) throw new ArithmeticException("divide by 0");
        if(d < 0)
        {
            d0 = -d;
            negative = true;
        }
        if(n < 0)
        {
            n0 = -n;
            negative = !negative;
        }
        qr = udiv64qr(n0, d0);
        if(negative)
        {
            qr[0] = -qr[0];
        }
        return qr;
    }

    public static long div64(long n, long d)
    {
        long q;
        long d0=d, n0=n;
        boolean negative = false;
        if(d==0) throw new ArithmeticException("divide by 0");
        if(d < 0)
        {
            d0 = -d;
            negative = true;
        }
        if(n < 0)
        {
            n0 = -n;
            negative = !negative;
        }
        q = udiv64(n0, d0);
        if(negative)
        {
            q = -q;
        }
        return q;
    }

    public static long mod64(long n, long d)
    {
        long r;
        long d0=d, n0=n;
        boolean negative = false;
        if(d==0) throw new ArithmeticException("divide by 0");
        if(d < 0)
        {
            d0 = -d;
            negative = true;
        }
        if(n < 0)
        {
            n0 = -n;
            negative = !negative;
        }
        r = umod64(n0, d0);
        return r;
    }
    /*
     * a is an unsigned dividend
     * b is an unsigned divisor
     * 
     * return a/b
     */
    public static long[] udiv64qr(long a, long b)
    {
        long q = 0, r = 0;

        for(int n=63; n >= 0; n--)
        {
            r = r << 1;
            r |= ((a >> n) & 0x1L);
            if(r >= b)
            {
                r -= b;
                q = q | (1L << n);
                //System.out.println(n + " " + Long.toHexString(q) + " ");
            }
        }
        //System.out.println();
        long result[] = new long[2];
        result[0] = q;
        result[1] = r;
        return result;
    }
    
    public static long udiv64(long a, long b)
    {
        long q = 0, r = 0;

        for(int n=63; n >= 0; n--)
        {
            r = r << 1;
            r |= ((a >> n) & 0x1L);
            if(r >= b)
            {
                r -= b;
                q = q | (1L << n);
                //System.out.println(n + " " + Long.toHexString(q) + " ");
            }
        }
        return q;
    }
    
    public static long umod64(long a, long b)
    {
        long q = 0, r = 0;

        for(int n=63; n >= 0; n--)
        {
            r = r << 1;
            r |= ((a >> n) & 0x1L);
            if(r >= b)
            {
                r -= b;
                q = q | (1L << n);
                //System.out.println(n + " " + Long.toHexString(q) + " ");
            }
        }
        return r;
    }
    
    public static void test(long a, long b)
    {
        long result;
        long e;
        
        result = div64(a,b);
        e = a / b;
        if(e != result) System.out.println("divide expected: "+ e + " actual: "+result);
        e = a % b;
        result = mod64(a,b);
        if(e != result) System.out.println("remainder expected: "+ e + " actual: "+result);
        //System.out.println("----------------------------");
    }
    
    public static void main(String[] args)
    {
        long a,b;
        
        a = 39303809;
        b = 111;
        test(a,b);
        
        a = 111111111111L;
        b = 294829;
        test(a,b);
        
        a=0x7fffffffffffffffL;
        b = 1;
        test(a,b);
        b=0x7fffffffffffffffL;
        test(a,b);
        a=1;
        b=1;
        test(a,b);
        a=0x8000000000000000L;
        b=1;
        test(a,b);
        b=0xffffffffffffffffL;
        test(a,b);
    }
}
