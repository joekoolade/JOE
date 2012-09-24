//===--------- Disassembler.cc - Intefarce to disassembler ----------------===//
//
//                      Micro Virtual Machine
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "mvm/JIT.h"

#ifdef HAVE_DISASSEMBLER

#if defined(__PPC__)
extern "C"
{
# include <dis-asm.h>
# include <bfd.h>
}



static struct disassemble_info  info;
static int      initialised= 0;  

// this is the only function exported from this file

int mvm::MvmModule::disassemble(unsigned int *addr)
{
  
  if (!initialised)
    {   
      INIT_DISASSEMBLE_INFO(info, stdout, fprintf);
      info.flavour=   bfd_target_elf_flavour;
      info.arch=    bfd_arch_powerpc;
      info.mach=    bfd_mach_ppc_750; // generic(ish) == PPC G3
      info.endian=    BFD_ENDIAN_BIG;
      info.buffer_length= 65536;
    }   
  info.buffer=     (bfd_byte *)addr;
  info.buffer_vma= (bfd_vma)(long)addr;
  return print_insn_big_powerpc((bfd_vma)(long)addr, &info);
  
}

#elif defined(__i386__)
extern "C"
{
# include <bfd.h>	// bfd types
# include <dis-asm.h>	// disassemble_info
  int print_insn_i386_att(bfd_vma, disassemble_info *);
}


static struct disassemble_info	info;
static int			initialised= 0;


int mvm::MvmModule::disassemble(unsigned int *addr)
{
  if (!initialised)
    {
      INIT_DISASSEMBLE_INFO(info, stdout, fprintf);
      info.flavour=	  bfd_target_elf_flavour;
      info.arch=	  bfd_arch_i386;
      info.mach=	  bfd_mach_i386_i386;
      info.endian=	  BFD_ENDIAN_LITTLE;
      info.buffer_length= 65536;
    }
  info.buffer=	   (bfd_byte *)addr;
  info.buffer_vma= (bfd_vma)(long)addr;
  return print_insn_i386_att((bfd_vma)(long)addr, &info);
}

#else

int mvm::MvmModule::disassemble(unsigned int* addr) {
  return 0;
}

#endif

#else

int mvm::MvmModule::disassemble(unsigned int* addr) {
  return 0;
}

#endif


