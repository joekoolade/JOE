// SourceLister.java, created Sun Dec  7 14:20:28 PST 2003
// Copyright (C) 2003 Godmar Back <gback@cs.utah.edu, @stanford.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Util.IO;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.Arrays;
import java.text.DecimalFormat;

import joeq.Class.jq_Class;
import joeq.Compiler.Analysis.IPA.ProgramLocation;

/**
 * SourceLister
 *
 * Maintains a set of source directories and lists source code 
 * around a given ProgramLocation.
 *
 * @author Godmar Back <gback@cs.utah.edu, @stanford.edu>
 */
public class SourceLister {
    // You can use reflection to set these, then use the default constructor to construct 
    // SourceLister instances with the new settings.
    public static String []defaultSrcDirs = new String[] { "." };
    public static int defaultLinesBefore = 5;
    public static int defaultLinesAfter = 5;

    String []srcDirs;

    /**
     * Create a new source lister with the default path and before/after lines.
     */
    public SourceLister() {
        this(defaultSrcDirs);
    }

    /**
     * Create a new source lister with the specified paths and before/after lines.
     */
    public SourceLister(String []srcDirs) {
        this.srcDirs = srcDirs;
    }

    /**
     * Try to find source for this location and return a section of the source file
     * formatted for display.
     */
    public String list(ProgramLocation pl) {
        return list(pl, true, defaultLinesBefore, defaultLinesAfter);
    }

    public String list(ProgramLocation pl, String comment) {
        return list(pl, true, defaultLinesBefore, defaultLinesAfter, comment);
    }

    public String list(ProgramLocation pl, boolean withnumbers, int linesBefore, int linesAfter) {
        return list(pl, withnumbers, linesBefore, linesAfter, "");
    }

    public String list(ProgramLocation pl, boolean withnumbers, int linesBefore, int linesAfter, String comment) {
        jq_Class clazz = pl.getContainingClass();
        String clazzName = clazz.getName();
        char fileSep = File.separatorChar;
        int lastdot = clazzName.lastIndexOf('.');
        String pathName = lastdot != -1 ? fileSep + clazzName.substring(0, lastdot) : "";
        String pathSuffix = pathName.replace('.', fileSep) + fileSep + pl.getSourceFile();
        DecimalFormat d5 = new DecimalFormat("00000");
        int lno = pl.getLineNumber();

    outer:
        for (int i = 0; i < srcDirs.length; i++) {
            String possibleInputFile = srcDirs[i] + pathSuffix;
            File f = new File(possibleInputFile);
            if (f.exists()) {
                StringBuffer res = new StringBuffer();
                BufferedReader r = null;
                try {
                    r = new BufferedReader(new FileReader(possibleInputFile));
                    int l = 1;
                    if (withnumbers)
                        res.append("# " + pl.getSourceFile() + ":" + lno + "\n");
                    for (; l < lno - linesBefore; l++)
                        r.readLine();
                    for (; l < lno + linesAfter + 1; l++) {
                        String s = r.readLine();
                        if (s == null)
                            break;
                        // 5+3 starts in column 8 for proper tabbing
                        if (withnumbers)
                            res.append(d5.format(l) + ":  ");
                        res.append(s);
                        if (withnumbers && l == lno)
                            res.append(" <<<==================== " + comment);
                        res.append("\n");
                    }
                } catch (IOException io) {
                    continue outer;
                } finally {
                    if (r != null) try { r.close(); } catch (IOException _) { }
                }
                return res.toString();
            }
        }
        return null;
    }

    public String toString() {
        return getClass() + " " + Arrays.asList(srcDirs);
    }
}
