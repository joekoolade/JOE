// jq_DontAlign.java, created Mon Apr  9  1:30:26 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

/**
 * This interface is used as a marker to signify that the fields in the
 * class should not be aligned.  This is necessary if the layout must
 * match another data structure exactly, for example: in
 * joeq.Scheduler.jq_RegisterState.
 * 
 * @see joeq.Scheduler.jq_RegisterState
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_DontAlign.java,v 1.11 2004/09/22 22:17:28 joewhaley Exp $
 */
public interface jq_DontAlign {

    jq_Class _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_DontAlign;");
}
