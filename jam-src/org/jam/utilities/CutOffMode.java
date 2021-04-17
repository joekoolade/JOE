/**
 * Created on Jan 28, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.utilities;

/**
 * @author Joe Kulig
 *
 */
public enum CutOffMode {
  Unique,          // as many digits as necessary to print a uniquely identifiable number
  TotalLength,     // up to cutoffNumber significant digits
  FractionLength,  // up to cutoffNumber significant digits past the decimal point
}
