# OUTPUT_FORMAT("elf32-i386")
start = 0x100100;
ENTRY(start)
SECTIONS
{
  .init 0x100000 : 
  AT(0x100000)
  {
    *(.jam.startup)
  }
  .data 0x00101000 :
  {
  	*(.jam.data)
  }
  .text 0x02000000 :
  {
  	*(.jam.code)
  }
  .rmap 0x04000000 :
  {
  	*(.jam.rmap)
  }
}
