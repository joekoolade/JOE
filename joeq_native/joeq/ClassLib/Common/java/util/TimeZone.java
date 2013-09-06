// TimeZone.java, created Thu Jul  4  4:50:04 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.util;

import joeq.Bootstrap.MethodInvocation;
import joeq.Class.jq_Class;
import joeq.Class.jq_DontAlign;
import joeq.Class.jq_Method;
import joeq.Class.jq_NameAndDesc;
import joeq.Main.jq;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Runtime.Debug;
import joeq.Runtime.Reflection;
import joeq.Runtime.SystemInterface;
import joeq.Runtime.Unsafe;
import joeq.Runtime.SystemInterface.ExternalLink;
import joeq.Runtime.SystemInterface.Library;
import jwutil.util.Assert;

/**
 * TimeZone
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: TimeZone.java,v 1.11 2004/09/30 03:35:34 joewhaley Exp $
 */
abstract class TimeZone {

    public static TimeZoneInformation cachedTimeZone;
    public static int cachedTimeZoneId;

    private static String getSystemTimeZoneID(String javaHome, String region) {
        if (cachedTimeZone == null) {
            TimeZoneInformation info = new TimeZoneInformation();
            int rc = get_time_zone_information(info);
            if (rc == TIME_ZONE_ID_INVALID) return null;
            cachedTimeZone = info;
            cachedTimeZoneId = rc;
        }
        String s;
        switch (cachedTimeZoneId) {
            case TIME_ZONE_ID_UNKNOWN:
            case TIME_ZONE_ID_STANDARD:
                s = cachedTimeZone.getStandardName();
                break;
            case TIME_ZONE_ID_DAYLIGHT:
                s = cachedTimeZone.getDaylightName();
                break;
            default:
                return null;
        }
        if (s.equals("Pacific Standard Time") || s.equals("Pacific Daylight Time"))
            return "America/Los Angeles";
        // TODO: other time zones.
        return "America/Los Angeles";
    }
    
    private static String getSystemGMTOffsetID() {
        if (cachedTimeZone == null) {
            TimeZoneInformation info = new TimeZoneInformation();
            int rc = get_time_zone_information(info);
            if (rc == TIME_ZONE_ID_INVALID) return null;
            cachedTimeZone = info;
            cachedTimeZoneId = rc;
        }
        int bias = -cachedTimeZone.Bias;
        int hr = bias / 60;
        int min = java.lang.Math.abs(bias % 60);
        String hr_s, min_s;
        if (hr >= 0) {
            if (hr <= 9) {
                hr_s = "+0"+hr;
            } else {
                hr_s = "+"+hr;
            }
        } else {
            hr = -hr;
            min = -min;
            if (hr <= 9) {
                hr_s = "-0"+hr;
            } else {
                hr_s = "-"+hr;
            }
        }
        Assert._assert(min >= 0);
        if (min <= 9) {
            min_s = ":0"+min;
        } else {
            min_s = ":"+min;
        }
        return "GMT"+hr_s+min_s;
    }
    
    public static class TimeZoneInformation implements jq_DontAlign {
        // from TIME_ZONE_INFORMATION structure in winbase.h
        int Bias;
        long StandardName0; // WCHAR array
        long StandardName1;
        long StandardName2;
        long StandardName3;
        short StandardDate_wYear;
        short StandardDate_wMonth;
        short StandardDate_wDayOfWeek;
        short StandardDate_wDay;
        short StandardDate_wHour;
        short StandardDate_wMinute;
        short StandardDate_wSecond;
        short StandardDate_wMilliseconds;
        int StandardBias;
        long DaylightName0; // WCHAR array
        long DaylightName1;
        long DaylightName2;
        long DaylightName3;
        short DaylightDate_wYear;
        short DaylightDate_wMonth;
        short DaylightDate_wDayOfWeek;
        short DaylightDate_wDay;
        short DaylightDate_wHour;
        short DaylightDate_wMinute;
        short DaylightDate_wSecond;
        short DaylightDate_wMilliseconds;
        int DaylightBias;
        
        public void dump() {
            Debug.writeln("Bias=", Bias);
            Debug.write("StandardName=");
            HeapAddress s = getStandardNameAddress();
            try {
                Unsafe.pushArg(strlen(s));
                Unsafe.pushArgA(s);
                Unsafe.invoke(SystemInterface.debugwwriteln_8);
            } catch (Throwable t) { }
            Debug.write("StandardDate={ wYear=", StandardDate_wYear);
            Debug.write(", wMonth=", StandardDate_wMonth);
            Debug.write(", wDayOfWeek=", StandardDate_wDayOfWeek);
            Debug.write(", wDay=", StandardDate_wDay);
            Debug.write(", wHour=", StandardDate_wHour);
            Debug.write(", wMinute=", StandardDate_wMinute);
            Debug.write(", wSecond=", StandardDate_wSecond);
            Debug.write(", wMilliseconds=", StandardDate_wMilliseconds);
            Debug.writeln(" }");
            Debug.writeln("StandardBias=", StandardBias);
            
            Debug.write("DaylightName=");
            s = getDaylightNameAddress();
            try {
                Unsafe.pushArg(strlen(s));
                Unsafe.pushArgA(s);
                Unsafe.invoke(SystemInterface.debugwwriteln_8);
            } catch (Throwable t) { }
            Debug.write("DaylightDate={ wYear=", DaylightDate_wYear);
            Debug.write(", wMonth=", DaylightDate_wMonth);
            Debug.write(", wDayOfWeek=", DaylightDate_wDayOfWeek);
            Debug.write(", wDay=", DaylightDate_wDay);
            Debug.write(", wHour=", DaylightDate_wHour);
            Debug.write(", wMinute=", DaylightDate_wMinute);
            Debug.write(", wSecond=", DaylightDate_wSecond);
            Debug.write(", wMilliseconds=", DaylightDate_wMilliseconds);
            Debug.writeln(" }");
            Debug.writeln("DaylightBias=", Bias);
        }
        
        public String getStandardName() {
            HeapAddress s = getStandardNameAddress();
            return fromCString(s);
        }
        
        HeapAddress getStandardNameAddress() {
            return (HeapAddress) HeapAddress.addressOf(this).offset(4);
        }
        
        public String getDaylightName() {
            HeapAddress s = getDaylightNameAddress();
            return fromCString(s);
        }
        
        HeapAddress getDaylightNameAddress() {
            return (HeapAddress) HeapAddress.addressOf(this).offset(56);
        }
        
        static String fromCString(HeapAddress s) {
            StringBuffer sb = new StringBuffer();
            int strlen = 0;
            char c;
            while ((c = (char)s.peek2()) != (char)0 && strlen < 32) {
                sb.append(c);
                ++strlen;
                s = (HeapAddress) s.offset(2);
            }
            return sb.toString();
        }
        
        static int strlen(HeapAddress s) {
            int strlen = 0;
            while (s.peek2() != (short)0 && strlen < 32) {
                ++strlen;
                s = (HeapAddress) s.offset(2);
            }
            return strlen;
        }
        
        
    }
    
    public static int get_time_zone_information(TimeZoneInformation t) {
        try {
            CodeAddress a = GetTimeZoneInformation.resolve();
            HeapAddress b = HeapAddress.addressOf(t);
            Unsafe.pushArgA(b);
            Unsafe.getThreadBlock().disableThreadSwitch();
            int rc = (int) Unsafe.invoke(a);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return rc;
        } catch (Throwable x) { Assert.UNREACHABLE(); }
        return 0;
    }
    
    // defined in winbase.h
    public static final int TIME_ZONE_ID_UNKNOWN = 0;
    public static final int TIME_ZONE_ID_STANDARD = 1;
    public static final int TIME_ZONE_ID_DAYLIGHT = 2;
    public static final int TIME_ZONE_ID_INVALID = 0xFFFFFFFF;
    
    public static /*final*/ ExternalLink GetTimeZoneInformation;

    static {
        if (jq.RunningNative) boot();
        else if (jq.on_vm_startup != null) {
            jq_Class c = (jq_Class) Reflection.getJQType(java.util.TimeZone.class);
            jq_Method m = c.getDeclaredStaticMethod(new jq_NameAndDesc("boot", "()V"));
            MethodInvocation mi = new MethodInvocation(m, null);
            jq.on_vm_startup.add(mi);
        }
    }

    public static void boot() {
        Library kernel32 = SystemInterface.registerLibrary("kernel32");

        if (kernel32 != null) {
            GetTimeZoneInformation = kernel32.resolve("GetTimeZoneInformation");
        } else {
            GetTimeZoneInformation = null;
        }

        //System.out.println("getSystemGMTOffsetID="+getSystemGMTOffsetID());
        //cachedTimeZone.dump();
        //System.out.println("timeZoneID="+cachedTimeZoneId);
    }
}
