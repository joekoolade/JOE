/**
 * Created on Aug 30, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

/**
 * @author Joe Kulig
 *
 */
public enum MdiRegister {
  CONTROL,
  STATUS,
  PHY1,
  PHY2,
  AUTONEG_ADVERTISEMENT,
  AUTONEG_LINK_PARTNER_ABILITY,
  AUTONEG_EXPANSION,
  RSVD7, RSVD8, RSVD9, RSVD10, RSVD11, RSVD12, RSVD13, RSVD14, RSVD15,
  STATUS_CTL_EXT,
  SPC_CTL,
  CLK_TEST_CTL;
}
