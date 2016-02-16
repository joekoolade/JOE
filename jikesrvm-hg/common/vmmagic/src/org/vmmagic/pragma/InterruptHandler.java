/**
 * Created on Feb 15, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.vmmagic.pragma;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.vmmagic.Pragma;

/**
 * @author Joe Kulig
 * 
 * Methods with this pragma are interrupt handlers and should save all registers in its prologue. The epilogue
 * should restore its registers.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Pragma
public @interface InterruptHandler {

}
