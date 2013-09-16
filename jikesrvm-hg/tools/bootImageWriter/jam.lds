OUTPUT_FORMAT("elf32-i386")
start = 0x100100;
ENTRY(start)
SECTIONS
{
  .text 0x100000 : 
  AT(0x100000)
  {
    *(.jam)
  }
}
