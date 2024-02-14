package org.jam.tests;

import java.text.DecimalFormat;
import java.util.Locale;

import org.jikesrvm.VM;

public class LocaleTest implements Runnable {

    @Override
    public void run() {
       localeTest();

    }

    public void localeTest() {
        System.out.println("Get Locale");
        Locale def = Locale.getDefault();
        System.out.println("def: "+def);
        def = Locale.getDefault(Locale.Category.DISPLAY);
        System.out.println("display: "+def);
        def = Locale.getDefault(Locale.Category.FORMAT);
        System.out.println("format: "+def);
        DecimalFormat dformat = new DecimalFormat();
        System.out.println("decimal format done");
        System.out.println("pattern: "+dformat.toPattern());
        loopForever();
    }

    private void loopForever() {
        System.out.println("loop forever");
        while(true);
    }

}
