/*
 * Copyright (C) 2000 Free Software Foundation
 * 
 * This file is part of libgcj.
 * 
 * This software is copyrighted work licensed under the terms of the Libgcj
 * License. Please consult the file "LIBGCJ_LICENSE" for details.
 */

package gnu.gcj.io;

import java.util.*;
import java.io.*;

/*
 * fixme: this whole class needs fixing or whoever uses it.
 * 
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MimeTypes {

  private static Hashtable mime_types;

  public static void fillFromFile(Hashtable table, String fname) throws IOException {
  }

  // This is the primary interface to this class.
  public static String getMimeTypeFromExtension(String extension) {
    return "";
  }
}