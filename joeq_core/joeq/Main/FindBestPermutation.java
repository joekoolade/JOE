// FindBestPermutation.java, created Mar 19, 2004 10:11:44 PM 2004 by jwhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Main;

import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import jwutil.util.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;

/**
 * FindBestPermutation
 * 
 * @author jwhaley
 * @version $Id: FindBestPermutation.java,v 1.11 2005/03/14 05:59:28 joewhaley Exp $
 */
public class FindBestPermutation extends Thread {
    
    static long bestTime = Integer.MAX_VALUE;
    static int bestNodeCount = Integer.MAX_VALUE;
    static String bestOrdering;
    
    boolean reverse;
    String orderingToTry;
    String filename;
    int myNodeCount;
    
    /**
     * @param reverse
     * @param orderingToTry
     * @param filename
     */
    public FindBestPermutation(boolean reverse, String orderingToTry,
            String filename) {
        super();
        this.reverse = reverse;
        this.orderingToTry = orderingToTry;
        this.filename = filename;
    }
    
    public void run() {
        BDDFactory bdd = JFactory.init(1000000, 50000);
        //bdd.setMaxIncrease(250000);
        bdd.setIncreaseFactor(2);
        readBDDConfig(bdd);
        int[] varorder = bdd.makeVarOrdering(reverse, orderingToTry);
        bdd.setVarOrder(varorder);
        //System.out.println("\nTrying ordering "+orderingToTry);
        try {
            BDD foo = bdd.load(filename);
            myNodeCount = foo.nodeCount();
        } catch (IOException x) {
        }
        System.out.println("Ordering: "+orderingToTry+" node count: "+myNodeCount+" vs. best "+bestNodeCount);
        bdd.done();
    }
    
    static int N_ITER = 100;
    
    public static void main(String[] args) {
        String ordering = System.getProperty("bddordering");
        boolean reverse = System.getProperty("bddnoreverse") == null;
        String filename = args[0];
        int nDomains = countDomains(ordering);
        //pg = new PermutationGenerator(nDomains);
        boolean flip = false;
        boolean updated = true;
        for (int i = 0; i < N_ITER; ++i) {
            FindBestPermutation t = new FindBestPermutation(reverse, ordering, filename);
            long time = System.currentTimeMillis();
            t.start();
            try {
                t.join(bestTime + 1000);
            } catch (InterruptedException e) {
            }
            if (t.myNodeCount == 0) {
                try {
                    System.out.println("Thread timed out!");
                    t.stop();
                    System.gc();
                    System.out.println("Thread killed.");
                    Thread.sleep(100);
                } catch (InterruptedException e2) { }
            } else if (t.myNodeCount < bestNodeCount) {
                bestNodeCount = t.myNodeCount;
                bestOrdering = ordering;
                bestTime = System.currentTimeMillis() - time;
                System.out.println("New best: ordering = "+bestOrdering+" node count: "+bestNodeCount+" time: "+bestTime+" ms");
                if (index1 != 0 || index2 != 0) updated = true;
            }
            if (flip) {
                ordering = tweakInterleaving(bestOrdering);
                System.out.println("Tweaked interleaving = "+ordering);
            } else {
                if (index1 == 0 && index2 == 0) {
                    if (!updated) {
                        break;
                    }
                    updated = false;
                }
                ordering = tweakOrdering(bestOrdering);
                System.out.println("Tweaked ordering = "+ordering);
            }
            flip = !flip;
        }
        System.out.println("Best: ordering = "+bestOrdering+" node count: "+bestNodeCount+" time: "+bestTime+" ms");
    }
    
    public static void readBDDConfig(BDDFactory bdd) {
        String fileName = System.getProperty("bddcfg", "bdd.cfg");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(fileName));
            for (;;) {
                String s = in.readLine();
                if (s == null || s.equals("")) break;
                StringTokenizer st = new StringTokenizer(s);
                String name = st.nextToken();
                long size = Long.parseLong(st.nextToken())-1;
                makeDomain(bdd, name, BigInteger.valueOf(size).bitLength());
            }
            in.close();
        } catch (IOException x) {
        } finally {
            if (in != null) try { in.close(); } catch (IOException _) { }
        }
    }
    
    static BDDDomain makeDomain(BDDFactory bdd, String name, int bits) {
        Assert._assert(bits < 64);
        BDDDomain d = bdd.extDomain(new long[] { 1L << bits })[0];
        d.setName(name);
        //System.out.println("Domain "+name+", "+bits+" bits");
        return d;
    }
    
    static int countDomains(String order) {
        StringTokenizer st = new StringTokenizer(order, "x_");
        int n = 1;
        while (st.hasMoreTokens()) {
            st.nextToken();
            ++n;
        }
        return n;
    }
    
    //static PermutationGenerator pg;
    
    static int index1, index2;
    
    static String tweakOrdering(String ordering) {
        StringTokenizer st = new StringTokenizer(ordering, "_");
        int num = 0;
        for (int i = 0; st.hasMoreTokens(); ++i) {
            st.nextToken();
            ++num;
        }
        st = new StringTokenizer(ordering, "_");
        String[] a = new String[num];
        for (int i = 0; i < a.length; ++i) {
            a[i] = st.nextToken();
        }
        ++index2;
        if (index2 == index1) ++index2;
        if (index2 >= a.length) {
            ++index1;
            index2 = index1 + 1;
            if (index1 >= a.length-1) {
                index1 = 0;
            }
            if (index2 >= a.length) {
                index2 = 0;
            }
        }
        System.out.println("Swapping "+index1+" and "+index2);
        String temp = a[index1];
        a[index1] = a[index2];
        a[index2] = temp;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < a.length; ++i) {
            sb.append(a[i]);
            if (i < a.length - 1)
                sb.append("_");
        }
        return sb.toString();
    }
    
    static int indexp;
    
    static String tweakInterleaving(String ordering) {
        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(ordering, "_x", true);
        sb.append(st.nextToken());
        for (int i = 0; i < indexp; ++i) {
            sb.append(st.nextToken());
            sb.append(st.nextToken());
        }
        if (!st.hasMoreTokens()) {
            indexp = 0;
            return sb.toString();
        }
        indexp++;
        String s = st.nextToken();
        if (s.equals("x")) sb.append("_");
        else sb.append("x");
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
        }
        return sb.toString();
    }
}
