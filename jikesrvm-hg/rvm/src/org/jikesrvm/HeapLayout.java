package org.jikesrvm;

import org.vmmagic.unboxed.Address;

/**
 * Class that is populated with the layout of the 
 * boot image
 */
public final class HeapLayout {

    private HeapLayout()
    {        
    }
    
    /** The address of the start of the data section of the boot image. */
    public static Address BOOT_IMAGE_DATA_START = Address.fromIntZeroExtend(0x108000);
    /** The address of the start of the code section of the boot image. */
    public static Address BOOT_IMAGE_CODE_START;
    /** The address of the start of the ref map section of the boot image. */
    public static Address BOOT_IMAGE_RMAP_START;
    
    /** The address in virtual memory that is the highest that can be mapped. */
    public static Address MAXIMUM_MAPPABLE = Address.fromIntZeroExtend(0xF0000000);
    
    /** 
     * The maximum boot image data size 
     * This value will change after the auto-sizing 
     */
    public static int  BOOT_IMAGE_DATA_SIZE = 0x10000000;
    /**
     * The maximum boot image code size
     * This initial value will change after auto-sizing
     */
    public static int BOOT_IMAGE_CODE_SIZE = 0x01000000;

    /* Typical compression ratio is about 1/20 */
    public static int BAD_MAP_COMPRESSION = 5;  // conservative heuristic
    public static int MAX_BOOT_IMAGE_RMAP_SIZE = -1;

    /** The address of the end of the data section of the boot image. */
    public static Address BOOT_IMAGE_DATA_END;
    /** The address of the end of the code section of the boot image. */
    public static Address BOOT_IMAGE_CODE_END;
    /** The address of the end of the ref map section of the boot image. */
    public static Address BOOT_IMAGE_RMAP_END;
    
    /** The address of the end of the boot image. */
    public static Address BOOT_IMAGE_END;
   
   public static void setDataImageStart(int address)
   {
       BOOT_IMAGE_DATA_START = Address.fromIntZeroExtend(address);
       if(BOOT_IMAGE_DATA_SIZE > 0)
       {
           /*
            * Assuming that autosizing is used so rounding down to nearest 1MB boundary
            */
           BOOT_IMAGE_DATA_END = BOOT_IMAGE_DATA_START.plus(BOOT_IMAGE_DATA_SIZE & 0xFFE0000);
       }
   }
   
   public static void setDataImageSize(int size)
   {
       BOOT_IMAGE_DATA_SIZE = size;
       MAX_BOOT_IMAGE_RMAP_SIZE = size / BAD_MAP_COMPRESSION;
       if(BOOT_IMAGE_DATA_START != null)
       {
           /*
            * Assuming that autosizing is used so rounding down to nearest 1MB boundary
            */
           BOOT_IMAGE_DATA_END = BOOT_IMAGE_DATA_START.plus(BOOT_IMAGE_DATA_SIZE & 0xFFE0000);
           BOOT_IMAGE_CODE_START = BOOT_IMAGE_DATA_END;
       }
       /*
        * Calculate RMAP END size if possible
        */
       if(BOOT_IMAGE_RMAP_START != null)
       {
           BOOT_IMAGE_RMAP_END = BOOT_IMAGE_RMAP_START.plus(MAX_BOOT_IMAGE_RMAP_SIZE);
           BOOT_IMAGE_END = BOOT_IMAGE_RMAP_END;
       }
   }
   
   public static void setCodeImageStart(int address)
   {
       BOOT_IMAGE_CODE_START = Address.fromIntZeroExtend(address);
       if(BOOT_IMAGE_CODE_SIZE > 0)
       {
           /*
            * Assuming that auto size is used so round by 0x10000
            */
           BOOT_IMAGE_CODE_END = BOOT_IMAGE_CODE_START.plus((BOOT_IMAGE_CODE_SIZE + 0x10000) & 0xFFFE0000);
           BOOT_IMAGE_RMAP_START = BOOT_IMAGE_CODE_END;
           BOOT_IMAGE_RMAP_END = BOOT_IMAGE_RMAP_START.plus(MAX_BOOT_IMAGE_RMAP_SIZE);
           BOOT_IMAGE_END = BOOT_IMAGE_RMAP_END;
       }
   }
   
   public static void setCodeImageSize(int size)
   {
       BOOT_IMAGE_CODE_SIZE = size;
       if(BOOT_IMAGE_CODE_START != null)
       {
           BOOT_IMAGE_CODE_END = BOOT_IMAGE_CODE_START.plus((BOOT_IMAGE_CODE_SIZE + 0x10000) & 0xFFFE0000);
           BOOT_IMAGE_RMAP_START = BOOT_IMAGE_CODE_END;
           BOOT_IMAGE_RMAP_END = BOOT_IMAGE_RMAP_START.plus(MAX_BOOT_IMAGE_RMAP_SIZE);
           BOOT_IMAGE_END = BOOT_IMAGE_RMAP_END;
       }
   }
}
