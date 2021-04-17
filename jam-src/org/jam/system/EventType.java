package org.jam.system;

import org.vmmagic.pragma.NonMoving;
import org.vmmagic.pragma.Uninterruptible;

@NonMoving
public enum EventType
{
    NONE,
    IRQ,
    SCHEDULE,
    SW,
}
