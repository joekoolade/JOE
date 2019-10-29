
public class TwoLongDivide
{
    /*
     * a is dividend
     * b is divisor
     * 
     * return a/b
     */
    public static long[] div64(long a, long b)
    {
        long q = 0, r = 0;

        if(b==0) throw new ArithmeticException("divide by 0");

        for(int n=63; n >= 0; n--)
        {
            r <<= 1;
            r |= (a >>> n) & 0x1;
            if(r > b)
            {
                r -= b;
                q |= (1 << n);
            }
        }
        long result[] = new long[2];
        result[0] = q;
        result[1] = r;
        return result;
    }
    
    public static void test(long a, long b)
    {
        long[] result;
        long e;
        
        result = div64(a,b);
        e = a / b;
        if(e != result[0]) System.out.println("divide expected: "+ e + " actual: "+result[0]);
        e = a % b;
        if(e != result[1]) System.out.println("remainder expected: "+ e + " actual: "+result[1]);
        System.out.println("----------------------------");
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
