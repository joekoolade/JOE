#include <gcj/cni.h>
#include <java/lang/Object.h>
#include <baremetal/processor/Pentium.h>

void
baremetal::processor::Pentium::hlt() {
	asm("hlt");
}

jlong
baremetal::processor::Pentium::rdtsc() {
	jlong tsc=0;
	asm("rdtsc": "=A" (tsc));
	return tsc;
}

void
baremetal::processor::Pentium::fninit() {
  asm("fninit");
}

jint
baremetal::processor::Pentium::cr0() {
  jint reg;
  asm("mov %%cr0,%[r]" : [r] "=r" (reg));
  return reg;
}

jint
baremetal::processor::Pentium::cr2() {
  jint reg;
  asm("mov %%cr2,%[r]" : [r] "=r" (reg));
  return reg;
}

jint
baremetal::processor::Pentium::cr3() {
  jint reg;
  asm("mov %%cr3,%[r]" : [r] "=r" (reg));
  return reg;
}

jint
baremetal::processor::Pentium::cr4() {
  jint reg;
  asm("mov %%cr4,%[r]" : [r] "=r" (reg));
  return reg;
}

void
baremetal::processor::Pentium::cr0(jint val) {
  asm("mov %[val],%%cr0" :: [val] "r" (val));
}

void
baremetal::processor::Pentium::cr2(jint val) {
  asm("mov %[val],%%cr2" :: [val] "r" (val));
}

void
baremetal::processor::Pentium::cr3(jint val) {
  asm("mov %[val],%%cr3" :: [val] "r" (val));
}

void
baremetal::processor::Pentium::cr4(jint val) {
  asm("mov %[val],%%cr4" :: [val] "r" (val));
}

void
baremetal::processor::Pentium::sti() {
  asm("sti");
}

void
baremetal::processor::Pentium::cli() {
  asm("cli");
}
